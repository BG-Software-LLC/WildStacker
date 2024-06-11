package com.bgsoftware.wildstacker.utils.items;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SpawnEggMeta;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

public final class ItemUtils {

    private static final ReflectMethod<EquipmentSlot> PLAYER_INTERACT_EVENT_GET_HAND = new ReflectMethod<>(
            PlayerInteractEvent.class, "getHand");
    private static final ReflectMethod<EquipmentSlot> BLOCK_PLACE_EVENT_GET_HAND = new ReflectMethod<>(
            BlockPlaceEvent.class, "getHand");
    private static final ReflectMethod<EquipmentSlot> PLAYER_INTERACT_ENTITY_EVENT_GET_HAND = new ReflectMethod<>(
            PlayerInteractEntityEvent.class, "getHand");
    private static final ReflectMethod<EquipmentSlot> PLAYER_BUCKET_EVENT_GET_HAND = new ReflectMethod<>(
            PlayerBucketEvent.class, "getHand");
    private static final ReflectMethod<ItemStack> PLAYER_INVENTORY_GET_ITEM_IN_OFFHAND = new ReflectMethod<>(
            PlayerInventory.class, "getItemInOffHand");
    private static final ReflectMethod<ItemStack> PLAYER_INVENTORY_SET_ITEM_IN_OFFHAND = new ReflectMethod<>(
            PlayerInventory.class, "setItemInOffHand", ItemStack.class);

    private static final EquipmentSlot OFF_HAND_EQUIPMENT_SLOT = ((Supplier<EquipmentSlot>) () -> {
        try {
            return EquipmentSlot.valueOf("OFF_HAND");
        } catch (IllegalArgumentException error) {
            return null;
        }
    }).get();

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final int MAX_PICKUP_DELAY = 32767;

    public static void addItems(ItemStack[] itemStacks, Inventory inventory, Location location) {
        Arrays.stream(itemStacks)
                .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                .forEach(itemStack -> addItem(itemStack, inventory, location));
    }

    public static void addItem(ItemStack itemStack, Inventory inventory, Location location) {
        HashMap<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);

        if (itemStack.getType().name().contains("BUCKET"))
            stackBucket(itemStack, inventory);
        if (itemStack.getType().name().contains("STEW") || itemStack.getType().name().contains("SOUP"))
            stackStew(itemStack, inventory);

