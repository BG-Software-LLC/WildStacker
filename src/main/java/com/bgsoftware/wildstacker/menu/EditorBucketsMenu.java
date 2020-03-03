package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorBucketsMenu extends EditorMenu {

    private final static EditorBucketsMenu NULL_HOLDER = new EditorBucketsMenu(null);

    private EditorBucketsMenu(Inventory inventory){
        super(inventory, "BUCKETS_SLOT_", "bucketsEditor");
    }

    public static void open(Player player){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(player));
            return;
        }

        Inventory inventory = buildInventory(NULL_HOLDER, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Buckets Settings", "buckets.", new String[0], new String[0]);
        EditorBucketsMenu editorBucketsMenu = new EditorBucketsMenu(inventory);

        Executor.sync(() -> player.openInventory(editorBucketsMenu.getInventory()));
    }

}
