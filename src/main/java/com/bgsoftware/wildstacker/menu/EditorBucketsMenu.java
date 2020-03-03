package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorBucketsMenu extends EditorMenu {

    private static EditorBucketsMenu NULL_HOLDER = new EditorBucketsMenu(null);

    private EditorBucketsMenu(Inventory inventory){
        super(inventory, "BUCKETS_SLOT_", "bucketsEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = Bukkit.createInventory(NULL_HOLDER, 9, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Buckets Settings");
        EditorBucketsMenu editorBucketsMenu = new EditorBucketsMenu(inventory);

        buildInventory(editorBucketsMenu, bucketsFields, "buckets.");

        Executor.sync(() -> player.openInventory(editorBucketsMenu.getInventory()));
    }

}
