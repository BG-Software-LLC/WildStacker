package com.bgsoftware.wildstacker.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ShulkerOversizedPatch implements Listener {

    @EventHandler
    public void onShulkerClick(InventoryClickEvent e){
        Inventory shulkerBox = e.getView().getTopInventory();

        if(shulkerBox == null || !shulkerBox.getType().name().equals("SHULKER_BOX"))
            return;

        ItemStack overSizeItem = null;

        switch (e.getClick()){
            case LEFT:
                if(e.getClickedInventory() == shulkerBox)
                    overSizeItem = e.getCursor();
                break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                if(e.getClickedInventory() != shulkerBox)
                    overSizeItem = e.getCurrentItem();
                break;
            case NUMBER_KEY:
                if(e.getClickedInventory() == shulkerBox)
                    overSizeItem = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                break;
        }

        if(overSizeItem != null && overSizeItem.getType() != Material.AIR && overSizeItem.getAmount() > overSizeItem.getMaxStackSize())
            e.setCancelled(true);
    }

}
