package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.menu.ConfigEditorMenu;
import com.bgsoftware.wildstacker.menu.WildMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class MenusListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuClick(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory != null && topInventory.getHolder() instanceof WildMenu) {
            WildMenu wildMenu = (WildMenu) topInventory.getHolder();
            wildMenu.onButtonClick(e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuClose(InventoryCloseEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory != null && topInventory.getHolder() instanceof WildMenu) {
            WildMenu wildMenu = (WildMenu) topInventory.getHolder();
            wildMenu.onMenuClose(e);
        }
    }

    /**
     * Listening to AsyncPlayerChatEvent for the editor menus.
     */

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Consumer<String> listener = ConfigEditorMenu.getChatListener(e.getPlayer());
        if(listener == null)
            return;

        e.setCancelled(true);
        listener.accept(e.getMessage());
    }

}
