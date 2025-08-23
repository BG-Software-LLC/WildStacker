package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.errors.ManagerLoadException;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class ConfigEditorMenu extends WildMenu {

    private static final File CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
    private static final Map<UUID, Consumer<String>> PLAYER_CHAT_LISTENERS = new HashMap<>();

    private static final Set<String> MAIN_SECTION_IGNORED_PATHS = new HashSet<>(Arrays.asList(
            "items", "entities", "buckets", "spawners", "barrels", "stews"));

    private final CommentedConfiguration config = new CommentedConfiguration();

    private ConfigEditorMenu() {
        super("ConfigEditorMenu");
    }

    public static void open(Player player) {
        ConfigEditorMenu menu = new ConfigEditorMenu();
        menu.openMenu(player);
    }

    static void listenChat(Player player, Consumer<String> onMessage) {
        PLAYER_CHAT_LISTENERS.put(player.getUniqueId(), onMessage);
    }

    @Nullable
    public static Consumer<String> getChatListener(Player player) {
        return PLAYER_CHAT_LISTENERS.remove(player.getUniqueId());
    }

    @Override
    protected Inventory buildInventory() {
        Inventory inventory = Bukkit.createInventory(this, 9 * 6, "" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker");

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

        reloadConfig();

        return inventory;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        e.setCancelled(true);

        ConfigSectionEditorMenu nextMenu;

        switch (e.getRawSlot()) {
            case 19:
                nextMenu = new ConfigSectionEditorMenu(this.config.getConfigurationSection(""), MAIN_SECTION_IGNORED_PATHS);
                break;
            case 21:
                nextMenu = new ConfigSectionEditorMenu(this.config.getConfigurationSection("items"));
                break;
            case 23:
                nextMenu = new ConfigSectionEditorMenu(this.config.getConfigurationSection("entities"));
                break;
            case 25:
                nextMenu = new ConfigSectionEditorMenu(this.config.getConfigurationSection("buckets"));
                break;
            case 29:
                nextMenu = new ConfigSectionEditorMenu(this.config.getConfigurationSection("spawners"));
                break;
            case 31:
                if (ServerVersion.isEquals(ServerVersion.v1_7)) {
                    e.getWhoClicked().sendMessage(ChatColor.RED + "Barrels are based on armor-stands, and therefore - disabled in 1.7");
                    return;
                } else {
                    nextMenu = new ConfigSectionEditorMenu(this.config.getConfigurationSection("barrels"));
                }
                break;
            case 33:
                nextMenu = new ConfigSectionEditorMenu(this.config.getConfigurationSection("stews"));
                break;
            case 49:
                Executor.async(() -> {
                    saveConfig();
                    e.getWhoClicked().sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker " + ChatColor.GRAY + "Saved configuration successfully.");
                });
                return;
            default:
                return;
        }

        nextMenu.setPreviousMenu(this);
        nextMenu.openMenu((Player) e.getWhoClicked());
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e) {
        // Do nothing
    }

    private void reloadConfig() {
        try {
            config.load(CONFIG_FILE);
        } catch (Exception error) {
            WildStackerPlugin.log("An unexpected error occurred while reloading config file:");
            error.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            config.save(CONFIG_FILE);
            try {
                plugin.getSettings().loadData();
            } catch (ManagerLoadException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception error) {
            WildStackerPlugin.log("An unexpected error occurred while saving config file:");
            error.printStackTrace();
        }
    }

}
