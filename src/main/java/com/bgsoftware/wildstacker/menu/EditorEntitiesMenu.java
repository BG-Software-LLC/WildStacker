package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorEntitiesMenu extends EditorMenu {

    private static EditorEntitiesMenu NULL_HOLDER = new EditorEntitiesMenu(null);

    private EditorEntitiesMenu(Inventory inventory){
        super(inventory, "ENTITIES_SLOT_", "entitiesEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = Bukkit.createInventory(NULL_HOLDER, 9 * 4, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings");
        EditorEntitiesMenu editorEntitiesMenu = new EditorEntitiesMenu(inventory);

        buildInventory(editorEntitiesMenu, entitiesFields, "entities.");

        Executor.sync(() -> player.openInventory(editorEntitiesMenu.getInventory()));
    }

}
