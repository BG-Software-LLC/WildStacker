package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorStewsMenu extends EditorMenu {

    private final static EditorStewsMenu NULL_HOLDER = new EditorStewsMenu(null);

    private EditorStewsMenu(Inventory inventory){
        super(inventory, "STEWS_SLOT_", "stewsEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = buildInventory(NULL_HOLDER, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Stews Settings", "stews.", new String[0], new String[0]);
        EditorStewsMenu editorStewsMenu = new EditorStewsMenu(inventory);

        Executor.sync(() -> player.openInventory(editorStewsMenu.getInventory()));
    }

}
