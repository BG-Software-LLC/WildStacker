package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorGeneralMenu extends EditorMenu {

    private static EditorGeneralMenu NULL_HOLDER = new EditorGeneralMenu(null);

    private EditorGeneralMenu(Inventory inventory){
        super(inventory, "GENERAL_SLOT_", "generalEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = Bukkit.createInventory(NULL_HOLDER, 18, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "General Settings");
        EditorGeneralMenu editorGeneralMenu = new EditorGeneralMenu(inventory);

        buildInventory(editorGeneralMenu, generalFields, "");

        Executor.sync(() -> player.openInventory(editorGeneralMenu.getInventory()));
    }

}
