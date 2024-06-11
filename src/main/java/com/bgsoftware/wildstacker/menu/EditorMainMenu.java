package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class EditorMainMenu extends EditorMenu {

    private static Inventory inventory = null;

    private EditorMainMenu() {
        super(inventory, "", "");
    }

    public static void open(Player player) {
        if (inventory == null)
            loadInventory();

        player.openInventory(inventory);
    }

    private static void loadInventory() {
        EditorMainMenu editorMainMenu = new EditorMainMenu();
        inventory = Bukkit.createInventory(editorMainMenu, 9 * 6, "" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker");

        ItemStack glassPane = new ItemBuilder(Materials.BLACK_STAINED_GLASS_PANE).withName("&6").build();

        for (int i = 0; i < 9; i++)
            inventory.setItem(i, glassPane);

        for (int i = 45; i < 54; i++)
            inventory.setItem(i, glassPane);

        inventory.setItem(9, glassPane);
        inventory.setItem(17, glassPane);
        inventory.setItem(18, glassPane);
        inventory.setItem(26, glassPane);
        inventory.setItem(27, glassPane);
        inventory.setItem(35, glassPane);
        inventory.setItem(36, glassPane);
        inventory.setItem(44, glassPane);

        inventory.setItem(19, new ItemBuilder(Materials.MAP)
                .withName("&6General Settings").withLore("&7Click to edit general settings.").build());

        inventory.setItem(21, new ItemBuilder(Material.GOLD_INGOT)
                .withName("&6Items Settings").withLore("&7Click to edit items settings.").build());

        inventory.setItem(23, new ItemBuilder(Material.ROTTEN_FLESH)
                .withName("&6Entities Settings").withLore("&7Click to edit entities settings.").build());

        inventory.setItem(25, new ItemBuilder(Material.BUCKET)
                .withName("&6Buckets Settings").withLore("&7Click to edit buckets settings.").build());

        inventory.setItem(29, new ItemBuilder(Materials.SPAWNER)
                .withName("&6Spawner Settings").withLore("&7Click to edit spawners settings.").build());

        inventory.setItem(31, new ItemBuilder(Materials.CAULDRON)
                .withName("&6Barrels Settings").withLore("&7Click to edit barrels settings.").build());

        inventory.setItem(33, new ItemBuilder(Materials.MUSHROOM_STEW)
                .withName("&6Stews Settings").withLore("&7Click to edit stews settings.").build());

        inventory.setItem(49, new ItemBuilder(Material.EMERALD)
                .withName("&aSave Changes").withLore("&7Click to save all changes.").build());
    }

    @Override
    public boolean onEditorClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        switch (e.getRawSlot()) {
            case 19:
                noResetClose.add(player.getUniqueId());
                EditorGeneralMenu.open(player);
                break;
            case 21:
                noResetClose.add(player.getUniqueId());
                EditorItemsMenu.open(player);
                break;
            case 23:
                noResetClose.add(player.getUniqueId());
                EditorEntitiesMenu.open(player);
                break;
            case 29:
                noResetClose.add(player.getUniqueId());
                EditorSpawnersMenu.open(player);
                break;
            case 31:
                if (ServerVersion.isEquals(ServerVersion.v1_7)) {
                    player.sendMessage(ChatColor.RED + "Barrels are based on armor-stands, and therefore - disabled in 1.7");
                } else {
                    noResetClose.add(player.getUniqueId());
                    EditorBarrelsMenu.open(player);
                }
                break;
            case 25:
                noResetClose.add(player.getUniqueId());
                EditorBucketsMenu.open(player);
                break;
            case 33:
                noResetClose.add(player.getUniqueId());
                EditorStewsMenu.open(player);
                break;
            case 49:
                Scheduler.runTaskAsync(() -> {
                    saveConfiguration();
                    player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker " + ChatColor.GRAY + "Saved configuration successfully.");
                });
                break;
        }

        return true;
    }

}
