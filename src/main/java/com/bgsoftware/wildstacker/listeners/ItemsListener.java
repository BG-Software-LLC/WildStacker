package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.listeners.events.EventsListener;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class ItemsListener implements Listener {

    private static final ReflectMethod<Void> ENTITY_EQUIPMENT_SET_ITEM_IN_MAIN_HAND = new ReflectMethod<>(EntityEquipment.class, "setItemInMainHand", ItemStack.class);

    private final WildStackerPlugin plugin;

    public ItemsListener(WildStackerPlugin plugin) {
        this.plugin = plugin;

        if (ServerVersion.isAtLeast(ServerVersion.v1_8))
            plugin.getServer().getPluginManager().registerEvents(new MergeListener(plugin), plugin);

        try {
            Class.forName("org.bukkit.event.block.BlockDropItemEvent");
            plugin.getServer().getPluginManager().registerEvents(new BlockDropItemListener(plugin), plugin);
        } catch (Exception ignored) {
        }

        EventsListener.registerEggLayListener(this::onEggLay);
        EventsListener.registerScuteDropListener(this::onScuteDrop);
        EventsListener.addEntityPickupListener(this::onEntityPickup, EventPriority.HIGHEST);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e) {
        if (!plugin.getSettings().itemsStackingEnabled || e.getEntity().getType() != EntityType.DROPPED_ITEM)
            return;

        StackedItem stackedItem = WStackedItem.ofBypass(e.getEntity());

        if (!stackedItem.isCached())
            return;

        EntitiesGetter.handleEntitySpawn(e.getEntity());

        int limit = stackedItem.getStackLimit();

        if (stackedItem.getStackAmount() > limit) {
            ItemStack cloned = stackedItem.getItemStack().clone();
            cloned.setAmount(cloned.getAmount() - limit);
            stackedItem.setStackAmount(limit, true);
            StackedItem spawnedItem = plugin.getSystemManager().spawnItemWithAmount(e.getEntity().getLocation(), cloned);
            spawnedItem.getItem().setPickupDelay(40);
        }

        stackedItem.runStackAsync(optionalItem -> {
            if (optionalItem.isPresent())
                return;

            Scheduler.runTask(() -> {
                if (EntityStorage.hasMetadata(e.getEntity(), EntityFlag.DROPPED_BY_PLAYER)) {
                    EntityStorage.removeMetadata(e.getEntity(), EntityFlag.DROPPED_BY_PLAYER);
                } else {
                    if(Scheduler.isRegionScheduler()) {
                        Scheduler.runTask(e.getLocation(), () -> finishItemSpawnChunkLimit(stackedItem, e.getLocation()));
                    } else {
                        finishItemSpawnChunkLimit(stackedItem, e.getLocation());
                    }
                }
            });
        });
    }

    private void finishItemSpawnChunkLimit(StackedItem stackedItem, Location location) {
        if (isChunkLimit(location.getChunk()))
            stackedItem.remove();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemDrop(PlayerDropItemEvent e) {
        EntityStorage.setMetadata(e.getItemDrop(), EntityFlag.DROPPED_BY_PLAYER, true);
    }

    private void onEggLay(Chicken chicken, Item egg) {
        if (!plugin.getSettings().eggLayMultiply || !EntityUtils.isStackable(chicken))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(chicken);
        if (stackedEntity.getStackAmount() > 1) {
            ItemStack eggItem = egg.getItemStack();
            eggItem.setAmount(stackedEntity.getStackAmount());
            egg.setItemStack(eggItem);
        }
    }

    //This method will be fired even if stacking-drops is disabled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e) {
        if (ItemUtils.isStackable(e.getEntity()))
            WStackedItem.of(e.getEntity()).remove();
    }

    private boolean onEntityPickup(Cancellable event, StackedItem stackedItem, LivingEntity livingEntity, int remaining) {
        if (EntitiesListener.IMP.secondPickupEventCall) {
            EntitiesListener.IMP.secondPickupEvent = event;
            return false;
        }

        if (((WStackedItem) stackedItem).isRemoved())
            return true;

        Item item = stackedItem.getItem();

        if (EntityStorage.hasMetadata(item, EntityFlag.RECENTLY_PICKED_UP)) {
            EntityStorage.removeMetadata(item, EntityFlag.RECENTLY_PICKED_UP);
            stackedItem.remove();
            return true;
        }

        // Should run only if items stacking is enabled, or the item's stack size is larger than the max stack size.
        // Another case is if buckets stacking is enabled and the item is a bucket.
        if (plugin.getSettings().itemsStackingEnabled || (stackedItem.getStackAmount() > stackedItem.getItemStack().getMaxStackSize() ||
                (plugin.getSettings().bucketsStackerEnabled && stackedItem.getItemStack().getType().name().contains("BUCKET")))) {
            plugin.getNMSEntities().handleItemPickup(livingEntity, stackedItem, remaining);
            return true;
        }

        return false;
    }

    //This method will be fired even if stacking-drops is disabled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryPickup(InventoryPickupItemEvent e) {
        if (!ItemUtils.isStackable(e.getItem()))
            return;

        // We don't want removed items to be picked up.
        if (EntityStorage.hasMetadata(e.getItem(), EntityFlag.REMOVED_ENTITY)) {
            e.getItem().remove(); // Remove it again, synchronized.
            e.setCancelled(true);
            return;
        }

        StackedItem stackedItem = WStackedItem.of(e.getItem());

        if (stackedItem.getStackAmount() > 1) {
            e.setCancelled(true);
            stackedItem.giveItemStack(e.getInventory());
            InventoryHolder inventoryHolder = e.getInventory().getHolder();
            if (inventoryHolder instanceof Hopper)
                ((Hopper) inventoryHolder).getBlock().getState().update();
        }
    }

    private void onScuteDrop(Entity turtle, Item scute) {
        if (!plugin.getSettings().scuteMultiply || !EntityUtils.isStackable(turtle))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(turtle);
        ItemStack scuteItem = scute.getItemStack();
        scuteItem.setAmount(scuteItem.getAmount() * stackedEntity.getStackAmount());
        scute.setItemStack(scuteItem);
    }

    private boolean isChunkLimit(Chunk chunk) {
        int chunkLimit = plugin.getSettings().itemsChunkLimit;

        if (chunkLimit <= 0)
            return false;

        return (int) Arrays.stream(chunk.getEntities()).filter(entity -> entity instanceof Item).count() > chunkLimit;
    }

    private void setItemInHand(LivingEntity entity, ItemStack itemStack) {
        if (ENTITY_EQUIPMENT_SET_ITEM_IN_MAIN_HAND.isValid()) {
            ENTITY_EQUIPMENT_SET_ITEM_IN_MAIN_HAND.invoke(entity.getEquipment(), itemStack);
        } else {
            entity.getEquipment().setItemInHand(itemStack);
        }
    }

    private static class MergeListener implements Listener {

        private final WildStackerPlugin plugin;

        private MergeListener(WildStackerPlugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onItemMerge(ItemMergeEvent e) {
            if (!plugin.getSettings().itemsStackingEnabled || !ItemUtils.isStackable(e.getEntity()) ||
                    !ItemUtils.isStackable(e.getTarget()))
                return;

            StackedItem stackedItem = WStackedItem.ofBypass(e.getEntity());

            if (!stackedItem.isCached())
                return;

            //We are overriding the merge system
            e.setCancelled(true);

            Scheduler.runTask(e.getEntity(), () -> {
                // `e.getEntity` and `e.getTarget` should be in the same region
                if (e.getEntity().isValid() && e.getTarget().isValid()) {
                    StackedItem targetItem = WStackedItem.ofBypass(e.getTarget());
                    stackedItem.runStackAsync(targetItem, null);
                }
            }, 5L);
        }

    }

    private static class BlockDropItemListener implements Listener {

        private final WildStackerPlugin plugin;

        private BlockDropItemListener(WildStackerPlugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onBlockDropItem(BlockDropItemEvent e) {
            if (!plugin.getSettings().itemsStackingEnabled)
                return;

            Map<ItemStack, Item> itemsMap = new HashMap<>();
            List<Item> itemsToRemove = new ArrayList<>();

            for (Item item : e.getItems()) {
                ItemStack clone = item.getItemStack().clone();
                clone.setAmount(1);

                StackedItem stackedItem = WStackedItem.ofBypass(item);

                if (stackedItem.isCached()) {
                    Item currentItem = itemsMap.get(clone);

                    if (currentItem == null) {
                        itemsMap.put(clone, item);
                    } else {
                        itemsToRemove.add(item);
                        currentItem.getItemStack().setAmount(currentItem.getItemStack().getAmount() + item.getItemStack().getAmount());
                    }

                    plugin.getSystemManager().removeStackObject(stackedItem);
                }
            }

            e.getItems().removeAll(itemsToRemove);
        }

    }

}
