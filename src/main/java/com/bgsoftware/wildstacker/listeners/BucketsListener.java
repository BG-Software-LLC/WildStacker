package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
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
import org.bukkit.inventory.meta.ItemMeta;

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
        if(plugin.getSettings().bucketsStackerEnabled) {
            e.setCancelled(true);

            PlayerInventory inventory = e.getPlayer().getInventory();
            int heldItemSlot = ItemUtils.getHeldItemSlot(inventory, e.getBucket());
            ItemStack itemInHand = inventory.getItem(heldItemSlot);
            ItemStack itemToGive = itemInHand.clone();
            itemToGive.setAmount(itemToGive.getAmount() - 1);

            Block fluidBlock = e.getBlockClicked().getRelative(e.getBlockFace());

            if(itemInHand.getType().name().contains("LAVA")) {
                fluidBlock.setType(Material.LAVA);
            }
            else{
                if(e.getBlockClicked().getWorld().getEnvironment() != World.Environment.NETHER) {
                    if(!plugin.getNMSAdapter().attemptToWaterLog(fluidBlock))
                        fluidBlock.setType(Material.WATER);
                }

                try{
                    String entityType = itemInHand.getType().name().replace("_BUCKET", "");

                    int amount = ItemUtils.getSpawnerItemAmount(itemInHand);
                    ItemMeta itemMeta = itemInHand.getItemMeta();
                    String fishName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : "";

                    StackedEntity stackedEntity = WStackedEntity.of(plugin.getSystemManager().spawnEntityWithoutStacking(
                            fluidBlock.getLocation().add(0.5, 0, 0.5), EntityType.valueOf(entityType).getEntityClass()));

                    if(!fishName.isEmpty()){
                        stackedEntity.setCustomName(fishName);
                        ((WStackedEntity) stackedEntity).setNameTag(true);
                    }

                    stackedEntity.setStackAmount(amount, true);
                }catch (Exception ignored){}
            }

            if(itemToGive.getAmount() <= 0){
                inventory.setItem(heldItemSlot, e.getItemStack().clone());
            }
            else {
                inventory.setItem(heldItemSlot, itemToGive);
                inventory.addItem(e.getItemStack().clone());
            }
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