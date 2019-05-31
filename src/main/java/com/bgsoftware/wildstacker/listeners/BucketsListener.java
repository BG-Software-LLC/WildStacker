package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public final class BucketsListener implements Listener {

    private WildStackerPlugin plugin;

    public BucketsListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBucketUse(PlayerBucketFillEvent e){
        if(plugin.getSettings().bucketsStackerEnabled) {
            Bukkit.getScheduler().runTask(plugin, () -> ItemUtil.stackBucket(e.getItemStack(), e.getPlayer().getInventory()));
        }
    }

    @EventHandler
    public void onBucketUse(PlayerBucketEmptyEvent e){
        if(plugin.getSettings().bucketsStackerEnabled) {
            ItemStack inHand = e.getPlayer().getItemInHand().clone();
            inHand.setAmount(inHand.getAmount() - 1);

            Bukkit.getScheduler().runTask(plugin, () -> {
                e.getPlayer().setItemInHand(inHand);
                e.getPlayer().getInventory().addItem(e.getItemStack());
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e){
        if(!plugin.getSettings().bucketsStackerEnabled)
            return;

        //Current Item - in slot, Cursor - holded item
        if(e.getCurrentItem() == null || (e.getCurrentItem().getType() != Material.WATER_BUCKET && e.getCurrentItem().getType() != Material.LAVA_BUCKET))
            return;

        if(e.getView().getTopInventory().getType() != InventoryType.CHEST)
            return;

        ItemStack cursor, clicked;

        switch (e.getClick()){
            case MIDDLE:
                if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE)
                    return;

                clicked = e.getCurrentItem().clone();
                cursor = clicked.clone();
                cursor.setAmount(16);
                e.getWhoClicked().getOpenInventory().setCursor(cursor);
                break;
            case RIGHT:
            case LEFT:
                if(e.getCursor() == null || (e.getCursor().getType() != Material.WATER_BUCKET && e.getCursor().getType() != Material.LAVA_BUCKET) ||
                        !e.getCursor().isSimilar(e.getCurrentItem()))
                    return;

                e.setCancelled(true);

                if(e.getCurrentItem().getAmount() >= 16)
                    return;

                int toAdd = 16 - e.getCurrentItem().getAmount();

                if(toAdd > e.getCursor().getAmount())
                    toAdd = e.getCursor().getAmount();

                if(e.getClick() == ClickType.RIGHT)
                    toAdd = 1;

                e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + toAdd);
                cursor = e.getCursor().clone();
                cursor.setAmount(cursor.getAmount() - toAdd);
                //e#setCursor is deprecated, so we can use this manipulate instead.
                e.getWhoClicked().getOpenInventory().setCursor(cursor);
                break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                Inventory invToAddItem = e.getClickedInventory().equals(e.getWhoClicked().getOpenInventory().getTopInventory()) ?
                        e.getWhoClicked().getOpenInventory().getBottomInventory() : e.getWhoClicked().getOpenInventory().getTopInventory();

                clicked = e.getCurrentItem();

                Executor.sync(() -> ItemUtil.stackBucket(clicked, invToAddItem));
                break;
            default:
                return;
        }

        for(HumanEntity humanEntity : e.getInventory().getViewers()) {
            if (humanEntity instanceof Player)
                ((Player) humanEntity).updateInventory();
        }
    }

}