        if (location != null && !additionalItems.isEmpty()) {
            for (ItemStack additional : additionalItems.values())
                dropItem(additional, location);
        }
    }

    public static void dropItem(ItemStack itemStack, Location location) {
        if (!Bukkit.isPrimaryThread()) {
            Scheduler.runTask(location, () -> dropItem(itemStack, location));
            return;
        }

        if (itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0)
            return;

        int amount = itemStack.getAmount();

        try {
            if (plugin.getSettings().itemsStackingEnabled && canBeStacked(itemStack, location.getWorld())) {
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(Math.min(itemStack.getMaxStackSize(), amount));
                plugin.getSystemManager().spawnItemWithAmount(location, cloned, amount);
            } else {
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
        } catch (Exception ex) {
            WildStackerPlugin.log("Error while dropping " + itemStack + ":");
            ex.printStackTrace();
        }
    }

    public static ItemStack setSpawnerItemAmount(ItemStack itemStack, int amount) {
        return plugin.getNMSAdapter().setTag(itemStack, "spawners-amount", amount);
    }

    public static ItemStack setSpawnerUpgrade(ItemStack itemStack, int upgradeId) {
        return plugin.getNMSAdapter().setTag(itemStack, "spawners-upgrade", upgradeId);
    }

    public static int getSpawnerItemAmount(ItemStack itemStack) {
        int spawnersAmount = plugin.getNMSAdapter().getTag(itemStack, "spawners-amount", Integer.class, 1);
        return Math.max(1, spawnersAmount);
    }

    public static int getSpawnerUpgrade(ItemStack itemStack) {
        int spawnerUpgrade = plugin.getNMSAdapter().getTag(itemStack, "spawners-upgrade", Integer.class, -1);
        return Math.max(-1, spawnerUpgrade);
    }

    public static ItemStack getSpawnerItem(EntityType entityType, int amount, SpawnerUpgrade spawnerUpgrade) {
        return plugin.getProviders().getSpawnersProvider().getSpawnerItem(entityType, amount, spawnerUpgrade);
    }

    @SuppressWarnings("deprecation")
    public static EntityTypes getEntityType(ItemStack itemStack) {
        if (!Materials.isValidAndSpawnEgg(itemStack))
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
        } catch (NullPointerException ex) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static void setEntityType(ItemStack itemStack, EntityType entityType) {
        if (!Materials.isValidAndSpawnEgg(itemStack))
            throw new IllegalArgumentException("Only spawn-eggs can be used in ItemUtil#getEntityType");

        if (ServerVersion.isLegacy()) {
            try {
                SpawnEggMeta spawnEggMeta = (SpawnEggMeta) itemStack.getItemMeta();
                spawnEggMeta.setSpawnedType(entityType);
                itemStack.setItemMeta(spawnEggMeta);
            } catch (NoClassDefFoundError error) {
                itemStack.setDurability(entityType.getTypeId());
            }
        } else {
            itemStack.setType(Material.valueOf(EntityTypes.fromName(entityType.name()).name() + "_SPAWN_EGG"));
        }
    }

    public static ItemStack getItemNMSEntityType(EntityType entityType) {
        ItemStack itemStack = new ItemStack(Materials.getSpawnEgg(EntityType.GHAST));
        return plugin.getNMSAdapter().setTag(itemStack, "entity-type", entityType.name());
    }

    public static EntityType getNMSEntityType(ItemStack itemStack) {
        String entityType = plugin.getNMSAdapter().getTag(itemStack, "entity-type", String.class, "");
        return entityType.isEmpty() ? null : EntityType.valueOf(entityType);
    }

    public static String getFormattedType(ItemStack itemStack) {
        String typeName = itemStack.getType().name().contains("LEGACY") ?
                itemStack.getType().name().replace("LEGACY_", "") : itemStack.getType().name();

        String customName = plugin.getSettings().customNames.get(typeName);
        if (customName != null)
            return customName;

        customName = plugin.getSettings().customNames.get(typeName + ":" + itemStack.getDurability());
        if (customName != null)
            return customName;

        return EntityUtils.getFormattedType(typeName);
    }

    public static void stackBucket(ItemStack bucket, Inventory inventory) {
        if (plugin.getSettings().bucketsStackerEnabled)
            stackItems(bucket, inventory, plugin.getSettings().bucketsMaxStack);
    }

    public static void stackStew(ItemStack stew, Inventory inventory) {
        if (plugin.getSettings().stewsStackingEnabled)
            stackItems(stew, inventory, plugin.getSettings().stewsMaxStack);
    }

    private static void stackItems(ItemStack item, Inventory inventory, int maxStack) {
        int amountOfItems = 0;
        int slotToSetFirstItem = -1;

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && itemStack.isSimilar(item)) {
                if (slotToSetFirstItem == -1)
                    slotToSetFirstItem = slot;
                amountOfItems += itemStack.getAmount();
                inventory.setItem(slot, new ItemStack(Material.AIR));
            }
        }

        updateInventory(inventory);

        ItemStack cloned = item.clone();
        cloned.setAmount(maxStack);

        for (int i = 0; i < amountOfItems / maxStack; i++) {
            if (slotToSetFirstItem != -1) {
                inventory.setItem(slotToSetFirstItem, cloned);
                slotToSetFirstItem = -1;
            } else {
                inventory.addItem(cloned);
            }
        }

        if (amountOfItems % maxStack > 0) {
            cloned.setAmount(amountOfItems % maxStack);
            if (slotToSetFirstItem != -1) {
                inventory.setItem(slotToSetFirstItem, cloned);
            } else {
                inventory.addItem(cloned);
            }
        }

        updateInventory(inventory);
    }

    public static ItemStack getFromBlock(Block block) {
        ItemStack itemStack = ServerVersion.isLegacy() ? block.getState().getData().toItemStack(1) : new ItemStack(block.getType());
        if (plugin.getNMSWorld().isRotatable(block))
            itemStack.setDurability((short) 0);
        return itemStack;
    }

    private static void updateInventory(Inventory inventory) {
        inventory.getViewers().stream().filter(humanEntity -> humanEntity instanceof Player)
                .forEach(player -> ((Player) player).updateInventory());
    }

    public static EquipmentSlot getHand(Event event) {
        ReflectMethod<EquipmentSlot> getHandMethod;

        if (event instanceof PlayerInteractEvent) {
            getHandMethod = PLAYER_INTERACT_EVENT_GET_HAND;
        } else if (event instanceof PlayerInteractEntityEvent) {
            getHandMethod = PLAYER_INTERACT_ENTITY_EVENT_GET_HAND;
        } else if (event instanceof BlockPlaceEvent) {
            getHandMethod = BLOCK_PLACE_EVENT_GET_HAND;
        } else if (event instanceof PlayerBucketEvent) {
            getHandMethod = PLAYER_BUCKET_EVENT_GET_HAND;
        } else {
            throw new IllegalArgumentException("Cannot get used hand for event: " + event.getClass());
        }

        return !getHandMethod.isValid() ? EquipmentSlot.HAND : getHandMethod.invoke(event);
    }

    public static void removeItemFromHand(Player player, int amount, EquipmentSlot equipmentSlot) {
        PlayerInventory playerInventory = player.getInventory();

        if (equipmentSlot == EquipmentSlot.HAND) {
            ItemStack mainHand = playerInventory.getItemInHand();
            if (mainHand != null) {
                mainHand.setAmount(mainHand.getAmount() - amount);
                playerInventory.setItemInHand(mainHand);
            }
        } else if (OFF_HAND_EQUIPMENT_SLOT == equipmentSlot) {
            ItemStack offHand = PLAYER_INVENTORY_GET_ITEM_IN_OFFHAND.invoke(playerInventory);
            if (offHand != null) {
                offHand.setAmount(offHand.getAmount() - amount);
                PLAYER_INVENTORY_SET_ITEM_IN_OFFHAND.invoke(playerInventory, offHand);
            }
        }
    }

    @Nullable
    public static ItemStack getItemInOffhand(PlayerInventory inventory) {
        return OFF_HAND_EQUIPMENT_SLOT == null ? null : getItemFromHand(inventory, OFF_HAND_EQUIPMENT_SLOT);
    }

    public static int countItem(Inventory inventory, ItemStack itemStack) {
        int counter = 0;

        for (ItemStack _itemStack : inventory.getContents()) {
            if (_itemStack != null && _itemStack.isSimilar(itemStack))
                counter += _itemStack.getAmount();
        }

        return counter;
    }

    public static void removeItemFromHand(Inventory inventory, ItemStack itemStack, int amount) {
        int amountRemoved = 0;

        for (int i = 0; i < inventory.getSize() && amountRemoved < amount; i++) {
            ItemStack _itemStack = inventory.getItem(i);
            if (_itemStack != null && _itemStack.isSimilar(itemStack)) {
                if (amountRemoved + _itemStack.getAmount() <= amount) {
                    amountRemoved += _itemStack.getAmount();
                    inventory.setItem(i, new ItemStack(Material.AIR));
                } else {
                    _itemStack.setAmount(_itemStack.getAmount() - amount + amountRemoved);
                    amountRemoved = amount;
                }
            }
        }
    }

    @Nullable
    public static ItemStack getItemFromHand(PlayerInventory inventory, EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.HAND) {
            return inventory.getItemInHand();
        } else if (equipmentSlot == OFF_HAND_EQUIPMENT_SLOT && PLAYER_INVENTORY_GET_ITEM_IN_OFFHAND.isValid()) {
            return PLAYER_INVENTORY_GET_ITEM_IN_OFFHAND.invoke(inventory);
        }

        return null;
    }

    public static void setItemInHand(PlayerInventory inventory, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        if (equipmentSlot == EquipmentSlot.HAND) {
            inventory.setItemInHand(itemStack);
        } else if (equipmentSlot == OFF_HAND_EQUIPMENT_SLOT && PLAYER_INVENTORY_SET_ITEM_IN_OFFHAND.isValid()) {
            PLAYER_INVENTORY_SET_ITEM_IN_OFFHAND.invoke(inventory, itemStack);
        }
    }

    public static boolean canPickup(Item item) {
        return item.getPickupDelay() < MAX_PICKUP_DELAY && !item.hasMetadata("ChestShop_Display") && !item.hasMetadata("no_pickup");
    }

    public static boolean isOffHand(Event event) {
        return OFF_HAND_EQUIPMENT_SLOT == getHand(event);
    }

    public static boolean isSword(Material material) {
        return material.name().contains("SWORD");
    }

    public static boolean isTool(Material material) {
        switch (material.name()) {
            case "IRON_SPADE":
            case "IRON_SHOVEL":
            case "IRON_PICKAXE":
            case "IRON_AXE":
            case "WOOD_SPADE":
            case "WOOD_PICKAXE":
            case "WOOD_AXE":
            case "WOODEN_SHOVEL":
            case "WOODEN_PICKAXE":
            case "WOODEN_AXE":
            case "STONE_SPADE":
            case "STONE_SHOVEL":
            case "STONE_PICKAXE":
            case "STONE_AXE":
            case "DIAMOND_SPADE":
            case "DIAMOND_SHOVEL":
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

    public static boolean isPickaxeAndHasSilkTouch(ItemStack itemStack) {
        if (itemStack == null || !itemStack.getType().name().contains("PICKAXE"))
            return false;

        int requiredLevel = plugin.getSettings().silkTouchMinimumLevel;

        return plugin.getProviders().hasEnchantmentLevel(itemStack, Enchantment.SILK_TOUCH, requiredLevel) ||
                itemStack.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= requiredLevel;
    }

    public static Location getSafeDropLocation(Location origin) {
        Location location;

        if ((location = origin.clone().add(0, 0.5, 0)).getBlock().getType() == Material.AIR)
            return location;
        else if ((location = origin.clone().subtract(0, 0.5, 0)).getBlock().getType() == Material.AIR)
            return location;

        return origin;
    }

    public static boolean isStackable(Entity entity) {
        return entity.isValid() && !entity.isDead() && entity instanceof Item &&
                plugin.getNMSEntities().isDroppedItem((Item) entity);
    }

    public static boolean canBeStacked(ItemStack itemStack, World world) {
        Material itemType = itemStack.getType();
        return !plugin.getSettings().blacklistedItems.contains(itemType) &&
                (plugin.getSettings().whitelistedItems.size() == 0 || plugin.getSettings().whitelistedItems.contains(itemType)) &&
                !plugin.getSettings().itemsDisabledWorlds.contains(world.getName());
    }

}
