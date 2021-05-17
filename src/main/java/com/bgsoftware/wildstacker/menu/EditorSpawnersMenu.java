package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorSpawnersMenu extends EditorMenu {

    private final static EditorSpawnersMenu NULL_HOLDER = new EditorSpawnersMenu(null);

    private final static String[] ignorePaths = new String[] { "merge-radius", "spawners.break-menu",
            "spawners.break-charge", "spawners.place-charge", "spawners.spawner-upgrades",
            "spawners.spawners-override.spawn-conditions" };

    private final static String[] sectionsPaths = new String[] {
            "limits"
    };

    private EditorSpawnersMenu(Inventory inventory){
        super(inventory, "SPAWNERS_SLOT_", "spawnersEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = buildInventory(NULL_HOLDER, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings", "spawners.", ignorePaths, sectionsPaths);
        EditorSpawnersMenu editorSpawnersMenu = new EditorSpawnersMenu(inventory);
        lastInventories.put(player.getUniqueId(), editorSpawnersMenu.editorIdentifier);

        Executor.sync(() -> player.openInventory(editorSpawnersMenu.getInventory()));
    }

}
