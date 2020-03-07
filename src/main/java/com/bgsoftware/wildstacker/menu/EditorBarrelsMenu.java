package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorBarrelsMenu extends EditorMenu {

    private final static EditorBarrelsMenu NULL_HOLDER = new EditorBarrelsMenu(null);

    private final static String[] sectionsPaths = new String[] {
            "limits"
    };

    private EditorBarrelsMenu(Inventory inventory){
        super(inventory, "BARRELS_SLOT_", "barrelsEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = buildInventory(NULL_HOLDER, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings", "barrels.", new String[0], sectionsPaths);
        EditorBarrelsMenu editorBarrelsMenu = new EditorBarrelsMenu(inventory);
        lastInventories.put(player.getUniqueId(), editorBarrelsMenu.editorIdentifier);

        Executor.sync(() -> player.openInventory(editorBarrelsMenu.getInventory()));
    }

}
