package com.bgsoftware.wildstacker.utils.items;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.hooks.WildToolsHook;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class ItemUtils {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final int MAX_PICKUP_DELAY = 32767;

    public static void addItems(ItemStack[] itemStacks, Inventory inventory, Location location){
        Arrays.stream(itemStacks)
                .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                .forEach(itemStack -> addItem(itemStack, inventory, location));
    }

    public static void addItem(ItemStack itemStack, Inventory inventory, Location location){
        HashMap<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);

        if(itemStack.getType().name().contains("BUCKET"))
            stackBucket(itemStack, inventory);
        if(itemStack.getType().name().contains("STEW") || itemStack.getType().name().contains("SOUP"))
            stackStew(itemStack, inventory);

        if (location != null && !additionalItems.isEmpty()) {
            for (ItemStack additional : additionalItems.values())
                dropItem(additional, location);
        }
    }

    public static void dropItem(ItemStack itemStack, Location location){
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> dropItem(itemStack, location));
            return;
        }

        if(itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0)
            return;

        int amount = itemStack.getAmount();

        if(plugin.getSettings().itemsStackingEnabled && canBeStacked(itemStack, location.getWorld())){
            ItemStack cloned = itemStack.clone();
            cloned.setAmount(Math.min(itemStack.getMaxStackSize(), amount));
            plugin.getSystemManager().spawnItemWithAmount(location, cloned, amount);
        }

        else {
            for (int i = 0; i < amount / 64; i++) {
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(64);
                plugin.getSystemManager().spawnItemWithAmount(location, cloned, 64);
            }

            if (amount % 64 > 0) {
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(amount % 64);
                plugin.getSystemManager().spawnItemWithAmount(location, cloned);
            }
        }
    }

    public static ItemStack setSpawnerItemAmount(ItemStack itemStack, int amount){
        return plugin.getNMSAdapter().setTag(itemStack, "spawners-amount", amount);
    }

    public static int getSpawnerItemAmount(ItemStack itemStack){
        int spawnersAmount = plugin.getNMSAdapter().getTag(itemStack, "spawners-amount", Integer.class, 1);

//        if(spawnersAmount <= 0 && itemStack.getItemMeta().hasDisplayName()){
//            String displayName = itemStack.getItemMeta().getDisplayName();
//            Matcher matcher = plugin.getSettings().SPAWNERS_PATTERN.matcher(displayName);
//            if(matcher.matches()) {
//                List<String> indexes = Stream.of("0", "1", "2")
//                        .sorted(Comparator.comparingInt(o -> displayName.indexOf("{" + o + "}"))).collect(Collectors.toList());
//                try {
//                    spawnersAmount = Integer.parseInt(matcher.group(indexes.indexOf("0") + 1));
//                }catch(Throwable ex){
//                    spawnersAmount = 1;
//                }
//            }
//        }

        return Math.max(1, spawnersAmount);
    }

    public static ItemStack getSpawnerItem(EntityType entityType, int amount){
//        if(SpawnersProvider_SilkSpawners.isRegisered()){
//            return SpawnersProvider_SilkSpawners.getSpawnerItem(entityType, amount);
//        }
        return plugin.getProviders().getSpawnerItem(entityType, amount);
    }

    @SuppressWarnings("deprecation")
    public static EntityTypes getEntityType(ItemStack itemStack){
        if(!Materials.isValidAndSpawnEgg(itemStack))
            throw new IllegalArgumentException("Only spawn-eggs can be used in ItemUtil#getEntityType");

        try {
            if (ServerVersion.isLegacy()) {
                try {
                    SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemStack.getItemMeta();
                    return spawnEggMeta.getSpawnedType() == null ? EntityTypes.PIG : EntityTypes.fromName(spawnEggMeta.getSpawnedType().name());
                } catch (NoClassDefFoundError error) {
                    return EntityTypes.fromName(EntityType.fromId(itemStack.getDurability()).name());
                }
            } else {
                return EntityTypes.fromName(itemStack.getType().name().replace("_SPAWN_EGG", ""));
            }
        }catch(NullPointerException ex){
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static void setEntityType(ItemStack itemStack, EntityType entityType){
        if(!Materials.isValidAndSpawnEgg(itemStack))
            throw new IllegalArgumentException("Only spawn-eggs can be used in ItemUtil#getEntityType");

        if(ServerVersion.isLegacy()) {
            try {
                SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemStack.getItemMeta();
                spawnEggMeta.setSpawnedType(entityType);
                itemStack.setItemMeta(spawnEggMeta);
            } catch (NoClassDefFoundError error) {
                itemStack.setDurability(entityType.getTypeId());
            }
        }else{
            itemStack.setType(Material.valueOf(EntityTypes.fromName(entityType.name()).name() + "_SPAWN_EGG"));
        }
    }

    public static ItemStack getItemNMSEntityType(EntityType entityType){
        ItemStack itemStack = new ItemStack(Materials.getSpawnEgg(EntityType.GHAST));
        return plugin.getNMSAdapter().setTag(itemStack, "entity-type", entityType.name());
    }

    public static EntityType getNMSEntityType(ItemStack itemStack){
        String entityType = plugin.getNMSAdapter().getTag(itemStack, "entity-type", String.class, "");
        return entityType.isEmpty() ? null : EntityType.valueOf(entityType);
    }

    public static String getFormattedType(ItemStack itemStack){
        String typeName = itemStack.getType().name().contains("LEGACY") ? itemStack.getType().name().replace("LEGACY_", "") : itemStack.getType().name();

        typeName = plugin.getSettings().customNames.getOrDefault(itemStack, typeName);

        return EntityUtils.getFormattedType(typeName);
    }

    public static void stackBucket(ItemStack bucket, Inventory inventory){
        if(plugin.getSettings().bucketsStackerEnabled)
            stackItems(bucket, inventory, plugin.getSettings().bucketsMaxStack);
    }

    public static void stackStew(ItemStack stew, Inventory inventory){
        if(plugin.getSettings().stewsStackingEnabled)
            stackItems(stew, inventory, plugin.getSettings().stewsMaxStack);
    }

    private static void stackItems(ItemStack item, Inventory inventory, int maxStack){
        int amountOfItems = 0;
        int slotToSetFirstItem = -1;

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && itemStack.isSimilar(item)) {
                if(slotToSetFirstItem == -1)
                    slotToSetFirstItem = slot;
                amountOfItems += itemStack.getAmount();
                inventory.setItem(slot, new ItemStack(Material.AIR));
            }
        }

        updateInventory(inventory);

        ItemStack cloned = item.clone();
        cloned.setAmount(maxStack);

        for(int i = 0; i < amountOfItems / maxStack; i++) {
            if(slotToSetFirstItem != -1){
                inventory.setItem(slotToSetFirstItem, cloned);
                slotToSetFirstItem = -1;
            }else {
                inventory.addItem(cloned);
            }
        }

        if(amountOfItems % maxStack > 0){
            cloned.setAmount(amountOfItems % maxStack);
            if(slotToSetFirstItem != -1){
                inventory.setItem(slotToSetFirstItem, cloned);
            }
            else {
                inventory.addItem(cloned);
            }
        }

        updateInventory(inventory);
    }

    public static ItemStack getFromBlock(Block block){
        ItemStack itemStack = ServerVersion.isLegacy() ? block.getState().getData().toItemStack(1) : new ItemStack(block.getType());
        if(plugin.getNMSAdapter().isRotatable(block))
            itemStack.setDurability((short) 0);
        return itemStack;
    }

    private static void updateInventory(Inventory inventory){
        inventory.getViewers().stream().filter(humanEntity -> humanEntity instanceof Player)
                .forEach(player -> ((Player) player).updateInventory());
    }

    public static ItemStack getFromConfig(ConfigurationSection section){
        Material type;

        try{
            type = Material.valueOf(section.getString("type"));
        }catch(IllegalArgumentException ex){
            WildStackerPlugin.log("Couldn't find a valid type for fill item " + section.getName() + " - skipping...");
            return null;
        }

        short data = (short) section.getInt("data", 0);

        ItemStack itemStack = new ItemStack(type, 1, data);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if(section.contains("name")){
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));
        }

        if(section.contains("lore")){
            List<String> lore = new ArrayList<>();

            for(String line : section.getStringList("lore")){
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @SuppressWarnings({"JavaReflectionMemberAccess", "unused"})
    public static void removeItem(ItemStack itemStack, PlayerInteractEvent event){
        try{
            EquipmentSlot equipmentSlot = (EquipmentSlot) PlayerInteractEvent.class.getMethod("getHand").invoke(event);
            if(equipmentSlot.name().equals("OFF_HAND")){
                ItemStack offHand = (ItemStack) PlayerInventory.class.getMethod("getItemInOffHand").invoke(event.getPlayer().getInventory());
                if(offHand.isSimilar(itemStack)){
                    offHand.setAmount(offHand.getAmount() - itemStack.getAmount());
                    PlayerInventory.class.getMethod("setItemInOffHand", ItemStack.class)
                            .invoke(event.getPlayer().getInventory(), offHand);
                }
            }
        }catch(Exception ignored){}

        event.getPlayer().getInventory().removeItem(itemStack);
    }

    public static int countItem(Inventory inventory, ItemStack itemStack){
        int counter = 0;

        for(ItemStack _itemStack : inventory.getContents()){
            if(_itemStack != null && _itemStack.isSimilar(itemStack))
                counter += _itemStack.getAmount();
        }

        return counter;
    }

    public static void removeItem(Inventory inventory, ItemStack itemStack, int amount){
        int amountRemoved = 0;

        for(int i = 0; i < inventory.getSize() && amountRemoved < amount; i++){
            ItemStack _itemStack = inventory.getItem(i);
            if(_itemStack != null && _itemStack.isSimilar(itemStack)){
                if(amountRemoved + _itemStack.getAmount() <= amount){
                    amountRemoved += _itemStack.getAmount();
                    inventory.setItem(i, new ItemStack(Material.AIR));
                }else{
                    _itemStack.setAmount(_itemStack.getAmount() - amount + amountRemoved);
                    amountRemoved = amount;
                }
            }
        }
    }

    public static int getHeldItemSlot(PlayerInventory inventory, Material material){
        ItemStack itemStack;

        if((itemStack = inventory.getItem(inventory.getHeldItemSlot())) != null && itemStack.getType() == material)
            return inventory.getHeldItemSlot();

        else try{
            if((itemStack = inventory.getItem(40)) != null && itemStack.getType() == material)
                return 40;
        }catch(ArrayIndexOutOfBoundsException ignored){}

        return -1;
    }

    public static void setItemInHand(PlayerInventory inventory, ItemStack inHand, ItemStack itemStack){
        int slot = getHeldItemSlot(inventory, inHand.getType());
        if(slot != -1)
            inventory.setItem(slot, itemStack);
    }

    public static boolean canPickup(Item item){
        return item.getPickupDelay() < MAX_PICKUP_DELAY && !item.hasMetadata("ChestShop_Display") && !item.hasMetadata("no_pickup");
    }

    public static boolean isOffHand(Event event){
        try{
            return event.getClass().getMethod("getHand").invoke(event).toString().equals("OFF_HAND");
        }catch(Throwable ex){
            return false;
        }
    }

    public static boolean isSword(Material material){
        return material.name().contains("SWORD");
    }

    public static boolean isTool(Material material){
        switch (material.name()){
            case "IRON_SPADE":
            case "IRON_PICKAXE":
            case "IRON_AXE":
            case "WOOD_SPADE":
            case "WOOD_PICKAXE":
            case "WOOD_AXE":
            case "STONE_SPADE":
            case "STONE_PICKAXE":
            case "STONE_AXE":
            case "DIAMOND_SPADE":
            case "DIAMOND_PICKAXE":
            case "DIAMOND_AXE":
            case "GOLD_SPADE":
            case "GOLD_PICKAXE":
            case "GOLD_AXE":
            case "GOLDEN_SHOVEL":
            case "GOLDEN_PICKAXE":
            case "GOLDEN_AXE":
                return true;
            default:
                return false;
        }
    }

    public static boolean isPickaxeAndHasSilkTouch(ItemStack itemStack){
        if(itemStack == null || !itemStack.getType().name().contains("PICKAXE"))
            return false;

        return WildToolsHook.hasSilkTouch(itemStack) || itemStack.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= 1;
    }

    public static Location getSafeDropLocation(Location origin){
        Location location;

        if((location = origin.clone().add(0, 0.5, 0)).getBlock().getType() == Material.AIR)
            return location;
        else if((location = origin.clone().subtract(0, 0.5, 0)).getBlock().getType() == Material.AIR)
            return location;

        return origin;
    }

    private static boolean canBeStacked(ItemStack itemStack, World world){
        return !plugin.getSettings().blacklistedItems.contains(itemStack) &&
                (plugin.getSettings().whitelistedItems.isEmpty() || plugin.getSettings().whitelistedItems.contains(itemStack)) &&
                !plugin.getSettings().itemsDisabledWorlds.contains(world.getName());
    }

}
