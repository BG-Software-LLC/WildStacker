package com.bgsoftware.wildstacker.menu;

import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.names.CustomNames;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class ConfigSectionEditorMenu extends WildMenu {

    private static final ItemStack[] EMPTY_ITEMS_ARRAY = new ItemStack[0];

    private static final Set<String> DATA_SECTIONS = new HashSet<>(Arrays.asList(
            "merge-radius", "limits", "minimum-required", "default-unstack"
    ));
    private static final Set<String> GLOBAL_IGNORED_PATHS = new HashSet<>(Arrays.asList(
            "break-charge", "place-charge", "spawners-override", "spawner-upgrades"
    ));

    private final List<String> pathSlots = new LinkedList<>();
    private final ConfigurationSection section;

    private final Set<String> ignoredPaths;

    @Nullable
    private WildMenu previousMenu;
    private boolean nextMenuMove = false;

    public ConfigSectionEditorMenu(ConfigurationSection section) {
        this(section, null);
    }

    public ConfigSectionEditorMenu(ConfigurationSection section, @Nullable Set<String> ignoredPaths) {
        super("ConfigSectionEditorMenu_" + section.getName());
        this.section = section;
        this.ignoredPaths = new HashSet<>(GLOBAL_IGNORED_PATHS);
        if (ignoredPaths != null)
            this.ignoredPaths.addAll(ignoredPaths);
    }

    public void setPreviousMenu(@Nullable WildMenu previousMenu) {
        this.previousMenu = previousMenu;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        try {
            String sectionPath = pathSlots.get(e.getRawSlot());

            if (sectionPath == null)
                return;

            if (this.section.isConfigurationSection(sectionPath) && !DATA_SECTIONS.contains(sectionPath)) {
                // Open a new ConfigSectionEditorMenu for the player
                ConfigSectionEditorMenu sectionEditor = new ConfigSectionEditorMenu(
                        this.section.getConfigurationSection(sectionPath));
                sectionEditor.previousMenu = this;
                this.inventory = null; // Refresh menu
                this.nextMenuMove = true;
                sectionEditor.openMenu(player);
            } else if (this.section.isBoolean(sectionPath)) {
                this.section.set(sectionPath, !this.section.getBoolean(sectionPath));
                this.inventory = null; // Refresh menu
                this.nextMenuMove = true;
                openMenu(player);
            } else {
                ConfigEditorMenu.listenChat(player, message -> onPlayerChat(player, message, sectionPath));

                this.nextMenuMove = true;
                player.closeInventory();

                player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

                if (this.section.isList(sectionPath)) {
                    player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
                } else if (this.section.isConfigurationSection(sectionPath)) {
                    player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " The format to be used is 'KEY: VALUE'");
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        if (this.nextMenuMove) {
            this.nextMenuMove = false;
            return;
        }

        if (this.previousMenu != null) {
            Executor.sync(() -> this.previousMenu.openMenu(player), 1L);
        }
    }

    private void onPlayerChat(Player player, Object message, String path) {
        if (!message.toString().equalsIgnoreCase("-cancel")) {
            if (this.section.isList(path)) {
                List<String> list = this.section.getStringList(path);

                if (list.contains(message.toString())) {
                    list.remove(message.toString());
                    player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Removed the value " + message + " from " + path);
                } else {
                    list.add(message.toString());
                    player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker" + ChatColor.GRAY + " Added the value " + message + " to " + path);
                }

                this.section.set(path, list);
            } else if (this.section.isConfigurationSection(path)) {
                ConfigurationSection section = this.section.getConfigurationSection(path);
                String[] messageSections = message.toString().split(":");

                if (messageSections.length != 2) {
                    player.sendMessage(ChatColor.RED + "Must be in a 'KEY: VALUE' format");
                } else {
                    String key = messageSections[0].trim();
                    Object value = messageSections[1].trim();

                    try {
                        // We try to convert the value to int if possible
                        value = Integer.valueOf((String) value);
                    } catch (NumberFormatException ignored) {
                    }

                    section.set(key, value);
                }
            } else {
                boolean valid = true;
                if (this.section.isInt(path)) {
                    try {
                        message = Integer.valueOf(message.toString());
                    } catch (IllegalArgumentException ex) {
                        player.sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                } else if (this.section.isDouble(path)) {
                    try {
                        message = Double.valueOf(message.toString());
                    } catch (IllegalArgumentException ex) {
                        player.sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                if (valid) {
                    this.section.set(path, message);
                }
            }
        }

        this.inventory = null; // Refresh menu
        this.nextMenuMove = true;
        openMenu(player);
    }

    @Override
    protected Inventory buildInventory() {
        List<ItemStack> contents = new ArrayList<>();

        this.pathSlots.clear();

        for (String path : this.section.getKeys(false)) {
            if (this.ignoredPaths.contains(path))
                continue;

            ItemBuilder itemBuilder = new ItemBuilder(Materials.CLOCK.toBukkitItem()).withName("&6" +
                    CustomNames.format(path.replace("-", "_")
                            .replace(".", "_").replace(" ", "_")));

            if (section.isBoolean(path))
                itemBuilder.withLore("&7Value: " + section.getBoolean(path));
            else if (section.isInt(path))
                itemBuilder.withLore("&7Value: " + section.getInt(path));
            else if (section.isDouble(path))
                itemBuilder.withLore("&7Value: " + section.getDouble(path));
            else if (section.isString(path))
                itemBuilder.withLore("&7Value: " + section.getString(path));
            else if (section.isList(path))
                itemBuilder.withLore("&7Value:", section.getStringList(path));
            else if (section.isConfigurationSection(path)) {
                if (DATA_SECTIONS.contains(path)) {
                    List<String> data = new LinkedList<>();
                    for (String key : section.getConfigurationSection(path).getKeys(false)) {
                        data.add(key + ": " + section.get(path + "." + key));
                    }
                    itemBuilder.withLore("&7Value:", data);
                } else {
                    itemBuilder.withLore("&7Click to edit section.");
                }
            }

            contents.add(itemBuilder.build());
            this.pathSlots.add(path);
        }

        int size = ((int) Math.ceil(contents.size() / 9D)) * 9;
        Inventory inventory = Bukkit.createInventory(this, size,
                "" + ChatColor.GOLD + ChatColor.BOLD + "Section: " + this.section.getName());

        inventory.setContents(contents.toArray(EMPTY_ITEMS_ARRAY));

        return inventory;
    }

}
