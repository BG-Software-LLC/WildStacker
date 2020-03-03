package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorItemsMenu extends EditorMenu {

    private static EditorItemsMenu NULL_HOLDER = new EditorItemsMenu(null);

    private EditorItemsMenu(Inventory inventory){
        super(inventory, "ITEMS_SLOT_", "itemsEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = Bukkit.createInventory(NULL_HOLDER, 27, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings");
        EditorItemsMenu editorItemsMenu = new EditorItemsMenu(inventory);

        buildInventory(editorItemsMenu, itemsFields, "items.");

        Executor.sync(() -> player.openInventory(editorItemsMenu.getInventory()));
    }

}
