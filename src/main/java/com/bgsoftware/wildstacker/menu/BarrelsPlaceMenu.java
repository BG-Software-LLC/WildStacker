package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class BarrelsPlaceMenu extends WildMenu {

    private final Inventory inventory;
    private final Location location;
    private final ItemStack barrelItem;

    private boolean closeFlag = false;

    private BarrelsPlaceMenu(Location location, ItemStack barrelItem) {
        super("barrelsPlace");
        this.location = location;
        this.inventory = Bukkit.createInventory(this, 9 * 4, plugin.getSettings().barrelsPlaceInventoryTitle
                .replace("{0}", EntityUtils.getFormattedType(barrelItem.getType().name())));
        this.barrelItem = barrelItem;
        this.cancelOnClick = false;
    }

    public static void open(Player player, StackedBarrel stackedBarrel) {
        BarrelsPlaceMenu barrelsPlaceMenu = new BarrelsPlaceMenu(stackedBarrel.getLocation(), stackedBarrel.getBarrelItem(1));
        ((WStackedBarrel) stackedBarrel).linkInventory(barrelsPlaceMenu.inventory);
        player.openInventory(barrelsPlaceMenu.inventory);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if (!plugin.getSettings().barrelsRequiredPermission.isEmpty() &&
                !e.getWhoClicked().hasPermission(plugin.getSettings().barrelsRequiredPermission)) {
            e.setCancelled(true);
            Locale.BARREL_NO_PERMISSION.send(e.getWhoClicked());
            Scheduler.runTask(e.getWhoClicked(), () -> e.getWhoClicked().closeInventory(), 1L);
            return;
        }

        if (!plugin.getSystemManager().isStackedBarrel(location)) {
            e.setCancelled(true);
            Scheduler.runTask(e.getWhoClicked(), () -> e.getWhoClicked().closeInventory(), 1L);
            return;
        }

        ItemStack barrelItem;

        switch (e.getAction()) {
            case HOTBAR_SWAP:
                barrelItem = e.getHotbarButton() < 0 ? null : e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
                break;
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
                barrelItem = e.getCursor();
                break;
            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
            case MOVE_TO_OTHER_INVENTORY:
                barrelItem = e.getCurrentItem();
                break;
            default:
                return;
        }

        if (barrelItem == null)
            return;

        if (!isSimilar(barrelItem)) {
            e.setCancelled(true);
        }

        Scheduler.runTask(e.getWhoClicked(), () -> {
            if (closeFlag) {
                for (ItemStack itemStack : e.getWhoClicked().getInventory().getContents()) {
                    if (barrelItem.equals(itemStack))
                        return;
                }

                ItemUtils.addItem(barrelItem, e.getWhoClicked().getInventory(), location);
            }
        }, 5L);
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e) {
        closeFlag = true;

        StackedBarrel stackedBarrel = WStackedBarrel.of(location);

        int amount = 0;
        boolean dropAll = false;

        if (!plugin.getSettings().barrelsRequiredPermission.isEmpty() &&
                !e.getPlayer().hasPermission(plugin.getSettings().barrelsRequiredPermission)) {
            Locale.BARREL_NO_PERMISSION.send(e.getPlayer());
            dropAll = true;
        }

        if (!plugin.getSystemManager().isStackedBarrel(location))
            dropAll = true;

        for (ItemStack itemStack : e.getInventory().getContents()) {
            if (isSimilar(itemStack) && !dropAll) {
                amount += (itemStack.getAmount() * ItemUtils.getSpawnerItemAmount(itemStack));
            } else if (itemStack != null && itemStack.getType() != Material.AIR) {
                ItemUtils.addItem(itemStack, e.getPlayer().getInventory(), stackedBarrel.getLocation());
            }
        }

        if (amount != 0) {
            int limit = stackedBarrel.getStackLimit();
            int currentStackAmount = stackedBarrel.getStackAmount();
            int increaseStackAmount = Math.min(amount, limit - currentStackAmount);

            if (increaseStackAmount != amount) {
                ItemStack toAdd = barrelItem.clone();
                toAdd.setAmount(amount - increaseStackAmount);
                ItemUtils.addItem(toAdd, e.getPlayer().getInventory(), stackedBarrel.getLocation());
            }

            if (EventsCaller.callBarrelPlaceInventoryEvent((Player) e.getPlayer(), stackedBarrel, increaseStackAmount)) {
                int newStackAmount = stackedBarrel.increaseStackAmount(increaseStackAmount, true);
                Locale.BARREL_UPDATE.send(e.getPlayer(), ItemUtils.getFormattedType(barrelItem), newStackAmount);
            } else {
                ItemUtils.addItems(e.getInventory().getContents(), e.getPlayer().getInventory(), stackedBarrel.getLocation());
            }
        }

        closeFlag = false;

        ((WStackedBarrel) stackedBarrel).unlinkInventory(e.getInventory());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private boolean isSimilar(ItemStack barrelItem) {
        return barrelItem != null && plugin.getProviders().canCreateBarrel(barrelItem) &&
                this.barrelItem.getType() == barrelItem.getType() &&
                (!ServerVersion.isLegacy() || this.barrelItem.getDurability() == barrelItem.getDurability());
    }

}
