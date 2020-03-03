package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public abstract class WildMenu implements InventoryHolder {

    protected static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public abstract void onButtonClick(InventoryClickEvent e);

    public void onMenuClose(InventoryCloseEvent e){

    }

}
