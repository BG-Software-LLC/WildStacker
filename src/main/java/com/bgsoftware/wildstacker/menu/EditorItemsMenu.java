package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorItemsMenu extends EditorMenu {

    private final static EditorItemsMenu NULL_HOLDER = new EditorItemsMenu(null);

    private final static String[] sectionsPaths = new String[] {
            "limits"
    };

    private EditorItemsMenu(Inventory inventory){
        super(inventory, "ITEMS_SLOT_", "itemsEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = buildInventory(NULL_HOLDER, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings", "items.", new String[0], sectionsPaths);
        EditorItemsMenu editorItemsMenu = new EditorItemsMenu(inventory);
        lastInventories.put(player.getUniqueId(), editorItemsMenu.editorIdentifier);

        Executor.sync(() -> player.openInventory(editorItemsMenu.getInventory()));
    }

}
