package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class EditorBucketsMenu extends EditorMenu {

    private final static EditorBucketsMenu NULL_HOLDER = new EditorBucketsMenu(null);

    private EditorBucketsMenu(Inventory inventory) {
        super(inventory, "BUCKETS_SLOT_", "bucketsEditor");
    }

    public static void open(Player player) {
        if (Bukkit.isPrimaryThread()) {
            Scheduler.runTaskAsync(() -> open(player));
            return;
        }

        Inventory inventory = buildInventory(NULL_HOLDER, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Buckets Settings", "buckets.", new String[0], new String[0]);
        EditorBucketsMenu editorBucketsMenu = new EditorBucketsMenu(inventory);
        lastInventories.put(player.getUniqueId(), editorBucketsMenu.editorIdentifier);

        Scheduler.runTask(player, () -> player.openInventory(editorBucketsMenu.getInventory()));
    }

}
