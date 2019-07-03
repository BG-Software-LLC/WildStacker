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

    public static String GENERAL_SLOT_0 = "save-interval", GENERAL_SLOT_1 = "give-item-name";

    public static String ITEMS_SLOT_0 = "items.enabled", ITEMS_SLOT_1 = "items.merge-radius",
            ITEMS_SLOT_2 = "items.custom-name", ITEMS_SLOT_3 = "items.blacklist", ITEMS_SLOT_4 = "items.whitelist",
            ITEMS_SLOT_5 = "items.limits", ITEMS_SLOT_6 = "items.disabled-worlds", ITEMS_SLOT_7 = "items.chunk-limit",
            ITEMS_SLOT_8 = "items.fix-stack", ITEMS_SLOT_9 = "items.item-display", ITEMS_SLOT_10 = "items.buckets-stacker.enabled",
            ITEMS_SLOT_11 = "items.buckets-stacker.name-blacklist", ITEMS_SLOT_12 = "items.buckets-stacker.max-stack",
            ITEMS_SLOT_13 = "items.kill-all", ITEMS_SLOT_14 = "items.names-toggle.enabled",
            ITEMS_SLOT_15 = "items.names-toggle.command", ITEMS_SLOT_16 = "items.pickup-sound.enabled",
            ITEMS_SLOT_17 = "items.pickup-sound.volume", ITEMS_SLOT_18 = "items.pickup-sound.pitch";

    public static String ENTITIES_SLOT_0 = "entities.enabled", ENTITIES_SLOT_1 = "entities.merge-radius",
            ENTITIES_SLOT_2 = "entities.custom-name", ENTITIES_SLOT_3 = "entities.blacklist",
            ENTITIES_SLOT_4 = "entities.whitelist", ENTITIES_SLOT_5 = "entities.limits",
            ENTITIES_SLOT_6 = "entities.minimum-limits", ENTITIES_SLOT_7 = "entities.disabled-worlds",
            ENTITIES_SLOT_8 = "entities.chunk-limit", ENTITIES_SLOT_9 = "entities.disabled-regions",
            ENTITIES_SLOT_10 = "entities.name-blacklist", ENTITIES_SLOT_11 = "entities.stack-interval",
            ENTITIES_SLOT_12 = "entities.kill-all.interval", ENTITIES_SLOT_13 = "entities.kill-all.clear-lagg",
            ENTITIES_SLOT_14 = "entities.stack-checks", ENTITIES_SLOT_15 = "entities.stack-split",
            ENTITIES_SLOT_16 = "entities.linked-entities.enabled", ENTITIES_SLOT_17 = "entities.linked-entities.max-distance",
            ENTITIES_SLOT_18 = "entities.instant-kill", ENTITIES_SLOT_19 = "entities.nerfed-spawning",
            ENTITIES_SLOT_20 = "entities.nerfed-worlds", ENTITIES_SLOT_21 = "entities.stack-down.enabled",
            ENTITIES_SLOT_22 = "entities.stack-down.stack-down-types", ENTITIES_SLOT_23 = "entities.keep-fire",
            ENTITIES_SLOT_24 = "entities.mythic-mobs-custom-name", ENTITIES_SLOT_25 = "entities.keep-lowest-health",
            ENTITIES_SLOT_26 = "entities.stack-after-breed", ENTITIES_SLOT_27 = "entities.hide-names",
            ENTITIES_SLOT_28 = "entities.next-stack-knockback";

    public static String SPAWNERS_SLOT_0 = "spawners.enabled", SPAWNERS_SLOT_1 = "spawners.merge-radius",
            SPAWNERS_SLOT_2 = "spawners.custom-name", SPAWNERS_SLOT_3 = "spawners.blacklist",
            SPAWNERS_SLOT_4 = "spawners.whitelist", SPAWNERS_SLOT_5 = "spawners.limits",
            SPAWNERS_SLOT_6 = "spawners.disabled-worlds", SPAWNERS_SLOT_7 = "spawners.chunk-limit",
            SPAWNERS_SLOT_8 = "spawners.chunk-merge", SPAWNERS_SLOT_9 = "spawners.explosions-break-stack",
            SPAWNERS_SLOT_10 = "spawners.explosions-break-chance", SPAWNERS_SLOT_11 = "spawners.silk-touch-break-chance",
            SPAWNERS_SLOT_12 = "spawners.drop-without-silk", SPAWNERS_SLOT_13 = "spawners.silk-spawners.enabled",
            SPAWNERS_SLOT_14 = "spawners.silk-spawners.custom-name", SPAWNERS_SLOT_15 = "spawners.silk-spawners.explosions-drop-spawner",
            SPAWNERS_SLOT_16 = "spawners.silk-spawners.drop-to-inventory", SPAWNERS_SLOT_17 = "spawners.shift-get-whole-stack",
            SPAWNERS_SLOT_18 = "spawners.get-stacked-item", SPAWNERS_SLOT_19 = "spawners.floating-names",
            SPAWNERS_SLOT_20 = "spawners.break-menu.enabled", SPAWNERS_SLOT_21 = "spawners.place-inventory",
            SPAWNERS_SLOT_22 = "spawners.placement-permission", SPAWNERS_SLOT_23 = "spawners.shift-place-stack",
            SPAWNERS_SLOT_24 = "spawners.break-charge.amount", SPAWNERS_SLOT_25 = "spawners.break-charge.multiply-stack-amount",
            SPAWNERS_SLOT_26 = "spawners.place-charge.amount", SPAWNERS_SLOT_27 = "spawners.place-charge.multiply-stack-amount",
            SPAWNERS_SLOT_28 = "spawners.change-using-eggs", SPAWNERS_SLOT_29 = "spawners.eggs-stack-multiply",
            SPAWNERS_SLOT_30 = "spawners.next-spawner-placement", SPAWNERS_SLOT_31 = "spawners.only-one-spawner";

    public static String BARRELS_SLOT_0 = "barrels.enabled", BARRELS_SLOT_1 = "barrels.merge-radius",
            BARRELS_SLOT_2 = "barrels.custom-name", BARRELS_SLOT_3 = "barrels.blacklist", BARRELS_SLOT_4 = "barrels.whitelist",
            BARRELS_SLOT_5 = "barrels.limits", BARRELS_SLOT_6 = "barrels.disabled-worlds", BARRELS_SLOT_7 = "barrels.chunk-limit",
            BARRELS_SLOT_8 = "barrels.chunk-merge", BARRELS_SLOT_9 = "barrels.explosions-break-stack",
            BARRELS_SLOT_10 = "barrels.toggle-command.enabled", BARRELS_SLOT_11 = "barrels.toggle-command.command",
            BARRELS_SLOT_12 = "barrels.place-inventory";

    public EditorHandler(WildStackerPlugin plugin){
        this.plugin = plugin;
        this.config = new CommentedConfiguration(ConfigComments.class, new File(plugin.getDataFolder(), "config.yml"));
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
                .withName("&6Save Interval").withLore("&7Value: " + config.getLong(GENERAL_SLOT_0)).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Give-Item Name").withLore("&7Value: " + config.getString(GENERAL_SLOT_1)).build());

        return editor;
    }

    public Inventory getItemsEditor(){
        Inventory editor = Bukkit.createInventory(null, 27, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Items Settings");

        editor.setItem(0, new ItemBuilder(Materials.CLOCK)
                .withName("&6Enabled").withLore("&7Value: " + config.getBoolean(ITEMS_SLOT_0)).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Merge Radius").withLore("&7Value: " + config.getInt(ITEMS_SLOT_1)).build());

        editor.setItem(2, new ItemBuilder(Materials.CLOCK)
                .withName("&6Custom Name").withLore("&7Value: " + config.getString(ITEMS_SLOT_2)).build());

        editor.setItem(3, new ItemBuilder(Materials.CLOCK)
                .withName("&6Blacklist").withLore("&7Value:", config.getStringList(ITEMS_SLOT_3)).build());

        editor.setItem(4, new ItemBuilder(Materials.CLOCK)
                .withName("&6Whitelist").withLore("&7Value:", config.getStringList(ITEMS_SLOT_4)).build());

        editor.setItem(5, new ItemBuilder(Materials.CLOCK)
                .withName("&6Limits").withLore("&7Value:", config.getConfigurationSection(ITEMS_SLOT_5)).build());

        editor.setItem(6, new ItemBuilder(Materials.CLOCK)
                .withName("&6Disabled Worlds").withLore("&7Value:", config.getStringList(ITEMS_SLOT_6)).build());

        editor.setItem(7, new ItemBuilder(Materials.CLOCK)
                .withName("&6Chunk Limit").withLore("&7Value: " + config.getInt(ITEMS_SLOT_7)).build());

        editor.setItem(8, new ItemBuilder(Materials.CLOCK)
                .withName("&6Fix Stack").withLore("&7Value: " + config.getBoolean(ITEMS_SLOT_8)).build());

        editor.setItem(9, new ItemBuilder(Materials.CLOCK)
                .withName("&6Item Display").withLore("&7Value: " + config.getBoolean(ITEMS_SLOT_9)).build());

        editor.setItem(10, new ItemBuilder(Materials.CLOCK)
                .withName("&6Buckets Stacker Enabled").withLore("&7Value: " + config.getBoolean(ITEMS_SLOT_10)).build());

        editor.setItem(11, new ItemBuilder(Materials.CLOCK)
                .withName("&6Buckets Name Blacklist").withLore("&7Value:", config.getStringList(ITEMS_SLOT_11)).build());

        editor.setItem(12, new ItemBuilder(Materials.CLOCK)
                .withName("&6Buckets Max Stack").withLore("&7Value: " + config.getInt(ITEMS_SLOT_12)).build());

        editor.setItem(13, new ItemBuilder(Materials.CLOCK)
                .withName("&6Items Kill All").withLore("&7Value: " + config.getBoolean(ITEMS_SLOT_13)).build());

        editor.setItem(14, new ItemBuilder(Materials.CLOCK)
                .withName("&6Names Toggle Enabled").withLore("&7Value: " + config.getBoolean(ITEMS_SLOT_14)).build());

        editor.setItem(15, new ItemBuilder(Materials.CLOCK)
                .withName("&6Names Toggle Command").withLore("&7Value: " + config.getString(ITEMS_SLOT_15)).build());

        editor.setItem(16, new ItemBuilder(Materials.CLOCK)
                .withName("&6Pickup Sound Enabled").withLore("&7Value: " + config.getBoolean(ITEMS_SLOT_16)).build());

        editor.setItem(17, new ItemBuilder(Materials.CLOCK)
                .withName("&6Pickup Sound Volume").withLore("&7Value: " + config.getDouble(ITEMS_SLOT_17)).build());

        editor.setItem(18, new ItemBuilder(Materials.CLOCK)
                .withName("&6Pickup Sound Pitch").withLore("&7Value: " + config.getDouble(ITEMS_SLOT_18)).build());

        return editor;
    }

    public Inventory getEntitiesEditor(){
        Inventory editor = Bukkit.createInventory(null, 9 * 4, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Entities Settings");

        editor.setItem(0, new ItemBuilder(Materials.CLOCK)
                .withName("&6Enabled").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_0)).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Merge Radius").withLore("&7Value: " + config.getInt(ENTITIES_SLOT_1)).build());

        editor.setItem(2, new ItemBuilder(Materials.CLOCK)
                .withName("&6Custom Name").withLore("&7Value: " + config.getString(ENTITIES_SLOT_2)).build());

        editor.setItem(3, new ItemBuilder(Materials.CLOCK)
                .withName("&6Blacklist").withLore("&7Value:", config.getStringList(ENTITIES_SLOT_3)).build());

        editor.setItem(4, new ItemBuilder(Materials.CLOCK)
                .withName("&6Whitelist").withLore("&7Value:", config.getStringList(ENTITIES_SLOT_4)).build());

        editor.setItem(5, new ItemBuilder(Materials.CLOCK)
                .withName("&6Limits").withLore("&7Value:", config.getConfigurationSection(ENTITIES_SLOT_5)).build());

        editor.setItem(6, new ItemBuilder(Materials.CLOCK)
                .withName("&6Minimum Limits").withLore("&7Value:", config.getConfigurationSection(ENTITIES_SLOT_6)).build());

        editor.setItem(7, new ItemBuilder(Materials.CLOCK)
                .withName("&6Disabled Worlds").withLore("&7Value:", config.getStringList(ENTITIES_SLOT_7)).build());

        editor.setItem(8, new ItemBuilder(Materials.CLOCK)
                .withName("&6Chunk Limit").withLore("&7Value: " + config.getInt(ENTITIES_SLOT_8)).build());

        editor.setItem(9, new ItemBuilder(Materials.CLOCK)
                .withName("&6Disabled Regions").withLore("&7Value:", config.getStringList(ENTITIES_SLOT_9)).build());

        editor.setItem(10, new ItemBuilder(Materials.CLOCK)
                .withName("&6Name Blacklist").withLore("&7Value:", config.getStringList(ENTITIES_SLOT_10)).build());

        editor.setItem(11, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Interval").withLore("&7Value: " + config.getInt(ENTITIES_SLOT_11)).build());

        editor.setItem(12, new ItemBuilder(Materials.CLOCK)
                .withName("&6Kill-All Interval").withLore("&7Value: " + config.getInt(ENTITIES_SLOT_12)).build());

        editor.setItem(13, new ItemBuilder(Materials.CLOCK)
                .withName("&6Kill-All ClearLagg").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_13)).build());

        editor.setItem(14, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Checks").withLore("&7Value &c(Do not remove sections)&7:", config.getConfigurationSection(ENTITIES_SLOT_14)).build());

        editor.setItem(15, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Split").withLore("&7Value &c(Do not remove sections)&7:", config.getConfigurationSection(ENTITIES_SLOT_15)).build());

        editor.setItem(16, new ItemBuilder(Materials.CLOCK)
                .withName("&6Linked Entities Enabled").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_16)).build());

        editor.setItem(17, new ItemBuilder(Materials.CLOCK)
                .withName("&6Linked Entities Max Distance").withLore("&7Value: " + config.getInt(ENTITIES_SLOT_17)).build());

        editor.setItem(18, new ItemBuilder(Materials.CLOCK)
                .withName("&6Instant Kill").withLore("&7Value:", config.getStringList(ENTITIES_SLOT_18)).build());

        editor.setItem(19, new ItemBuilder(Materials.CLOCK)
                .withName("&6Nerfed Spawning").withLore("&7Value:", config.getStringList(ENTITIES_SLOT_19)).build());

        editor.setItem(20, new ItemBuilder(Materials.CLOCK)
                .withName("&6Nerfed Worlds").withLore("&7Value:", config.getStringList(ENTITIES_SLOT_20)).build());

        editor.setItem(21, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Down Enabled").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_21)).build());

        editor.setItem(22, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack Down Types").withLore("&7Value:", config.getStringList(ENTITIES_SLOT_22)).build());

        editor.setItem(23, new ItemBuilder(Materials.CLOCK)
                .withName("&6Keep Fire").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_23)).build());

        editor.setItem(24, new ItemBuilder(Materials.CLOCK)
                .withName("&6MythicMobs Custom Name").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_24)).build());

        editor.setItem(25, new ItemBuilder(Materials.CLOCK)
                .withName("&6Keep Lowest Health").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_25)).build());

        editor.setItem(26, new ItemBuilder(Materials.CLOCK)
                .withName("&6Stack After Breed").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_26)).build());

        editor.setItem(27, new ItemBuilder(Materials.CLOCK)
                .withName("&6Hide Names").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_27)).build());

        editor.setItem(28, new ItemBuilder(Materials.CLOCK)
                .withName("&6Next Stack Knockback").withLore("&7Value: " + config.getBoolean(ENTITIES_SLOT_28)).build());

        return editor;
    }

    public Inventory getSpawnersEditor(){
        Inventory editor = Bukkit.createInventory(null, 9 * 4, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Spawners Settings");

        editor.setItem(0, new ItemBuilder(Materials.CLOCK)
                .withName("&6Enabled").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_0)).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Merge Radius").withLore("&7Value: " + config.getInt(SPAWNERS_SLOT_1)).build());

        editor.setItem(2, new ItemBuilder(Materials.CLOCK)
                .withName("&6Custom Name").withLore("&7Value: " + config.getString(SPAWNERS_SLOT_2)).build());

        editor.setItem(3, new ItemBuilder(Materials.CLOCK)
                .withName("&6Blacklist").withLore("&7Value:", config.getStringList(SPAWNERS_SLOT_3)).build());

        editor.setItem(4, new ItemBuilder(Materials.CLOCK)
                .withName("&6Whitelist").withLore("&7Value:", config.getStringList(SPAWNERS_SLOT_4)).build());

        editor.setItem(5, new ItemBuilder(Materials.CLOCK)
                .withName("&6Limits").withLore("&7Value:", config.getConfigurationSection(SPAWNERS_SLOT_5)).build());

        editor.setItem(6, new ItemBuilder(Materials.CLOCK)
                .withName("&6Disabled Worlds").withLore("&7Value:", config.getStringList(SPAWNERS_SLOT_6)).build());

        editor.setItem(7, new ItemBuilder(Materials.CLOCK)
                .withName("&6Chunk Limit").withLore("&7Value: " + config.getInt(SPAWNERS_SLOT_7)).build());

        editor.setItem(8, new ItemBuilder(Materials.CLOCK)
                .withName("&6Chunk Merge").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_8)).build());

        editor.setItem(9, new ItemBuilder(Materials.CLOCK)
                .withName("&6Explosions Break Stack").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_9)).build());

        editor.setItem(10, new ItemBuilder(Materials.CLOCK)
                .withName("&6Explosions Break Chance").withLore("&7Value: " + config.getInt(SPAWNERS_SLOT_10)).build());

        editor.setItem(11, new ItemBuilder(Materials.CLOCK)
                .withName("&6Silk Touch Break Chance").withLore("&7Value: " + config.getInt(SPAWNERS_SLOT_11)).build());

        editor.setItem(12, new ItemBuilder(Materials.CLOCK)
                .withName("&6Drop Without Silk").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_12)).build());

        editor.setItem(13, new ItemBuilder(Materials.CLOCK)
                .withName("&6Silk-Spawners Enabled").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_13)).build());

        editor.setItem(14, new ItemBuilder(Materials.CLOCK)
                .withName("&6Silk-Spawners Custom Name").withLore("&7Value: " + config.getString(SPAWNERS_SLOT_14)).build());

        editor.setItem(15, new ItemBuilder(Materials.CLOCK)
                .withName("&6Silk-Spawners Explosions Drop Spawner").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_15)).build());

        editor.setItem(16, new ItemBuilder(Materials.CLOCK)
                .withName("&6Silk-Spawners Drop To Inventory").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_16)).build());

        editor.setItem(17, new ItemBuilder(Materials.CLOCK)
                .withName("&6Shift Get Whole Stack").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_17)).build());

        editor.setItem(18, new ItemBuilder(Materials.CLOCK)
                .withName("&6Get Stacked Item").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_18)).build());

        editor.setItem(19, new ItemBuilder(Materials.CLOCK)
                .withName("&6Floating Names").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_19)).build());

        editor.setItem(20, new ItemBuilder(Materials.CLOCK)
                .withName("&6Break Menu").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_20)).build());

        editor.setItem(21, new ItemBuilder(Materials.CLOCK)
                .withName("&6Place Menu").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_21)).build());

        editor.setItem(22, new ItemBuilder(Materials.CLOCK)
                .withName("&6Placement Permission").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_22)).build());

        editor.setItem(23, new ItemBuilder(Materials.CLOCK)
                .withName("&6Shift Place Stack").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_23)).build());

        editor.setItem(24, new ItemBuilder(Materials.CLOCK)
                .withName("&6Break Charge Amount").withLore("&7Value: " + config.getInt(SPAWNERS_SLOT_24)).build());

        editor.setItem(25, new ItemBuilder(Materials.CLOCK)
                .withName("&6Break Charge Multiply").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_25)).build());

        editor.setItem(26, new ItemBuilder(Materials.CLOCK)
                .withName("&6Place Charge Amount").withLore("&7Value: " + config.getInt(SPAWNERS_SLOT_26)).build());

        editor.setItem(27, new ItemBuilder(Materials.CLOCK)
                .withName("&6Place Charge Multiply").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_27)).build());

        editor.setItem(28, new ItemBuilder(Materials.CLOCK)
                .withName("&6Change Using Eggs").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_28)).build());

        editor.setItem(29, new ItemBuilder(Materials.CLOCK)
                .withName("&6Eggs Stack Multiply").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_29)).build());

        editor.setItem(30, new ItemBuilder(Materials.CLOCK)
                .withName("&6Next Spawner Placement").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_30)).build());

        editor.setItem(31, new ItemBuilder(Materials.CLOCK)
                .withName("&6Only One Spawner").withLore("&7Value: " + config.getBoolean(SPAWNERS_SLOT_31)).build());

        return editor;
    }

    public Inventory getBarrelsEditor(){
        Inventory editor = Bukkit.createInventory(null, 9 * 2, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Barrels Settings");

        editor.setItem(0, new ItemBuilder(Materials.CLOCK)
                .withName("&6Enabled").withLore("&7Value: " + config.getBoolean(BARRELS_SLOT_0)).build());

        editor.setItem(1, new ItemBuilder(Materials.CLOCK)
                .withName("&6Merge Radius").withLore("&7Value: " + config.getInt(BARRELS_SLOT_1)).build());

        editor.setItem(2, new ItemBuilder(Materials.CLOCK)
                .withName("&6Custom Name").withLore("&7Value: " + config.getString(BARRELS_SLOT_2)).build());

        editor.setItem(3, new ItemBuilder(Materials.CLOCK)
                .withName("&6Blacklist").withLore("&7Value:", config.getStringList(BARRELS_SLOT_3)).build());

        editor.setItem(4, new ItemBuilder(Materials.CLOCK)
                .withName("&6Whitelist").withLore("&7Value:", config.getStringList(BARRELS_SLOT_4)).build());

        editor.setItem(5, new ItemBuilder(Materials.CLOCK)
                .withName("&6Limits").withLore("&7Value:", config.getConfigurationSection(BARRELS_SLOT_5)).build());

        editor.setItem(6, new ItemBuilder(Materials.CLOCK)
                .withName("&6Disabled Worlds").withLore("&7Value:", config.getStringList(BARRELS_SLOT_6)).build());

        editor.setItem(7, new ItemBuilder(Materials.CLOCK)
                .withName("&6Chunk Limit").withLore("&7Value: " + config.getInt(BARRELS_SLOT_7)).build());

        editor.setItem(8, new ItemBuilder(Materials.CLOCK)
                .withName("&6Chunk Merge").withLore("&7Value: " + config.getBoolean(BARRELS_SLOT_8)).build());

        editor.setItem(9, new ItemBuilder(Materials.CLOCK)
                .withName("&6Explosions Break Stack").withLore("&7Value: " + config.getBoolean(BARRELS_SLOT_9)).build());

        editor.setItem(10, new ItemBuilder(Materials.CLOCK)
                .withName("&6Toggle Command Enabled").withLore("&7Value: " + config.getBoolean(BARRELS_SLOT_10)).build());

        editor.setItem(11, new ItemBuilder(Materials.CLOCK)
                .withName("&6Toggle Command Command").withLore("&7Value: " + config.getString(BARRELS_SLOT_11)).build());

        editor.setItem(12, new ItemBuilder(Materials.CLOCK)
                .withName("&6Place Inventory").withLore("&7Value: " + config.getString(BARRELS_SLOT_12)).build());

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
