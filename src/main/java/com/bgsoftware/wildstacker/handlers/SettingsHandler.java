package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackSplit;
import com.bgsoftware.wildstacker.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.key.Key;
import com.bgsoftware.wildstacker.key.KeyMap;
import com.bgsoftware.wildstacker.key.KeySet;
import com.bgsoftware.wildstacker.utils.Pair;
import com.bgsoftware.wildstacker.utils.entity.StackCheck;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.particles.ParticleWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler {

    //Global settings
    public final String giveItemName;
    public final ItemStack inspectTool, simulateTool;
    public final boolean deleteInvalidWorlds, deleteInvalidBlocks, killTaskStackedEntities, killTaskUnstackedEntities,
            killTaskStackedItems, killTaskUnstackedItems, killTaskSyncClearLagg;
    public final KeyMap<String> customNames;
    public final long killTaskInterval;
    public final List<String> killTaskWhitelist;
    public final List<String> killTaskBlacklist;
    public final List<String> killTaskWorlds;

    //Items settings
    public final boolean itemsStackingEnabled, itemsParticlesEnabled, itemsFixStackEnabled, itemsDisplayEnabled,
            itemsUnstackedCustomName, itemsNamesToggleEnabled, itemsSoundEnabled, itemsMaxPickupDelay;
    public final List<String> itemsDisabledWorlds;
    public final KeySet blacklistedItems, whitelistedItems;
    public final int itemsCheckRange, itemsChunkLimit;
    public final String itemsCustomName, itemsNamesToggleCommand;
    public final KeyMap<Integer> itemsLimits;
    public final float itemsSoundVolume, itemsSoundPitch;
    public final List<ParticleWrapper> itemsParticles;

    //Entities settings
    public final boolean entitiesStackingEnabled, entitiesParticlesEnabled, linkedEntitiesEnabled, stackDownEnabled,
            keepFireEnabled, mythicMobsCustomNameEnabled, stackAfterBreed, entitiesHideNames,
            entitiesNamesToggleEnabled, nextStackKnockback, eggLayMultiply, entitiesClearEquipment;
    public final long entitiesStackInterval;
    public final String entitiesCustomName, entitiesNamesToggleCommand;
    public final int entitiesCheckRange, linkedEntitiesMaxDistance, entitiesChunkLimit;
    public final List<String> entitiesDisabledWorlds, entitiesDisabledRegions, blacklistedEntities, whitelistedEntities,
            blacklistedEntitiesSpawnReasons, blacklistedEntitiesNames, entitiesInstantKills, entitiesNerfedWhitelist,
            entitiesNerfedBlacklist, entitiesNerfedWorlds, stackDownTypes, keepLowestHealth, entitiesAutoExpPickup;
    public final KeyMap<Integer> entitiesLimits, minimumEntitiesLimit, defaultUnstack;
    public final List<ParticleWrapper> entitiesParticles;

    //Spawners settings
    public final boolean spawnersStackingEnabled, perSpawnerLimit, spawnersParticlesEnabled, chunkMergeSpawners, explosionsBreakSpawnerStack,
            silkTouchSpawners, explosionsDropSpawner, explosionsDropToInventory, dropToInventory, shiftGetWholeSpawnerStack, getStackedItem,
            dropSpawnerWithoutSilk, floatingSpawnerNames, spawnersBreakMenu, spawnersPlaceMenu, spawnersPlacementPermission,
            spawnersShiftPlaceStack, changeUsingEggs, eggsStackMultiply, nextSpawnerPlacement, onlyOneSpawner;
    public final int spawnersCheckRange, explosionsBreakChance, explosionsAmountPercentage,
            silkTouchBreakChance, spawnersChunkLimit;
    public final List<String> spawnersDisabledWorlds, blacklistedSpawners, whitelistedSpawners, silkCustomLore, silkWorlds;
    public final String hologramCustomName, silkCustomName, spawnersPlaceMenuTitle;
    public final KeyMap<Integer> spawnersLimits;
    public final List<ParticleWrapper> spawnersParticles;
    public final KeyMap<Pair<Double, Boolean>> spawnersBreakCharge, spawnersPlaceCharge;

    //Barrels settings
    public final boolean barrelsStackingEnabled, barrelsParticlesEnabled, chunkMergeBarrels, explosionsBreakBarrelStack,
            barrelsToggleCommand, barrelsPlaceInventory, forceCauldron;
    public final int barrelsCheckRange, barrelsChunkLimit;
    public final String barrelsCustomName, barrelsToggleCommandSyntax, barrelsPlaceInventoryTitle, barrelsRequiredPermission;
    public final List<String> barrelsDisabledWorlds;
    public final KeySet blacklistedBarrels, whitelistedBarrels;
    public final KeyMap<Integer> barrelsLimits;
    public final List<ParticleWrapper> barrelsParticles;

    //Buckets settings
    public final boolean bucketsStackerEnabled;
    public final List<String> bucketsBlacklistedNames;
    public final int bucketsMaxStack;

    //Stews settings
    public final boolean stewsStackingEnabled;
    public final int stewsMaxStack;

    public SettingsHandler(WildStackerPlugin plugin){
        WildStackerPlugin.log("Loading configuration started...");
        long startTime = System.currentTimeMillis();
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        dataConvertor(cfg);

        cfg.syncWithConfig(file, plugin.getResource("config.yml"), "limits", "minimum-limits", "default-unstack", "break-slots", "fill-items", "break-charge", "place-charge");

        giveItemName = ChatColor.translateAlternateColorCodes('&', cfg.getString("give-item-name", "&e{0} &f{1} x{2}"));
        inspectTool = new ItemBuilder(Material.valueOf(cfg.getString("inspect-tool.type")), cfg.getInt("inspect-tool.data", 0))
                .withName(cfg.getString("inspect-tool.name"))
                .withLore(cfg.getStringList("inspect-tool.lore")).build();
        simulateTool = new ItemBuilder(Material.valueOf(cfg.getString("inspect-tool.type")), cfg.getInt("inspect-tool.data", 0))
                .withName(cfg.getString("simulate-tool.name"))
                .withLore(cfg.getStringList("simulate-tool.lore")).build();
        deleteInvalidWorlds = cfg.getBoolean("database.delete-invalid-worlds", false);
        deleteInvalidBlocks = cfg.getBoolean("database.delete-invalid-blocks", false);
        killTaskInterval = cfg.getLong("kill-task.interval", 300);
        killTaskStackedEntities = cfg.getBoolean("kill-task.stacked-entities", true);
        killTaskUnstackedEntities = cfg.getBoolean("kill-task.unstacked-entities", true);
        killTaskStackedItems = cfg.getBoolean("kill-task.stacked-items", true);
        killTaskUnstackedItems = cfg.getBoolean("kill-task.unstacked-items", true);
        killTaskSyncClearLagg = cfg.getBoolean("kill-task.sync-clear-lagg", false);
        killTaskWhitelist = cfg.getStringList("kill-task.whitelist");
        killTaskBlacklist = cfg.getStringList("kill-task.blacklist");
        killTaskWorlds = cfg.getStringList("kill-task.worlds");
        customNames = new KeyMap<>();
        loadCustomNames(plugin);

        itemsStackingEnabled = cfg.getBoolean("items.enabled", true);
        itemsParticlesEnabled = cfg.getBoolean("items.particles", true);
        itemsParticles = getParticles(plugin, "items");
        itemsDisabledWorlds = cfg.getStringList("items.disabled-worlds");
        itemsUnstackedCustomName = cfg.getBoolean("items.unstacked-custom-name", false);
        itemsFixStackEnabled = cfg.getBoolean("items.fix-stack", false);
        blacklistedItems = new KeySet(cfg.getStringList("items.blacklist"));
        whitelistedItems = new KeySet(cfg.getStringList("items.whitelist"));
        itemsCheckRange = cfg.getInt("items.merge-radius", 5);
        itemsChunkLimit = cfg.getInt("items.chunk-limit", 0);
        itemsCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("items.custom-name", "&6&lx{0} {1}"));
        itemsDisplayEnabled = cfg.getBoolean("items.item-display", false);
        itemsNamesToggleEnabled = cfg.getBoolean("items.names-toggle.enabled", false);
        itemsNamesToggleCommand = cfg.getString("items.names-toggle.command", "stacker names item");
        itemsSoundEnabled = cfg.getBoolean("items.pickup-sound.enabled", true);
        itemsSoundVolume = (float) cfg.getDouble("items.pickup-sound.volume");
        itemsSoundPitch = (float) cfg.getDouble("items.pickup-sound.pitch");
        itemsMaxPickupDelay = cfg.getBoolean("items.max-pickup-delay", false);

        entitiesStackingEnabled = cfg.getBoolean("entities.enabled", true);
        entitiesParticlesEnabled = cfg.getBoolean("entities.particles", true);
        entitiesParticles = getParticles(plugin, "entities");
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
        entitiesNerfedWhitelist = cfg.getStringList("entities.nerfed-entities.whitelist");
        entitiesNerfedBlacklist = cfg.getStringList("entities.nerfed-entities.blacklist");
        entitiesNerfedWorlds = cfg.getStringList("entities.nerfed-entities.worlds");
        stackDownEnabled = cfg.getBoolean("entities.stack-down.enabled", true);
        stackDownTypes = cfg.getStringList("entities.stack-down.stack-down-types");
        keepFireEnabled = cfg.getBoolean("entities.keep-fire", true);
        mythicMobsCustomNameEnabled = cfg.getBoolean("entities.mythic-mobs-custom-name", true);
        keepLowestHealth = cfg.getStringList("entities.keep-lowest-health");
        stackAfterBreed = cfg.getBoolean("entities.stack-after-breed", true);
        entitiesHideNames = cfg.getBoolean("entities.hide-names", false);
        entitiesNamesToggleEnabled = cfg.getBoolean("entities.names-toggle.enabled", false);
        entitiesNamesToggleCommand = cfg.getString("entities.names-toggle.command", "stacker names entity");
        nextStackKnockback = cfg.getBoolean("entities.next-stack-knockback", true);
        entitiesAutoExpPickup = cfg.getStringList("entities.auto-exp-pickup");
        eggLayMultiply = cfg.getBoolean("entities.egg-lay-multiply", true);
        entitiesClearEquipment = cfg.getBoolean("entities.clear-equipment", false);

        spawnersStackingEnabled = cfg.getBoolean("spawners.enabled", true);
        perSpawnerLimit = cfg.getBoolean("spawners.per-spawner-limit", false);
        spawnersParticlesEnabled = cfg.getBoolean("spawners.particles", true);
        spawnersParticles = getParticles(plugin, "spawners");
        spawnersDisabledWorlds = cfg.getStringList("spawners.disabled-worlds");
        spawnersCheckRange = cfg.getInt("spawners.merge-radius", 1);
        chunkMergeSpawners = cfg.getBoolean("spawners.chunk-merge", false);
        blacklistedSpawners = cfg.getStringList("spawners.blacklist");
        whitelistedSpawners = cfg.getStringList("spawners.whitelist");
        spawnersChunkLimit = cfg.getInt("spawners.chunk-limit", 0);
        hologramCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("spawners.custom-name", "&9&lx{0} {1}"));
        explosionsBreakSpawnerStack = cfg.getBoolean("spawners.explosions-break-stack", true);
        explosionsBreakChance = cfg.getInt("spawners.explosions-break-chance", 100);
        explosionsAmountPercentage = cfg.getInt("spawners.explosions-amount-percentage", 100);
        explosionsDropToInventory = cfg.getBoolean("spawners.explosions-drop-to-inventory", false);
        silkTouchBreakChance = cfg.getInt("spawners.silk-touch-break-chance", 100);
        dropSpawnerWithoutSilk = cfg.getBoolean("spawners.drop-without-silk", false);
        silkTouchSpawners = cfg.getBoolean("spawners.silk-spawners.enabled", true);
        silkCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("spawners.silk-spawners.custom-name", "&e{0} &fSpawner"));
        silkCustomLore = cfg.getStringList("spawners.silk-spawners.custom-lore").stream().map(line ->
                ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        explosionsDropSpawner = cfg.getBoolean("spawners.silk-spawners.explosions-drop-spawner", true);
        dropToInventory = cfg.getBoolean("spawners.silk-spawners.drop-to-inventory", true);
        silkWorlds = cfg.getStringList("spawners.silk-spawners.worlds");
        shiftGetWholeSpawnerStack = cfg.getBoolean("spawners.shift-get-whole-stack", true);
        getStackedItem = cfg.getBoolean("spawners.get-stacked-item", true);
        floatingSpawnerNames = cfg.getBoolean("spawners.floating-names", false);
        spawnersBreakMenu = cfg.getBoolean("spawners.break-menu.enabled", true);
        spawnersPlaceMenu = !spawnersBreakMenu && cfg.getBoolean("spawners.place-inventory.enabled", false);
        spawnersPlaceMenuTitle = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("spawners.place-inventory.title", "Add items here ({0})"));
        plugin.getBreakMenuHandler().loadMenu(cfg.getConfigurationSection("spawners.break-menu"));
        spawnersPlacementPermission = cfg.getBoolean("spawners.placement-permission", false);
        spawnersShiftPlaceStack = cfg.getBoolean("spawners.shift-place-stack", true);
        spawnersBreakCharge = new KeyMap<>();
        for(String key : cfg.getConfigurationSection("spawners.break-charge").getKeys(false)){
            ConfigurationSection mobSection = cfg.getConfigurationSection("spawners.break-charge." + key);
            double amount = mobSection.getDouble("price", 0.0);
            if(amount > 0) {
                spawnersBreakCharge.put(Key.of(key), new Pair<>(amount, mobSection.getBoolean("multiply-stack-amount", false)));
            }
        }
        spawnersPlaceCharge = new KeyMap<>();
        for(String key : cfg.getConfigurationSection("spawners.place-charge").getKeys(false)){
            ConfigurationSection mobSection = cfg.getConfigurationSection("spawners.place-charge." + key);
            double amount = mobSection.getDouble("price", 0.0);
            if(amount > 0) {
                spawnersBreakCharge.put(Key.of(key), new Pair<>(amount, mobSection.getBoolean("multiply-stack-amount", false)));
            }
        }
        changeUsingEggs = cfg.getBoolean("spawners.change-using-eggs", true);
        eggsStackMultiply = cfg.getBoolean("spawners.eggs-stack-multiply", true);
        nextSpawnerPlacement = cfg.getBoolean("spawners.next-spawner-placement", true);
        onlyOneSpawner = cfg.getBoolean("spawners.only-one-spawner", true);

        barrelsStackingEnabled = cfg.getBoolean("barrels.enabled", true);
        barrelsParticlesEnabled = cfg.getBoolean("barrels.particles", true);
        barrelsParticles = getParticles(plugin, "barrels");
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
        barrelsPlaceInventory = cfg.getBoolean("barrels.place-inventory.enabled", true);
        barrelsPlaceInventoryTitle = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("barrels.place-inventory.title", "Add items here ({0})"));
        forceCauldron = cfg.getBoolean("barrels.force-cauldron", false);
        barrelsRequiredPermission = cfg.getString("barrels.required-permission", "");

        bucketsStackerEnabled = cfg.getBoolean("buckets.enabled", true);
        bucketsBlacklistedNames = cfg.getStringList("buckets.name-blacklist");
        bucketsMaxStack = cfg.getInt("buckets.max-stack", 16);

        stewsStackingEnabled = cfg.getBoolean("stews.enabled", true);
        stewsMaxStack = cfg.getInt("stews.max-stack", 16);

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
        loadLimits((defaultUnstack = new KeyMap<>()), cfg.getConfigurationSection("entities.default-unstack"));

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

    private YamlConfiguration particlesYaml = null;

    private List<ParticleWrapper> getParticles(WildStackerPlugin plugin, String sectionPath){
        if(particlesYaml == null){
            File file = new File(plugin.getDataFolder(), "particles.yml");

            if(!file.exists())
                plugin.saveResource("particles.yml", false);

            particlesYaml = YamlConfiguration.loadConfiguration(file);
        }

        List<ParticleWrapper> particleWrappers = new ArrayList<>();
        ConfigurationSection section = particlesYaml.getConfigurationSection(sectionPath);

        if(section != null){
            for(String key : section.getKeys(false)){
                ConfigurationSection particleSection = section.getConfigurationSection(key);
                try {
                    particleWrappers.add(new ParticleWrapper(
                            particleSection.getString("type"),
                            particleSection.getInt("count", 0),
                            particleSection.getInt("offsetX", 0),
                            particleSection.getInt("offsetY", 0),
                            particleSection.getInt("offsetZ", 0),
                            particleSection.getDouble("extra", 0.0)
                    ));
                }catch(IllegalArgumentException ex){
                    WildStackerPlugin.log("Particle " + sectionPath + "." + key + " is missing 'type'.");
                }
            }
        }

        return particleWrappers;
    }

    private void dataConvertor(YamlConfiguration cfg){
        if(cfg.contains("items.kill-all"))
            cfg.set("kill-task.stacked-items", cfg.getBoolean("items.kill-all"));
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
        if(cfg.contains("entities.kill-all.interval"))
            cfg.set("kill-task.interval", cfg.getLong("entities.kill-all.interval"));
        if(cfg.contains("entities.kill-all.clear-lagg"))
            cfg.set("kill-task.sync-clear-lagg", cfg.getBoolean("entities.kill-all.clear-lagg"));
        if(cfg.isBoolean("entities.keep-lowest-health")){
            if(cfg.getBoolean("entities.keep-lowest-health"))
                cfg.set("entities.keep-lowest-health", Collections.singletonList("all"));
            else
                cfg.set("entities.keep-lowest-health", new ArrayList<>());
        }
        if(cfg.contains("spawners.holograms.custom-name"))
            cfg.set("spawners.custom-name", cfg.getString("spawners.holograms.custom-name"));
        if(cfg.contains("spawners.holograms.enabled") && !cfg.getBoolean("spawners.holograms.enabled"))
            cfg.set("spawners.custom-name", "");
        if(cfg.contains("items.custom-display"))
            cfg.set("items.custom-display", null);
        if(cfg.getConfigurationSection("spawners.break-menu") == null)
            cfg.createSection("spawners.break-menu");
        if(cfg.isBoolean("spawners.place-inventory"))
            cfg.set("spawners.place-inventory.enabled", cfg.getBoolean("spawners.place-inventory"));
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
        if(cfg.isBoolean("barrels.place-inventory"))
            cfg.set("barrels.place-inventory.enabled", cfg.getBoolean("barrels.place-inventory"));
        if(cfg.contains("items.buckets-stacker.enabled"))
            cfg.set("buckets.enabled", cfg.getBoolean("items.buckets-stacker.enabled"));
        if(cfg.contains("items.buckets-stacker.name-blacklist"))
            cfg.set("buckets.name-blacklist", cfg.getStringList("items.buckets-stacker.name-blacklist"));
        if(cfg.contains("items.buckets-stacker.max-stack"))
            cfg.set("buckets.max-stack", cfg.getInt("items.buckets-stacker.max-stack"));
        if(cfg.contains("entities.nerfed-spawning"))
            cfg.set("entities.nerfed-entities.whitelist", cfg.getStringList("entities.nerfed-spawning"));
        if(cfg.contains("entities.nerfed-worlds"))
            cfg.set("entities.nerfed-entities.worlds", cfg.getStringList("entities.nerfed-worlds"));
        if(cfg.contains("spawners.break-charge.amount")){
            List<String> mobs = cfg.getStringList("spawners.break-charge.whitelist");
            if(mobs.isEmpty()) {
                mobs.add("EXAMPLE_MOB");
            }

            for (String mob : mobs) {
                cfg.set("spawners.break-charge." + mob + ".price", cfg.getInt("spawners.break-charge.amount"));
                cfg.set("spawners.break-charge." + mob + ".multiply-stack-amount", cfg.getBoolean("spawners.break-charge.multiply-stack-amount"));
            }

            cfg.set("spawners.break-charge.amount", null);
            cfg.set("spawners.break-charge.multiply-stack-amount", null);
            cfg.set("spawners.break-charge.whitelist", null);
        }
        if(cfg.contains("spawners.place-charge.amount")){
            List<String> mobs = cfg.getStringList("spawners.place-charge.whitelist");
            if(mobs.isEmpty()) {
                mobs.add("EXAMPLE_MOB");
            }

            for (String mob : mobs) {
                cfg.set("spawners.place-charge." + mob + ".price", cfg.getInt("spawners.place-charge.amount"));
                cfg.set("spawners.place-charge." + mob + ".multiply-stack-amount", cfg.getBoolean("spawners.place-charge.multiply-stack-amount"));
            }

            cfg.set("spawners.place-charge.amount", null);
            cfg.set("spawners.place-charge.multiply-stack-amount", null);
            cfg.set("spawners.place-charge.whitelist", null);
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
