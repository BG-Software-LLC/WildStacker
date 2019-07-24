package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.hooks.SpawnersProvider_SilkSpawners;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.reflection.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ItemUtil {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static boolean legacy = !Bukkit.getVersion().contains("1.13") && !Bukkit.getVersion().contains("1.14");

    public static int getFreeSpace(Inventory inventory, ItemStack itemStack){
        int freeSpace = 0, invSize = inventory.getType() == InventoryType.PLAYER ? 36 : inventory.getSize();

        for(int slot = 0; slot < invSize; slot++){
            ItemStack currentItem = inventory.getItem(slot);
            if(currentItem == null || currentItem.getType() == Material.AIR)
                freeSpace += plugin.getSettings().itemsFixStackEnabled ? itemStack.getMaxStackSize() : 64;
            else if(itemStack.isSimilar(currentItem)) {
                int amount = currentItem.getAmount(), maxStack = currentItem.getMaxStackSize();
                if(maxStack < amount)
                   maxStack = 64;

                freeSpace += maxStack - amount;
            }
        }

        return freeSpace;
    }

    public static void addItem(ItemStack itemStack, Inventory inventory, Location location){
        if(!itemStack.getType().name().contains("BUCKET") || !ItemUtil.stackBucket(itemStack, inventory)) {
            HashMap<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);
            if (location != null && !additionalItems.isEmpty()) {
                for (ItemStack additional : additionalItems.values())
                    dropItem(additional, location);
            }
        }
    }

    public static void dropItem(ItemStack itemStack, Location location){
        if(itemStack.getType() == Material.AIR) return;
        int amount = itemStack.getAmount();

        for (int i = 0; i < amount / 64; i++) {
            ItemStack cloned = itemStack.clone();
            cloned.setAmount(64);
            location.getWorld().dropItemNaturally(location, cloned);
        }

        if (amount % 64 > 0) {
            ItemStack cloned = itemStack.clone();
            cloned.setAmount(amount % 64);
            location.getWorld().dropItemNaturally(location, cloned);
        }
    }

    public static ItemStack setSpawnerItemAmount(ItemStack itemStack, int amount){
        return plugin.getNMSAdapter().setTag(itemStack, "spawners-amount", amount);
    }

    public static int getSpawnerItemAmount(ItemStack itemStack){
        return plugin.getNMSAdapter().getTag(itemStack, "spawners-amount", Integer.class);
    }

    public static ItemStack getSpawnerItem(EntityType entityType, int amount){
        if(Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") && SpawnersProvider_SilkSpawners.isRegisered()){
            return SpawnersProvider_SilkSpawners.getSpawnerItem(entityType, amount);
        }

        ItemStack itemStack = Materials.SPAWNER.toBukkitItem(amount);

        if(!plugin.getSettings().silkTouchSpawners)
            return itemStack;

        if(plugin.getSettings().getStackedItem) {
            itemStack.setAmount(1);
            itemStack = ItemUtil.setSpawnerItemAmount(itemStack, amount);
        }

        BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();

        creatureSpawner.setSpawnedType(entityType);

        blockStateMeta.setBlockState(creatureSpawner);

        String customName = plugin.getSettings().silkCustomName;

        if(!customName.equals(""))
            blockStateMeta.setDisplayName(customName.replace("{0}", ItemUtil.getSpawnerItemAmount(itemStack) + "")
                    .replace("{1}", EntityUtil.getFormattedType(entityType.name())));

        itemStack.setItemMeta(blockStateMeta);

        return itemStack;
    }

    @SuppressWarnings("deprecation")
    public static EntityType getEntityType(ItemStack itemStack){
        if(!Materials.isValidAndSpawnEgg(itemStack))
            throw new IllegalArgumentException("Only spawn-eggs can be used in ItemUtil#getEntityType");

        if(legacy) {
            try {
                SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemStack.getItemMeta();
                return spawnEggMeta.getSpawnedType() == null ? EntityType.PIG : spawnEggMeta.getSpawnedType();
            } catch (NoClassDefFoundError error) {
                return EntityType.fromId(itemStack.getDurability());
            }
        }else{
            return EntityTypes.fromName(itemStack.getType().name().replace("_SPAWN_EGG", "")).toBukkit();
        }
    }

    @SuppressWarnings("deprecation")
    public static void setEntityType(ItemStack itemStack, EntityType entityType){
        if(!Materials.isValidAndSpawnEgg(itemStack))
            throw new IllegalArgumentException("Only spawn-eggs can be used in ItemUtil#getEntityType");

        if(legacy) {
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

    public static String getFormattedType(ItemStack itemStack){
        String typeName = itemStack.getType().name().contains("LEGACY") ? itemStack.getType().name().replace("LEGACY_", "") : itemStack.getType().name();

        typeName = ChatColor.stripColor(plugin.getSettings().customNames.getOrDefault(itemStack, typeName));

        return EntityUtil.getFormattedType(typeName);
    }

    @SuppressWarnings("deprecation")
    public static Object getBlockData(Material type, byte data){
        Object iBlockData;

        if(Methods.BLOCK_GET_BY_COMBINED_ID.exists()){
            int combined = type.getId() + (data << 12);
            iBlockData = Methods.BLOCK_GET_BY_COMBINED_ID.invoke(null, combined);
        }
        else{
            iBlockData = Methods.MAGIC_GET_BLOCK.invoke(null, type, data);
        }

        return Methods.BLOCK_DATA_FROM_DATA.invoke(null, iBlockData);
    }

    public static boolean stackBucket(ItemStack bucket, Inventory inventory){
        if(plugin.getSettings().bucketsStackerEnabled) {
            int amountOfBuckets = 0;
            int maxStack = plugin.getSettings().bucketsMaxStack;
            int slotToSetFirstBucket = -1;

            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack itemStack = inventory.getItem(slot);
                if (itemStack != null && itemStack.isSimilar(bucket)) {
                    if(slotToSetFirstBucket == -1)
                        slotToSetFirstBucket = slot;
                    amountOfBuckets += itemStack.getAmount();
                    inventory.setItem(slot, new ItemStack(Material.AIR));
                }
            }

            updateInventory(inventory);

            ItemStack cloned = bucket.clone();
            cloned.setAmount(maxStack);

            for(int i = 0; i < amountOfBuckets / maxStack; i++) {
                if(slotToSetFirstBucket != -1){
                    inventory.setItem(slotToSetFirstBucket, cloned);
                    slotToSetFirstBucket = -1;
                }else {
                    inventory.addItem(cloned);
                }
            }

            if(amountOfBuckets % maxStack > 0){
                cloned.setAmount(amountOfBuckets % maxStack);
                if(slotToSetFirstBucket != -1){
                    inventory.setItem(slotToSetFirstBucket, cloned);
                }
                else {
                    inventory.addItem(cloned);
                }
            }

            updateInventory(inventory);
        }
        return false;
    }

    public static ItemStack getFromBlock(Block block){
        if(legacy)
            return block.getState().getData().toItemStack(1);
        else
            return new ItemStack(block.getType());
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

    public static void setItemInHand(PlayerInventory inventory, ItemStack inHand, ItemStack itemStack){
        int slot = -1;

        if(inHand.equals(inventory.getItem(inventory.getHeldItemSlot())))
            slot = inventory.getHeldItemSlot();

        else try{
            if(inHand.equals(inventory.getItem(40)))
                slot = 40;
        }catch(ArrayIndexOutOfBoundsException ignored){}

        inventory.setItem(slot, itemStack);
    }

}
