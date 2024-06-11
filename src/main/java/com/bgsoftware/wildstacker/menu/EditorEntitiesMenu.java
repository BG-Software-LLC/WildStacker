package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorEntitiesMenu extends EditorMenu {

    private final static EditorEntitiesMenu NULL_HOLDER = new EditorEntitiesMenu(null);

    private final static String[] sectionsPaths = new String[]{"merge-radius", "limits", "minimum-required",
            "stack-checks", "stack-split", "default-unstack"};

    private EditorEntitiesMenu(Inventory inventory) {
        super(inventory, "ENTITIES_SLOT_", "entitiesEditor");
    }

    public static void open(Player player) {
        if (Bukkit.isPrimaryThread()) {
            Scheduler.runTaskAsync(() -> open(player));
            return;
        }

        Inventory inventory = buildInventory(NULL_HOLDER, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings", "entities.", new String[0], sectionsPaths);
        EditorEntitiesMenu editorEntitiesMenu = new EditorEntitiesMenu(inventory);
        lastInventories.put(player.getUniqueId(), editorEntitiesMenu.editorIdentifier);

        Scheduler.runTask(player, () -> player.openInventory(editorEntitiesMenu.getInventory()));
    }

}
