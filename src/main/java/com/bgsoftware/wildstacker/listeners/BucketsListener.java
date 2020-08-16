package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@SuppressWarnings("unused")
public final class BucketsListener implements Listener {

    private final WildStackerPlugin plugin;

    public BucketsListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketUse(PlayerBucketFillEvent e){
        if(plugin.getSettings().bucketsStackerEnabled && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            Bukkit.getScheduler().runTask(plugin, () -> ItemUtils.stackBucket(e.getItemStack(), e.getPlayer().getInventory()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketUse(PlayerBucketEmptyEvent e){
        if(plugin.getSettings().bucketsStackerEnabled && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);

            PlayerInventory inventory = e.getPlayer().getInventory();
            int heldItemSlot = ItemUtils.getHeldItemSlot(inventory, e.getBucket());
            ItemStack itemInHand = inventory.getItem(heldItemSlot).clone();
            ItemStack itemToGive = itemInHand.clone();
            itemToGive.setAmount(itemToGive.getAmount() - 1);

            if(e.getBlockClicked().getWorld().getEnvironment() != World.Environment.NETHER){
                Block waterBlock = e.getBlockClicked().getRelative(e.getBlockFace());
                waterBlock.setType(Material.WATER);
            }

            inventory.setItem(heldItemSlot, itemToGive);
            inventory.addItem(e.getItemStack());
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
        int maxStack = plugin.getSettings().bucketsMaxStack;

        switch (e.getClick()){
            case MIDDLE:
                if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE)
                    return;

                clicked = e.getCurrentItem().clone();
                cursor = clicked.clone();
                cursor.setAmount(maxStack);
                e.getWhoClicked().getOpenInventory().setCursor(cursor);
                break;
            case RIGHT:
            case LEFT:
                if(e.getCursor() == null || (e.getCursor().getType() != Material.WATER_BUCKET && e.getCursor().getType() != Material.LAVA_BUCKET) ||
                        !e.getCursor().isSimilar(e.getCurrentItem()))
                    return;

                e.setCancelled(true);

                if(e.getCurrentItem().getAmount() >= maxStack)
                    return;

                int toAdd = maxStack - e.getCurrentItem().getAmount();

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

                clicked = e.getCurrentItem().clone();

                Executor.sync(() -> ItemUtils.stackBucket(clicked, invToAddItem), 1L);
                return;
            default:
                return;
        }

        for(HumanEntity humanEntity : e.getInventory().getViewers()) {
            if (humanEntity instanceof Player)
                ((Player) humanEntity).updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void g(PlayerItemConsumeEvent e){
        if(!plugin.getSettings().bucketsStackerEnabled || e.getItem().getType() != Material.MILK_BUCKET)
            return;

        if(e.getItem().getAmount() > 1)
            e.getPlayer().getInventory().addItem(new ItemStack(Material.BUCKET));
    }

}