package xyz.wildseries.wildstacker.listeners;

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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.utils.ItemUtil;

@SuppressWarnings("unused")
public class BucketsListener implements Listener {

    private WildStackerPlugin plugin;

    public BucketsListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent e){
        ItemStack newBucketItem;

        if(e.getBlockClicked().getType().name().contains("LAVA"))
            newBucketItem = new ItemStack(Material.LAVA_BUCKET);
        else if(e.getBlockClicked().getType().name().contains("WATER"))
            newBucketItem = new ItemStack(Material.WATER_BUCKET);
        else return;

        e.setCancelled(true);

        e.getBlockClicked().setType(Material.AIR);

        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ItemStack inHand = e.getPlayer().getItemInHand().clone();
                inHand.setAmount(inHand.getAmount() - 1);
                e.getPlayer().setItemInHand(inHand);
                ItemUtil.addItem(newBucketItem, e.getPlayer().getInventory(), e.getPlayer().getLocation());
                e.getPlayer().updateInventory();
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e){
        Material blockType;

        if(e.getBucket().name().contains("LAVA"))
            blockType = Material.LAVA;
        else if(e.getBucket().name().contains("WATER"))
            blockType = Material.WATER;
        else return;

        e.setCancelled(true);

        e.getBlockClicked().getRelative(e.getBlockFace()).setType(blockType);

        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ItemStack inHand = e.getPlayer().getItemInHand().clone();
                inHand.setAmount(inHand.getAmount() - 1);
                e.getPlayer().setItemInHand(inHand);
                ItemUtil.addItem(new ItemStack(Material.BUCKET), e.getPlayer().getInventory(), e.getPlayer().getLocation());
                e.getPlayer().updateInventory();
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e){
        if(!plugin.getSettings().bucketsStackerEnabled)
            return;

        //Current Item - in slot, Cursor - holded item
        if(e.getCurrentItem() == null || (e.getCurrentItem().getType() != Material.WATER_BUCKET && e.getCurrentItem().getType() != Material.LAVA_BUCKET))
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
