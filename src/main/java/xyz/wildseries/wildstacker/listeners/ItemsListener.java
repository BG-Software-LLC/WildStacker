package xyz.wildseries.wildstacker.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedItem;
import xyz.wildseries.wildstacker.objects.WStackedItem;
import xyz.wildseries.wildstacker.utils.SafeStacker;
import xyz.wildseries.wildstacker.utils.async.AsyncCallback;

@SuppressWarnings("unused")
public final class ItemsListener implements Listener {

    private WildStackerPlugin plugin;

    public ItemsListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e){
        if(!plugin.getSettings().itemsStackingEnabled || plugin.getSettings().blacklistedItems.contains(e.getEntity().getItemStack()))
            return;

        if(plugin.getSettings().itemsDisabledWorlds.contains(e.getEntity().getWorld().getName()))
            return;

        StackedItem item = WStackedItem.of(e.getEntity());
        int limit;

        if(item.getStackAmount() > (limit = plugin.getSettings().itemsLimits.get(item.getItemStack()))){
            ItemStack cloned = item.getItemStack().clone();
            cloned.setAmount(cloned.getAmount() - limit);
            item.setStackAmount(limit, true);
            Item spawnedItem = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), cloned);
            spawnedItem.setPickupDelay(40);
        }

        SafeStacker.tryStack(item, new AsyncCallback<Item>() {
            @Override
            public void run(Item returnValue) {
                if(returnValue == null) {
                    //Set the amount of item-stack to 1
                    ItemStack is = item.getItemStack();
                    is.setAmount(1);
                    item.setItemStack(is);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent e){
        if(!plugin.getSettings().itemsStackingEnabled)
            return;

        if(plugin.getSettings().blacklistedItems.contains(e.getEntity().getItemStack()))
            return;

        //We are overriding the merge system
        e.setCancelled(true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(e.getEntity().isValid() && e.getTarget().isValid()){
                StackedItem stackedItem = WStackedItem.of(e.getEntity()), targetItem = WStackedItem.of(e.getTarget());
                SafeStacker.tryStackInto(stackedItem, targetItem);
            }
        }, 5L);
    }

    //This method will be fired even if stacking-drops is disabled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e){
        WStackedItem.of(e.getEntity()).remove();
    }

    //This method will be fired even if stacking-drops is disabled.
    //Priority is high so it will run before McMMO
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPickup(PlayerPickupItemEvent e) {
        if(e.getItem() == null)
            return;

        StackedItem stackedItem = WStackedItem.of(e.getItem());

        //Should run only if the item is 1 (stacked item)
        if(stackedItem.getStackAmount() > 1 || e.getItem().getItemStack().getType().name().contains("BUCKET")) {
            e.setCancelled(true);

            int stackAmount = stackedItem.getStackAmount();

            stackedItem.giveItemStack(e.getPlayer().getInventory());

            if(stackAmount != stackedItem.getStackAmount()){
                Sound pickUpItem;

                //Different name on 1.12
                try {
                    pickUpItem = Sound.valueOf("ITEM_PICKUP");
                } catch (IllegalArgumentException ex) {
                    pickUpItem = Sound.valueOf("ENTITY_ITEM_PICKUP");
                }
                e.getPlayer().playSound(e.getPlayer().getLocation(), pickUpItem, 1, 1);
            }

            if (stackedItem.getStackAmount() <= 0) {
                e.getItem().remove();
                stackedItem.remove();
            }
        }
    }

    //This method will be fired even if stacking-drops is disabled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryPickup(InventoryPickupItemEvent e){
        StackedItem stackedItem = WStackedItem.of(e.getItem());
        if(stackedItem.getStackAmount() > 1) {
            e.setCancelled(true);
            stackedItem.giveItemStack(e.getInventory());
            if (stackedItem.getStackAmount() <= 0) {
                stackedItem.remove();
            }

            Block hopper = e.getItem().getLocation().subtract(0, 1, 0).getBlock();
            hopper.getState().update();
        }
    }

}
