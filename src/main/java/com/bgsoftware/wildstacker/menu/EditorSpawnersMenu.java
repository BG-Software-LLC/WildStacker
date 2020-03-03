package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorSpawnersMenu extends EditorMenu {

    private static EditorSpawnersMenu NULL_HOLDER = new EditorSpawnersMenu(null);

    private EditorSpawnersMenu(Inventory inventory){
        super(inventory, "SPAWNERS_SLOT_", "spawnersEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = Bukkit.createInventory(NULL_HOLDER, 9 * 5, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings");
        EditorSpawnersMenu editorSpawnersMenu = new EditorSpawnersMenu(inventory);

        buildInventory(editorSpawnersMenu, spawnersFields, "spawners.");

        Executor.sync(() -> player.openInventory(editorSpawnersMenu.getInventory()));
    }

}
