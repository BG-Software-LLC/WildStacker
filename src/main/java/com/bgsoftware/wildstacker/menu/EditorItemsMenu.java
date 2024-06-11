package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorItemsMenu extends EditorMenu {

    private final static EditorItemsMenu NULL_HOLDER = new EditorItemsMenu(null);

    private final static String[] sectionsPaths = new String[]{"merge-radius", "limits"};

    private EditorItemsMenu(Inventory inventory) {
        super(inventory, "ITEMS_SLOT_", "itemsEditor");
    }

    public static void open(Player player) {
        if (Bukkit.isPrimaryThread()) {
            Scheduler.runTaskAsync(() -> open(player));
            return;
        }

        Inventory inventory = buildInventory(NULL_HOLDER, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings", "items.", new String[0], sectionsPaths);
        EditorItemsMenu editorItemsMenu = new EditorItemsMenu(inventory);
        lastInventories.put(player.getUniqueId(), editorItemsMenu.editorIdentifier);

        Scheduler.runTask(player, () -> player.openInventory(editorItemsMenu.getInventory()));
    }

}
