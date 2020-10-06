package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
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

    private BarrelsPlaceMenu(Location location, ItemStack barrelItem){
        this.location = location;
        this.inventory = Bukkit.createInventory(this, 9 * 4, plugin.getSettings().barrelsPlaceInventoryTitle
                .replace("{0}", EntityUtils.getFormattedType(barrelItem.getType().name())));
        this.barrelItem = barrelItem;
    }

    @Override
    public void onButtonClick(InventoryClickEvent e) {
        if(!plugin.getSettings().barrelsRequiredPermission.isEmpty() &&
                !e.getWhoClicked().hasPermission(plugin.getSettings().barrelsRequiredPermission)){
            e.setCancelled(true);
            Locale.BARREL_NO_PERMISSION.send(e.getWhoClicked());
            Executor.sync(() -> e.getWhoClicked().closeInventory(), 1L);
            return;
        }

        if(!plugin.getSystemManager().isStackedBarrel(location)){
            e.setCancelled(true);
            Executor.sync(() -> e.getWhoClicked().closeInventory(), 1L);
            return;
        }

        ItemStack barrelItem;

        switch (e.getAction()){
            case HOTBAR_SWAP:
                barrelItem = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
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

        if(!this.barrelItem.isSimilar(barrelItem)) {
            e.setCancelled(true);
        }

        if(barrelItem != null) {
            Executor.sync(() -> {
                if (closeFlag) {
                    for(ItemStack itemStack : e.getWhoClicked().getInventory().getContents()){
                        if(barrelItem.equals(itemStack))
                            return;
                    }

                    ItemUtils.addItem(barrelItem, e.getWhoClicked().getInventory(), location);
                }
            }, 5L);
        }
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e) {
        closeFlag = true;

        StackedBarrel stackedBarrel = WStackedBarrel.of(location);

        int amount = 0;
        boolean dropAll = false;

        if(!plugin.getSettings().barrelsRequiredPermission.isEmpty() &&
                !e.getPlayer().hasPermission(plugin.getSettings().barrelsRequiredPermission)){
            Locale.BARREL_NO_PERMISSION.send(e.getPlayer());
            dropAll = true;
        }

        if(!plugin.getSystemManager().isStackedBarrel(location))
            dropAll = true;

        for(ItemStack itemStack : e.getInventory().getContents()){
            if(barrelItem.isSimilar(itemStack) && !dropAll)
                amount += itemStack.getAmount();
            else if(itemStack != null && itemStack.getType() != Material.AIR)
                ItemUtils.addItem(itemStack, e.getPlayer().getInventory(), stackedBarrel.getLocation());
        }

        if(amount != 0) {
            int limit = stackedBarrel.getStackLimit();
            int newStackAmount = stackedBarrel.getStackAmount() + amount;

            if(stackedBarrel.getStackAmount() + amount > limit){
                ItemStack toAdd = barrelItem.clone();
                toAdd.setAmount(stackedBarrel.getStackAmount() + amount - limit);
                ItemUtils.addItem(toAdd, e.getPlayer().getInventory(), stackedBarrel.getLocation());
                newStackAmount = limit;
            }

            BarrelPlaceInventoryEvent barrelPlaceInventoryEvent = new BarrelPlaceInventoryEvent((Player) e.getPlayer(), stackedBarrel, newStackAmount - stackedBarrel.getStackAmount());
            Bukkit.getPluginManager().callEvent(barrelPlaceInventoryEvent);

            if(!barrelPlaceInventoryEvent.isCancelled()) {
                stackedBarrel.setStackAmount(newStackAmount, true);
                Locale.BARREL_UPDATE.send(e.getPlayer(), ItemUtils.getFormattedType(barrelItem), stackedBarrel.getStackAmount());
            }
            else{
                ItemUtils.addItems(e.getInventory().getContents(), e.getPlayer().getInventory(), stackedBarrel.getLocation());
            }
        }

        ((WStackedBarrel) stackedBarrel).unlinkInventory(e.getInventory());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void open(Player player, StackedBarrel stackedBarrel){
        BarrelsPlaceMenu barrelsPlaceMenu = new BarrelsPlaceMenu(stackedBarrel.getLocation(), stackedBarrel.getBarrelItem(1));
        ((WStackedBarrel) stackedBarrel).linkInventory(barrelsPlaceMenu.inventory);
        player.openInventory(barrelsPlaceMenu.inventory);
    }

}
