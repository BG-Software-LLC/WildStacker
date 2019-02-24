package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public final class BucketsListener implements Listener {

    private WildStackerPlugin plugin;

    public BucketsListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketUse(PlayerInteractEvent e){
        if(!plugin.getSettings().bucketsStackerEnabled)
            return;

        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null || !e.getItem().getType().name().contains("BUCKET"))
            return;

        plugin.getProviders().enableBypass(e.getPlayer());
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getProviders().disableBypass(e.getPlayer()), 5L);

        Block toBeReplaced = e.getClickedBlock().getRelative(e.getBlockFace());
        Material replacedType = e.getItem().getType().name().contains("LAVA") ? Material.LAVA : Material.WATER;
        ItemStack bucketToAdd = new ItemStack(Material.BUCKET);
        boolean returnIfNotFullBlock = true;

        switch (e.getItem().getType()){
            case WATER_BUCKET:
                if(toBeReplaced.getType().name().contains("WATER"))
                    returnIfNotFullBlock = false;
                replacedType = Material.WATER;
                bucketToAdd = new ItemStack(Material.BUCKET);
                break;
            case LAVA_BUCKET:
                if(toBeReplaced.getType().name().contains("LAVA"))
                    returnIfNotFullBlock = false;
                replacedType = Material.LAVA;
                bucketToAdd = new ItemStack(Material.BUCKET);
                break;
            case BUCKET:
                replacedType = Material.AIR;
                bucketToAdd = toBeReplaced.getType().name().contains("WATER") ? new ItemStack(Material.WATER_BUCKET) : new ItemStack(Material.LAVA_BUCKET);
                break;
            case MILK_BUCKET:
                return;
        }

        e.setCancelled(true);

        //noinspection deprecation
        if(toBeReplaced.getData() != 0 && returnIfNotFullBlock)
            return;

        if(toBeReplaced.getType() == Material.AIR && e.getItem().getType() == Material.BUCKET)
            return;

        switch (replacedType){
            case WATER:
            case LAVA:
                BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(toBeReplaced, toBeReplaced.getState(), e.getClickedBlock(), e.getItem(), e.getPlayer(), true);
                Bukkit.getPluginManager().callEvent(blockPlaceEvent);
                if(blockPlaceEvent.isCancelled())
                    return;
                break;
            case AIR:
                BlockBreakEvent blockBreakEvent = new BlockBreakEvent(toBeReplaced, e.getPlayer());
                Bukkit.getPluginManager().callEvent(blockBreakEvent);
                if(blockBreakEvent.isCancelled())
                    return;
                break;
        }

        toBeReplaced.setType(replacedType);

        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            ItemStack inHand = e.getItem().clone();
            inHand.setAmount(1);
            ItemUtil.removeItem(inHand, e);

            ItemUtil.addItem(bucketToAdd, e.getPlayer().getInventory(), e.getPlayer().getLocation());
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
                e.setCancelled(true);

                Inventory invToAddItem = e.getWhoClicked().getOpenInventory().getTopInventory();
                if(e.getClickedInventory().equals(e.getWhoClicked().getOpenInventory().getTopInventory()))
                    invToAddItem = e.getWhoClicked().getOpenInventory().getBottomInventory();

                if(ItemUtil.stackBucket(e.getCurrentItem(), invToAddItem) || invToAddItem.addItem(e.getCurrentItem()).isEmpty()){
                    e.setCurrentItem(new ItemStack(Material.AIR));
                }
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
