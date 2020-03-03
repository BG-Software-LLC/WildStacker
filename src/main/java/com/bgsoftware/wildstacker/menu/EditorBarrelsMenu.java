package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorBarrelsMenu extends EditorMenu {

    private static EditorBarrelsMenu NULL_HOLDER = new EditorBarrelsMenu(null);

    private EditorBarrelsMenu(Inventory inventory){
        super(inventory, "BARRELS_SLOT_", "barrelsEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = Bukkit.createInventory(NULL_HOLDER, 9 * 2, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings");
        EditorBarrelsMenu editorBarrelsMenu = new EditorBarrelsMenu(inventory);

        buildInventory(editorBarrelsMenu, barrelsFields, "barrels.");

        Executor.sync(() -> player.openInventory(editorBarrelsMenu.getInventory()));
    }

}
