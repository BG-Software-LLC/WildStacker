package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@SuppressWarnings("unused")
public final class BucketsListener implements Listener {

    private final WildStackerPlugin plugin;

    public BucketsListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketUse(PlayerBucketFillEvent e) {
        if (plugin.getSettings().bucketsStackerEnabled && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            Scheduler.runTask(e.getPlayer(), () -> ItemUtils.stackBucket(e.getItemStack(), e.getPlayer().getInventory()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketUse(PlayerBucketEmptyEvent e) {
        PlayerInventory inventory = e.getPlayer().getInventory();

        EquipmentSlot usedHand = ItemUtils.getHand(e);
        ItemStack itemInHand = ItemUtils.getItemFromHand(inventory, usedHand);

        if (itemInHand == null || itemInHand.getAmount() <= 1)
            return;

        Scheduler.runTask(e.getPlayer(), () -> {
            ItemStack newItemInHand = ItemUtils.getItemFromHand(inventory, usedHand);

            if (newItemInHand == null || newItemInHand.getType() != Material.BUCKET)
                return;

            itemInHand.setAmount(itemInHand.getAmount() - 1);
            ItemUtils.setItemInHand(inventory, usedHand, itemInHand);
            ItemUtils.addItem(newItemInHand, inventory, e.getPlayer().getLocation());
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!plugin.getSettings().bucketsStackerEnabled)
            return;

        //Current Item - in slot, Cursor - holded item
        if (e.getCurrentItem() == null || (e.getCurrentItem().getType() != Material.WATER_BUCKET && e.getCurrentItem().getType() != Material.LAVA_BUCKET))
            return;

        if (e.getView().getTopInventory().getType() != InventoryType.CHEST)
            return;

        ItemStack cursor, clicked;
        int maxStack = plugin.getSettings().bucketsMaxStack;

        switch (e.getClick()) {
            case MIDDLE:
                if (e.getWhoClicked().getGameMode() != GameMode.CREATIVE)
                    return;

                clicked = e.getCurrentItem().clone();
                cursor = clicked.clone();
                cursor.setAmount(maxStack);
                e.getWhoClicked().getOpenInventory().setCursor(cursor);
                break;
            case RIGHT:
            case LEFT:
                if (e.getCursor() == null || (e.getCursor().getType() != Material.WATER_BUCKET && e.getCursor().getType() != Material.LAVA_BUCKET) ||
                        !e.getCursor().isSimilar(e.getCurrentItem()))
                    return;

                e.setCancelled(true);

                if (e.getCurrentItem().getAmount() >= maxStack)
                    return;

                int toAdd = maxStack - e.getCurrentItem().getAmount();

                if (toAdd > e.getCursor().getAmount())
                    toAdd = e.getCursor().getAmount();

                if (e.getClick() == ClickType.RIGHT)
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

                clicked = e.getCurrentItem().clone();

                Scheduler.runTask(e.getWhoClicked(), () -> ItemUtils.stackBucket(clicked, invToAddItem), 1L);
                return;
            default:
                return;
        }

        for (HumanEntity humanEntity : e.getInventory().getViewers()) {
            if (humanEntity instanceof Player)
                ((Player) humanEntity).updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void g(PlayerItemConsumeEvent e) {
        if (!plugin.getSettings().bucketsStackerEnabled || e.getItem().getType() != Material.MILK_BUCKET)
            return;

        if (e.getItem().getAmount() > 1)
            e.getPlayer().getInventory().addItem(new ItemStack(Material.BUCKET));
    }

}