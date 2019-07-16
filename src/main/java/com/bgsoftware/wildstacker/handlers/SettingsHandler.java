package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackCheck;
import com.bgsoftware.wildstacker.api.enums.StackSplit;
import com.bgsoftware.wildstacker.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.config.ConfigComments;
import com.bgsoftware.wildstacker.key.Key;
import com.bgsoftware.wildstacker.key.KeyMap;
import com.bgsoftware.wildstacker.key.KeySet;
import com.bgsoftware.wildstacker.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler {

    //Global settings
    public final long saveInterval;
    public final String giveItemName;
    public final ItemStack inspectTool;
    public final KeyMap<String> customNames;

    //Items settings
    public final boolean itemsStackingEnabled, itemsFixStackEnabled, itemsDisplayEnabled, bucketsStackerEnabled,
            itemsUnstackedCustomName, itemsKillAll, itemsNamesToggleEnabled, itemsSoundEnabled;
    public final List<String> itemsDisabledWorlds, bucketsBlacklistedNames;
    public final KeySet blacklistedItems, whitelistedItems;
    public final int itemsCheckRange, itemsChunkLimit, bucketsMaxStack;
    public final String itemsCustomName, itemsNamesToggleCommand;
    public final KeyMap<Integer> itemsLimits;
    public final float itemsSoundVolume, itemsSoundPitch;

    //Entities settings
    public final boolean entitiesStackingEnabled, linkedEntitiesEnabled, clearLaggHookEnabled, stackDownEnabled, keepFireEnabled,
            mythicMobsCustomNameEnabled, keepLowestHealth, stackAfterBreed, entitiesHideNames, entitiesNamesToggleEnabled,
            nextStackKnockback;
    public final long entitiesStackInterval, entitiesKillAllInterval;
    public final String entitiesCustomName, entitiesNamesToggleCommand;
    public final int entitiesCheckRange, linkedEntitiesMaxDistance, entitiesChunkLimit;
    public final List<String> entitiesDisabledWorlds, entitiesDisabledRegions, blacklistedEntities, whitelistedEntities,
            blacklistedEntitiesSpawnReasons, blacklistedEntitiesNames, entitiesInstantKills, nerfedSpawning, nerfedWorlds,
            noAiSpawning, noAiWorlds, stackDownTypes;
    public final KeyMap<Integer> entitiesLimits, minimumEntitiesLimit;

    //Spawners settings
    public final boolean spawnersStackingEnabled, chunkMergeSpawners, explosionsBreakSpawnerStack, silkTouchSpawners,
            explosionsDropSpawner, dropToInventory, shiftGetWholeSpawnerStack, getStackedItem, dropSpawnerWithoutSilk,
            floatingSpawnerNames, spawnersBreakMenu, spawnersPlaceMenu, spawnersPlacementPermission, spawnersShiftPlaceStack,
            breakChargeMultiply, placeChargeMultiply, changeUsingEggs, eggsStackMultiply, nextSpawnerPlacement, onlyOneSpawner;
    public final int spawnersCheckRange, explosionsBreakChance, silkTouchBreakChance, spawnersChunkLimit;
    public final double breakChargeAmount, placeChargeAmount;
    public final List<String> spawnersDisabledWorlds, blacklistedSpawners, whitelistedSpawners;
    public final String hologramCustomName, silkCustomName;
    public final KeyMap<Integer> spawnersLimits;

    //Barrels settings
    public final boolean barrelsStackingEnabled, chunkMergeBarrels, explosionsBreakBarrelStack, barrelsToggleCommand,
            barrelsPlaceInventory;
    public final int barrelsCheckRange, barrelsChunkLimit;
    public final String barrelsCustomName, barrelsToggleCommandSyntax;
    public final List<String> barrelsDisabledWorlds;
    public final KeySet blacklistedBarrels, whitelistedBarrels;
    public final KeyMap<Integer> barrelsLimits;

    public SettingsHandler(WildStackerPlugin plugin){
        WildStackerPlugin.log("Loading configuration started...");
        long startTime = System.currentTimeMillis();
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = new CommentedConfiguration(ConfigComments.class, file);

        dataConvertor(cfg);

        cfg.resetYamlFile(plugin, "config.yml");

        saveInterval = cfg.getLong("save-interval", 6000);
        giveItemName = ChatColor.translateAlternateColorCodes('&', cfg.getString("give-item-name", "&e{0} &f{1} x{2}"));
        inspectTool = new ItemBuilder(Material.valueOf(cfg.getString("inspect-tool.type")), cfg.getInt("inspect-tool.data", 0))
                .withName(cfg.getString("inspect-tool.name"))
                .withLore(cfg.getStringList("inspect-tool.lore")).build();
        customNames = new KeyMap<>();
        loadCustomNames(plugin);

        itemsStackingEnabled = cfg.getBoolean("items.enabled", true);
        itemsDisabledWorlds = cfg.getStringList("items.disabled-worlds");
        itemsUnstackedCustomName = cfg.getBoolean("items.unstacked-custom-name", false);
        itemsFixStackEnabled = cfg.getBoolean("items.fix-stack", false);
        blacklistedItems = new KeySet(cfg.getStringList("items.blacklist"));
        whitelistedItems = new KeySet(cfg.getStringList("items.whitelist"));
        itemsCheckRange = cfg.getInt("items.merge-radius", 5);
        itemsChunkLimit = cfg.getInt("items.chunk-limit", 0);
        itemsCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("items.custom-name", "&6&lx{0} {1}"));
        itemsDisplayEnabled = cfg.getBoolean("items.item-display", false);
        bucketsStackerEnabled = cfg.getBoolean("items.buckets-stacker.enabled", true);
        bucketsBlacklistedNames = cfg.getStringList("items.buckets-stacker.name-blacklist");
        bucketsMaxStack = cfg.getInt("items.buckets-stacker.max-stack", 16);
        itemsKillAll = cfg.getBoolean("items.kill-all", true);
        itemsNamesToggleEnabled = cfg.getBoolean("items.names-toggle.enabled", false);
        itemsNamesToggleCommand = cfg.getString("items.names-toggle.command", "stacker names item");
        itemsSoundEnabled = cfg.getBoolean("items.pickup-sound.enabled", true);
        itemsSoundVolume = (float) cfg.getDouble("items.pickup-sound.volume");
        itemsSoundPitch = (float) cfg.getDouble("items.pickup-sound.pitch");

        entitiesStackingEnabled = cfg.getBoolean("entities.enabled", true);
        entitiesStackInterval = cfg.getLong("entities.stack-interval", 0);
        entitiesDisabledWorlds = cfg.getStringList("entities.disabled-worlds");
        entitiesCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("entities.custom-name", "&d&lx{0} {1}"));
        entitiesCheckRange = cfg.getInt("entities.merge-radius", 10);
        entitiesChunkLimit = cfg.getInt("entities.chunk-limit", 0);
        entitiesDisabledRegions = cfg.getStringList("entities.disabled-regions");
        linkedEntitiesEnabled = cfg.getBoolean("entities.linked-entities.enabled", true);
        linkedEntitiesMaxDistance = cfg.getInt("entities.linked-entities.max-distance", 10);
        blacklistedEntities = cfg.getStringList("entities.blacklist");
        whitelistedEntities = cfg.getStringList("entities.whitelist");
        blacklistedEntitiesSpawnReasons = cfg.getStringList("entities.spawn-blacklist");
        blacklistedEntitiesNames = cfg.getStringList("entities.name-blacklist");
        entitiesInstantKills = cfg.getStringList("entities.instant-kill");
        entitiesKillAllInterval = cfg.getLong("entities.kill-all.interval", 6000);
        clearLaggHookEnabled = cfg.getBoolean("entities.kill-all.clear-lagg", true);
        nerfedSpawning = cfg.getStringList("entities.nerfed-spawning");
        nerfedWorlds = cfg.getStringList("entities.nerfed-worlds");
        noAiSpawning = cfg.getStringList("entities.no-ai-spawning");
        noAiWorlds = cfg.getStringList("entities.no-ai-worlds");
        stackDownEnabled = cfg.getBoolean("entities.stack-down.enabled", true);
        stackDownTypes = cfg.getStringList("entities.stack-down.stack-down-types");
        keepFireEnabled = cfg.getBoolean("entities.keep-fire", true);
        mythicMobsCustomNameEnabled = cfg.getBoolean("entities.mythic-mobs-custom-name", true);
        keepLowestHealth = cfg.getBoolean("entities.keep-lowest-health", false);
        stackAfterBreed = cfg.getBoolean("entities.stack-after-breed", true);
        entitiesHideNames = cfg.getBoolean("entities.hide-names", false);
        entitiesNamesToggleEnabled = cfg.getBoolean("entities.names-toggle.enabled", false);
        entitiesNamesToggleCommand = cfg.getString("entities.names-toggle.command", "stacker names entity");
        nextStackKnockback = cfg.getBoolean("entities.next-stack-knockback", true);

        boolean stackingEnable = !Bukkit.getPluginManager().isPluginEnabled("EpicSpawners") && !Bukkit.getPluginManager().isPluginEnabled("MergedSpawner");
        spawnersStackingEnabled = stackingEnable && cfg.getBoolean("spawners.enabled", true);
        spawnersDisabledWorlds = cfg.getStringList("spawners.disabled-worlds");
        spawnersCheckRange = cfg.getInt("spawners.merge-radius", 1);
        chunkMergeSpawners = cfg.getBoolean("spawners.chunk-merge", false);
        blacklistedSpawners = cfg.getStringList("spawners.blacklist");
        whitelistedSpawners = cfg.getStringList("spawners.whitelist");
        spawnersChunkLimit = cfg.getInt("spawners.chunk-limit", 0);
        hologramCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("spawners.custom-name", "&9&lx{0} {1}"));
        explosionsBreakSpawnerStack = cfg.getBoolean("spawners.explosions-break-stack", true);
        explosionsBreakChance = cfg.getInt("spawners.explosions-break-chance", 100);
        silkTouchBreakChance = cfg.getInt("spawners.silk-touch-break-chance", 100);
        dropSpawnerWithoutSilk = cfg.getBoolean("spawners.drop-without-silk", false);
        silkTouchSpawners = cfg.getBoolean("spawners.silk-spawners.enabled", true);
        silkCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("spawners.silk-spawners.custom-name", "&e{0} &fSpawner"));
        explosionsDropSpawner = cfg.getBoolean("spawners.silk-spawners.explosions-drop-spawner", true);
        dropToInventory = cfg.getBoolean("spawners.silk-spawners.drop-to-inventory", true);
        shiftGetWholeSpawnerStack = cfg.getBoolean("spawners.shift-get-whole-stack", true);
        getStackedItem = cfg.getBoolean("spawners.get-stacked-item", true);
        floatingSpawnerNames = cfg.getBoolean("spawners.floating-names", false);
        spawnersBreakMenu = cfg.getBoolean("spawners.break-menu.enabled", true);
        spawnersPlaceMenu = !spawnersBreakMenu && cfg.getBoolean("spawners.place-inventory", false);
        plugin.getBreakMenuHandler().loadMenu(cfg.getConfigurationSection("spawners.break-menu"));
        spawnersPlacementPermission = cfg.getBoolean("spawners.placement-permission", false);
        spawnersShiftPlaceStack = cfg.getBoolean("spawners.shift-place-stack", true);
        breakChargeAmount = cfg.getDouble("spawners.break-charge.amount", 0);
        breakChargeMultiply = cfg.getBoolean("spawners.break-charge.multiply-stack-amount", false);
        placeChargeAmount = cfg.getDouble("spawners.place-charge.amount", 0);
        placeChargeMultiply = cfg.getBoolean("spawners.place-charge.multiply-stack-amount", false);
        changeUsingEggs = cfg.getBoolean("spawners.change-using-eggs", true);
        eggsStackMultiply = cfg.getBoolean("spawners.eggs-stack-multiply", true);
        nextSpawnerPlacement = cfg.getBoolean("spawners.next-spawner-placement", true);
        onlyOneSpawner = cfg.getBoolean("spawners.only-one-spawner", true);

        barrelsStackingEnabled = cfg.getBoolean("barrels.enabled", true);
        barrelsDisabledWorlds = cfg.getStringList("barrels.disabled-worlds");
        barrelsCheckRange = cfg.getInt("barrels.merge-radius", 1);
        chunkMergeBarrels = cfg.getBoolean("barrels.chunk-merge", false);
        barrelsCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("barrels.custom-name", "&9&lx{0} {1}"));
        blacklistedBarrels = new KeySet(cfg.getStringList("barrels.blacklist"));
        whitelistedBarrels = new KeySet(cfg.getStringList("barrels.whitelist"));
        barrelsChunkLimit = cfg.getInt("barrels.chunk-limit", 0);
        explosionsBreakBarrelStack = cfg.getBoolean("barrels.explosions-break-stack", true);
        barrelsToggleCommand = cfg.getBoolean("barrels.toggle-command.enabled", false);
        barrelsToggleCommandSyntax = cfg.getString("barrels.toggle-command.command", "stacker toggle");
        barrelsPlaceInventory = cfg.getBoolean("barrels.place-inventory", true);

        for(StackCheck check : StackCheck.values()) {
            check.setEnabled(cfg.getBoolean("entities.stack-checks." + check.name(), false));
        }

        for(StackSplit split : StackSplit.values()) {
            split.setEnabled(cfg.getBoolean("entities.stack-split." + split.name(), false));
        }


        loadLimits((itemsLimits = new KeyMap<>()), cfg.getConfigurationSection("items.limits"));
        loadLimits((entitiesLimits = new KeyMap<>()), cfg.getConfigurationSection("entities.limits"));
        loadLimits((minimumEntitiesLimit = new KeyMap<>()), cfg.getConfigurationSection("entities.minimum-limits"));
        loadLimits((spawnersLimits = new KeyMap<>()), cfg.getConfigurationSection("spawners.limits"));
        loadLimits((barrelsLimits = new KeyMap<>()), cfg.getConfigurationSection("barrels.limits"));

        WildStackerPlugin.log(" - Stacking drops is " + getBoolean(itemsStackingEnabled));
        WildStackerPlugin.log(" - Stacking entities is " + getBoolean(entitiesStackingEnabled));
        WildStackerPlugin.log(" - Stacking spawners is " + getBoolean(spawnersStackingEnabled));
        WildStackerPlugin.log(" - Stacking barrels is " + getBoolean(barrelsStackingEnabled));

        WildStackerPlugin.log("Loading configuration done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    private String getBoolean(boolean bool){
        return bool ? "enabled" : "disabled";
    }

    private void loadLimits(KeyMap<Integer> limitsMap, ConfigurationSection configSection){
        if(configSection != null) {
            for (String type : configSection.getKeys(false)) {
                limitsMap.put(type, configSection.getInt(type));
            }
        }
    }

    private void loadCustomNames(WildStackerPlugin plugin){
        File file = new File(plugin.getDataFolder(), "custom-names.yml");

        if(!file.exists())
            plugin.saveResource("custom-names.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        for(String stringKey : cfg.getConfigurationSection("").getKeys(false)){
            if(cfg.isString(stringKey)){
                customNames.put(Key.of(stringKey), ChatColor.translateAlternateColorCodes('&', cfg.getString(stringKey)));
            }
        }

        if(!cfg.getBoolean("enabled", true))
            customNames.clear();
    }

    private void dataConvertor(YamlConfiguration cfg){
        if(cfg.contains("items.check-range"))
            cfg.set("items.merge-radius", cfg.getLong("items.check-range"));
        if(cfg.contains("items.save-interval"))
            cfg.set("save-interval", cfg.getLong("items.save-interval"));
        if(cfg.contains("items.blocked-materials"))
            cfg.set("items.blacklist", cfg.getStringList("items.blocked-materials"));
        if(cfg.contains("entities.check-range"))
            cfg.set("entities.merge-radius", cfg.getLong("entities.check-range"));
        if(cfg.contains("entities.reason-blacklist"))
            cfg.set("entities.spawn-blacklist", cfg.getStringList("entities.reason-blacklist"));
        if(cfg.contains("spawners.holograms.custom-name"))
            cfg.set("spawners.custom-name", cfg.getString("spawners.holograms.custom-name"));
        if(cfg.contains("spawners.holograms.enabled") && !cfg.getBoolean("spawners.holograms.enabled"))
            cfg.set("spawners.custom-name", "");
        if(cfg.contains("items.custom-display"))
            cfg.set("items.custom-display", null);
        if(cfg.getConfigurationSection("spawners.break-menu") == null)
            cfg.createSection("spawners.break-menu");
        if(cfg.isBoolean("buckets-stacker"))
            cfg.set("buckets-stacker.enabled", cfg.getBoolean("buckets-stacker"));
        if(cfg.contains("entities.spawn-blacklist") || !cfg.getBoolean("mythic-mobs-stack", true)){
            List<String> blacklisted = cfg.getStringList("entities.blacklist");
            if(cfg.contains("entities.spawn-blacklist"))
                blacklisted.addAll(cfg.getStringList("entities.spawn-blacklist"));
            if(!cfg.getBoolean("mythic-mobs-stack", true))
                blacklisted.add("MYTHIC_MOBS");
            cfg.set("entities.blacklist", blacklisted);
        }
    }

    public static void reload(){
        try{
            WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
            Field field = WildStackerPlugin.class.getDeclaredField("settingsHandler");
            field.setAccessible(true);
            field.set(plugin, new SettingsHandler(plugin));
        }catch(NoSuchFieldException | IllegalAccessException ex){
            ex.printStackTrace();
        }
    }

}
