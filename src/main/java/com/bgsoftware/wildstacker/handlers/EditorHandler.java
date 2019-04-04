package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.config.ConfigComments;
import com.bgsoftware.wildstacker.utils.ItemBuilder;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public final class EditorHandler {

    private WildStackerPlugin plugin;

    private ItemStack[] settingsEditor;

    public CommentedConfiguration config;

    public EditorHandler(WildStackerPlugin plugin){
        this.plugin = plugin;
        this.config = new CommentedConfiguration(ConfigComments.class);
        this.config.load(new File(plugin.getDataFolder(), "config.yml"));
        loadSettingsEditor();
    }

    public void saveConfiguration(){
        config.save(new File(plugin.getDataFolder(), "config.yml"));
        SettingsHandler.reload();
    }

    public void reloadConfiguration(){
        config.load(new File(plugin.getDataFolder(), "config.yml"));
    }

    public Inventory getSettingsEditor(){
        Inventory editor = Bukkit.createInventory(null, settingsEditor.length, "" + ChatColor.GOLD + ChatColor.BOLD + "WildStacker");
        editor.setContents(settingsEditor);
        return editor;
    }

    public Inventory getGeneralEditor(){
        Inventory editor = Bukkit.createInventory(null, 9, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "General Settings");

        editor.setItem(0, new ItemBuilder(Materials.CLOCK)
                .withName("&6Save Interval").withLore("&7Value: " + config.getLong("save-interval")).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Give-Item Name").withLore("&7Value: " + config.getString("give-item-name")).build());

        return editor;
    }

    public Inventory getItemsEditor(){
        Inventory editor = Bukkit.createInventory(null, 18, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings");

        editor.setItem(0, new ItemBuilder(Materials.CLOCK)
                .withName("&6Enabled").withLore("&7Value: " + config.getBoolean("items.enabled")).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Merge Radius").withLore("&7Value: " + config.getInt("items.merge-radius")).build());

        editor.setItem(2, new ItemBuilder(Materials.CLOCK)
                .withName("&6Custom Name").withLore("&7Value: " + config.getString("items.custom-name")).build());

        editor.setItem(3, new ItemBuilder(Materials.CLOCK)
                .withName("&6Blacklist").withLore("&7Value:", config.getStringList("items.blacklist")).build());

        editor.setItem(4, new ItemBuilder(Materials.CLOCK)
                .withName("&6Limits").withLore("&7Value:", config.getConfigurationSection("items.limits")).build());

        editor.setItem(5, new ItemBuilder(Materials.CLOCK)
                .withName("&6Disabled Worlds").withLore("&7Value:", config.getStringList("items.disabled-worlds")).build());

        editor.setItem(6, new ItemBuilder(Materials.CLOCK)
                .withName("&6Fix Stack").withLore("&7Value: " + config.getBoolean("items.fix-stack")).build());

        editor.setItem(7, new ItemBuilder(Materials.CLOCK)
                .withName("&6Item Display").withLore("&7Value: " + config.getBoolean("items.item-display")).build());

        editor.setItem(8, new ItemBuilder(Materials.CLOCK)
                .withName("&6Buckets Stacker Enabled").withLore("&7Value: " + config.getBoolean("items.buckets-stacker.enabled")).build());

        editor.setItem(9, new ItemBuilder(Materials.CLOCK)
                .withName("&6Buckets Name Blacklist").withLore("&7Value:", config.getStringList("items.buckets-stacker.name-blacklist")).build());

        editor.setItem(10, new ItemBuilder(Materials.CLOCK)
                .withName("&6Items Kill All").withLore("&7Value: " + config.getBoolean("items.kill-all")).build());

        return editor;
    }

    public Inventory getEntitiesEditor(){
        Inventory editor = Bukkit.createInventory(null, 9 * 3, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings");

        editor.setItem(0, new ItemBuilder(Materials.CLOCK)
                .withName("&6Enabled").withLore("&7Value: " + config.getBoolean("entities.enabled")).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Merge Radius").withLore("&7Value: " + config.getInt("entities.merge-radius")).build());

        editor.setItem(2, new ItemBuilder(Materials.CLOCK)
                .withName("&6Custom Name").withLore("&7Value: " + config.getString("entities.custom-name")).build());

        editor.setItem(3, new ItemBuilder(Materials.CLOCK)
                .withName("&6Blacklist").withLore("&7Value:", config.getStringList("entities.blacklist")).build());

        editor.setItem(4, new ItemBuilder(Materials.CLOCK)
                .withName("&6Limits").withLore("&7Value:", config.getConfigurationSection("entities.limits")).build());

        editor.setItem(5, new ItemBuilder(Materials.CLOCK)
                .withName("&6Minimum Limits").withLore("&7Value:", config.getConfigurationSection("entities.minimum-limits")).build());

        editor.setItem(6, new ItemBuilder(Materials.CLOCK)
                .withName("&6Disabled Worlds").withLore("&7Value:", config.getStringList("entities.disabled-worlds")).build());

        editor.setItem(7, new ItemBuilder(Materials.CLOCK)
                .withName("&6Spawn Blacklist").withLore("&7Value:", config.getStringList("entities.spawn-blacklist")).build());

        editor.setItem(8, new ItemBuilder(Materials.CLOCK)
                .withName("&6Name Blacklist").withLore("&7Value:", config.getStringList("entities.name-blacklist")).build());

        editor.setItem(9, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Interval").withLore("&7Value: " + config.getInt("entities.stack-interval")).build());

        editor.setItem(10, new ItemBuilder(Materials.CLOCK)
                .withName("&6Kill-All Interval").withLore("&7Value: " + config.getInt("entities.kill-all.interval")).build());

        editor.setItem(11, new ItemBuilder(Materials.CLOCK)
                .withName("&6Kill-All ClearLagg").withLore("&7Value: " + config.getBoolean("entities.kill-all.clear-lagg")).build());

        editor.setItem(12, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Checks").withLore("&7Value &c(Do not remove sections)&7:", config.getConfigurationSection("entities.stack-checks")).build());

        editor.setItem(13, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Split").withLore("&7Value &c(Do not remove sections)&7:", config.getConfigurationSection("entities.stack-split")).build());

        editor.setItem(14, new ItemBuilder(Materials.CLOCK)
                .withName("&6Linked Entities Enabled").withLore("&7Value: " + config.getBoolean("entities.linked-entities.enabled")).build());

        editor.setItem(15, new ItemBuilder(Materials.CLOCK)
                .withName("&6Linked Entities Max Distance").withLore("&7Value: " + config.getInt("entities.linked-entities.max-distance")).build());

        editor.setItem(16, new ItemBuilder(Materials.CLOCK)
                .withName("&6Instant Kill").withLore("&7Value:", config.getStringList("entities.instant-kill")).build());

        editor.setItem(17, new ItemBuilder(Materials.CLOCK)
                .withName("&6Nerfed Spawning").withLore("&7Value:", config.getStringList("entities.nerfed-spawning")).build());

        editor.setItem(18, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Down Enabled").withLore("&7Value: " + config.getBoolean("entities.stack-down.enabled")).build());

        editor.setItem(19, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Down Types").withLore("&7Value:", config.getStringList("entities.stack-down.stack-down-types")).build());

        editor.setItem(20, new ItemBuilder(Materials.CLOCK)
                .withName("&6Keep Fire").withLore("&7Value: " + config.getBoolean("entities.keep-fire")).build());

        editor.setItem(21, new ItemBuilder(Materials.CLOCK)
                .withName("&6MythicMobs Stack").withLore("&7Value: " + config.getBoolean("entities.mythic-mobs-stack")).build());

        editor.setItem(22, new ItemBuilder(Materials.CLOCK)
                .withName("&6Blazes Always Drop").withLore("&7Value: " + config.getBoolean("entities.blazes-always-drop")).build());

        editor.setItem(23, new ItemBuilder(Materials.CLOCK)
                .withName("&6Keep Lowest Health").withLore("&7Value: " + config.getBoolean("entities.keep-lowest-health")).build());

        editor.setItem(24, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack After Breed").withLore("&7Value: " + config.getBoolean("entities.stack-after-breed")).build());

        editor.setItem(25, new ItemBuilder(Materials.CLOCK)
                .withName("&6Hide Names").withLore("&7Value: " + config.getBoolean("entities.hide-names")).build());

        return editor;
    }

    public Inventory getSpawnersEditor(){
        Inventory editor = Bukkit.createInventory(null, 9 * 4, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings");

        editor.setItem(0, new ItemBuilder(Materials.CLOCK)
                .withName("&6Enabled").withLore("&7Value: " + config.getBoolean("spawners.enabled")).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Merge Radius").withLore("&7Value: " + config.getInt("spawners.merge-radius")).build());

        editor.setItem(2, new ItemBuilder(Materials.CLOCK)
                .withName("&6Custom Name").withLore("&7Value: " + config.getString("spawners.custom-name")).build());

        editor.setItem(3, new ItemBuilder(Materials.CLOCK)
                .withName("&6Blacklist").withLore("&7Value:", config.getStringList("spawners.blacklist")).build());

        editor.setItem(4, new ItemBuilder(Materials.CLOCK)
                .withName("&6Limits").withLore("&7Value:", config.getConfigurationSection("spawners.limits")).build());

        editor.setItem(5, new ItemBuilder(Materials.CLOCK)
                .withName("&6Disabled Worlds").withLore("&7Value:", config.getStringList("spawners.disabled-worlds")).build());

        editor.setItem(6, new ItemBuilder(Materials.CLOCK)
                .withName("&6Chunk Merge").withLore("&7Value: " + config.getBoolean("spawners.chunk-merge")).build());

        editor.setItem(7, new ItemBuilder(Materials.CLOCK)
                .withName("&6Explosions Break Stack").withLore("&7Value: " + config.getBoolean("spawners.explosions-break-stack")).build());

        editor.setItem(8, new ItemBuilder(Materials.CLOCK)
                .withName("&6Explosions Break Chance").withLore("&7Value: " + config.getInt("spawners.explosions-break-chance")).build());

        editor.setItem(9, new ItemBuilder(Materials.CLOCK)
                .withName("&6Drop Without Silk").withLore("&7Value: " + config.getBoolean("spawners.drop-without-silk")).build());

        editor.setItem(10, new ItemBuilder(Materials.CLOCK)
                .withName("&6Silk-Spawners Enabled").withLore("&7Value: " + config.getBoolean("spawners.silk-spawners.enabled")).build());

        editor.setItem(11, new ItemBuilder(Materials.CLOCK)
                .withName("&6Silk-Spawners Custom Name").withLore("&7Value: " + config.getString("spawners.silk-spawners.custom-name")).build());

        editor.setItem(12, new ItemBuilder(Materials.CLOCK)
                .withName("&6Silk-Spawners Explosions Drop Spawner").withLore("&7Value: " + config.getBoolean("spawners.silk-spawners.explosions-drop-spawner")).build());

        editor.setItem(13, new ItemBuilder(Materials.CLOCK)
                .withName("&6Silk-Spawners Drop To Inventory").withLore("&7Value: " + config.getBoolean("spawners.silk-spawners.drop-to-inventory")).build());

        editor.setItem(14, new ItemBuilder(Materials.CLOCK)
                .withName("&6Shift Get Whole Stack").withLore("&7Value: " + config.getBoolean("spawners.shift-get-whole-stack")).build());

        editor.setItem(15, new ItemBuilder(Materials.CLOCK)
                .withName("&6Get Stacked Item").withLore("&7Value: " + config.getBoolean("spawners.get-stacked-item")).build());

        editor.setItem(16, new ItemBuilder(Materials.CLOCK)
                .withName("&6Floating Names").withLore("&7Value: " + config.getBoolean("spawners.floating-names")).build());

        editor.setItem(17, new ItemBuilder(Materials.CLOCK)
                .withName("&6Break Menu").withLore("&7Value: " + config.getBoolean("spawners.break-menu.enabled")).build());

        editor.setItem(18, new ItemBuilder(Materials.CLOCK)
                .withName("&6Place Menu").withLore("&7Value: " + config.getBoolean("spawners.place-inventory")).build());

        editor.setItem(19, new ItemBuilder(Materials.CLOCK)
                .withName("&6Placement Permission").withLore("&7Value: " + config.getBoolean("spawners.placement-permission")).build());

        editor.setItem(20, new ItemBuilder(Materials.CLOCK)
                .withName("&6Shift Place Stack").withLore("&7Value: " + config.getBoolean("spawners.shift-place-stack")).build());

        editor.setItem(21, new ItemBuilder(Materials.CLOCK)
                .withName("&6Break Charge Amount").withLore("&7Value: " + config.getInt("spawners.break-charge.amount")).build());

        editor.setItem(22, new ItemBuilder(Materials.CLOCK)
                .withName("&6Break Charge Multiply").withLore("&7Value: " + config.getBoolean("spawners.break-charge.multiply-stack-amount")).build());

        editor.setItem(23, new ItemBuilder(Materials.CLOCK)
                .withName("&6Place Charge Amount").withLore("&7Value: " + config.getInt("spawners.place-charge.amount")).build());

        editor.setItem(24, new ItemBuilder(Materials.CLOCK)
                .withName("&6Place Charge Multiply").withLore("&7Value: " + config.getBoolean("spawners.place-charge.multiply-stack-amount")).build());

        editor.setItem(25, new ItemBuilder(Materials.CLOCK)
                .withName("&6Change Using Eggs").withLore("&7Value: " + config.getBoolean("spawners.change-using-eggs")).build());

        editor.setItem(26, new ItemBuilder(Materials.CLOCK)
                .withName("&6Eggs Stack Multiply").withLore("&7Value: " + config.getBoolean("spawners.eggs-stack-multiply")).build());

        editor.setItem(27, new ItemBuilder(Materials.CLOCK)
                .withName("&6Next Spawner Placement").withLore("&7Value: " + config.getBoolean("spawners.next-spawner-placement")).build());

        return editor;
    }

    public Inventory getBarrelsEditor(){
        Inventory editor = Bukkit.createInventory(null, 9 * 2, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings");

        editor.setItem(0, new ItemBuilder(Materials.CLOCK)
                .withName("&6Enabled").withLore("&7Value: " + config.getBoolean("barrels.enabled")).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Merge Radius").withLore("&7Value: " + config.getInt("barrels.merge-radius")).build());

        editor.setItem(2, new ItemBuilder(Materials.CLOCK)
                .withName("&6Custom Name").withLore("&7Value: " + config.getString("barrels.custom-name")).build());

        editor.setItem(3, new ItemBuilder(Materials.CLOCK)
                .withName("&6Blacklist").withLore("&7Value:", config.getStringList("barrels.blacklist")).build());

        editor.setItem(4, new ItemBuilder(Materials.CLOCK)
                .withName("&6Limits").withLore("&7Value:", config.getConfigurationSection("barrels.limits")).build());

        editor.setItem(5, new ItemBuilder(Materials.CLOCK)
                .withName("&6Disabled Worlds").withLore("&7Value:", config.getStringList("barrels.disabled-worlds")).build());

        editor.setItem(6, new ItemBuilder(Materials.CLOCK)
                .withName("&6Chunk Merge").withLore("&7Value: " + config.getBoolean("barrels.chunk-merge")).build());

        editor.setItem(7, new ItemBuilder(Materials.CLOCK)
                .withName("&6Explosions Break Stack").withLore("&7Value: " + config.getBoolean("barrels.explosions-break-stack")).build());

        editor.setItem(8, new ItemBuilder(Materials.CLOCK)
                .withName("&6Toggle Command Enabled").withLore("&7Value: " + config.getBoolean("barrels.toggle-command.enabled")).build());

        editor.setItem(9, new ItemBuilder(Materials.CLOCK)
                .withName("&6Toggle Command Command").withLore("&7Value: " + config.getString("barrels.toggle-command.command")).build());

        editor.setItem(10, new ItemBuilder(Materials.CLOCK)
                .withName("&6Place Inventory").withLore("&7Value: " + config.getString("barrels.place-inventory")).build());

        return editor;
    }

    public Inventory getEditor(String editor){
        switch (editor){
            case "generalEditor":
                return getGeneralEditor();
            case "itemsEditor":
                return getItemsEditor();
            case "entitiesEditor":
                return getEntitiesEditor();
            case "spawnersEditor":
                return getSpawnersEditor();
            case "barrelsEditor":
                return getBarrelsEditor();
        }

        return null;
    }

    private void loadSettingsEditor(){
        Inventory editor = Bukkit.createInventory(null, 9 * 6);

        ItemStack glassPane = new ItemBuilder(Materials.BLACK_STAINED_GLASS_PANE).withName("&6").build();

        for(int i = 0; i < 9; i++)
            editor.setItem(i, glassPane);

        for(int i = 45; i < 54; i++)
            editor.setItem(i, glassPane);

        editor.setItem(9, glassPane);
        editor.setItem(17, glassPane);
        editor.setItem(18, glassPane);
        editor.setItem(26, glassPane);
        editor.setItem(27, glassPane);
        editor.setItem(35, glassPane);
        editor.setItem(36, glassPane);
        editor.setItem(44, glassPane);

        editor.setItem(20, new ItemBuilder(Materials.MAP)
                .withName("&6General Settings").withLore("&7Click to edit general settings.").build());

        editor.setItem(22, new ItemBuilder(Material.GOLD_INGOT)
                .withName("&6Items Settings").withLore("&7Click to edit items settings.").build());

        editor.setItem(24, new ItemBuilder(Material.ROTTEN_FLESH)
                .withName("&6Entities Settings").withLore("&7Click to edit entities settings.").build());

        editor.setItem(30, new ItemBuilder(Materials.SPAWNER)
                .withName("&6Spawner Settings").withLore("&7Click to edit spawners settings.").build());

        editor.setItem(32, new ItemBuilder(Materials.CAULDRON)
                .withName("&6Barrels Settings").withLore("&7Click to edit barrels settings.").build());

        editor.setItem(49, new ItemBuilder(Material.EMERALD)
                .withName("&aSave Changes").withLore("&7Click to save all changes.").build());

        settingsEditor = editor.getContents();
    }

}
