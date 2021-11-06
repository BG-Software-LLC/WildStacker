package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorGeneralMenu extends EditorMenu {

    private final static EditorGeneralMenu NULL_HOLDER = new EditorGeneralMenu(null);

    private final static String[] ignorePaths = new String[]{
            "inspect-tool", "simulate-tool", "items", "entities", "spawners", "barrels", "buckets", "stews"
    };

    private EditorGeneralMenu(Inventory inventory) {
        super(inventory, "GENERAL_SLOT_", "generalEditor");
    }

    public static void open(Player player) {
        if (Bukkit.isPrimaryThread()) {
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = buildInventory(NULL_HOLDER, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "General Settings", "", ignorePaths, new String[0]);
        EditorGeneralMenu editorGeneralMenu = new EditorGeneralMenu(inventory);
        lastInventories.put(player.getUniqueId(), editorGeneralMenu.editorIdentifier);

        Executor.sync(() -> player.openInventory(editorGeneralMenu.getInventory()));
    }

}
