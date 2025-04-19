package com.bgsoftware.wildstacker.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.data.structures.IFastEnumMap;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackSplit;
import com.bgsoftware.wildstacker.api.objects.*;
import com.bgsoftware.wildstacker.api.particles.ParticleEffect;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.errors.ManagerLoadException;
import com.bgsoftware.wildstacker.menu.SpawnerAmountsMenu;
import com.bgsoftware.wildstacker.menu.SpawnerUpgradeMenu;
import com.bgsoftware.wildstacker.menu.SpawnersManageMenu;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.data.structures.*;
import com.bgsoftware.wildstacker.utils.entity.StackCheck;
import com.bgsoftware.wildstacker.utils.files.FileUtils;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.names.NameBuilder;
import com.bgsoftware.wildstacker.utils.names.NamePlaceholder;
import com.bgsoftware.wildstacker.utils.particles.ParticleWrapper;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SettingsContainer {

    public final Pattern SPAWNERS_PATTERN;

    //Global settings
    public final String giveItemName, killTaskTimeCommand;
    public final ItemStack inspectTool, simulateTool;
    public final boolean deleteInvalidWorlds, killTaskStackedEntities, killTaskUnstackedEntities,
            killTaskStackedItems, killTaskUnstackedItems, killTaskSyncClearLagg;

    public final String databaseType;
    public final String databaseMySQLAddress;
    public final int databaseMySQLPort;
    public final String databaseMySQLDBName;
    public final String databaseMySQLUsername;
    public final String databaseMySQLPassword;
    public final String databaseMySQLPrefix;
    public final boolean databaseMySQLSSL;
    public final boolean databaseMySQLPublicKeyRetrieval;
    public final long databaseMySQLWaitTimeout;
    public final long databaseMySQLMaxLifetime;

    public final Map<String, String> customNames;
    public final long killTaskInterval;
    public final Fast2EnumsArray<EntityType, SpawnCause> killTaskEntitiesWhitelist, killTaskEntitiesBlacklist;
    public final FastEnumArray<Material> killTaskItemsWhitelist, killTaskItemsBlacklist;
    public final List<String> killTaskEntitiesWorlds, killTaskItemsWorlds;

    //Items settings
    public final boolean itemsStackingEnabled, itemsParticlesEnabled, itemsFixStackEnabled, itemsDisplayEnabled,
            itemsUnstackedCustomName, itemsNamesToggleEnabled, itemsSoundEnabled, itemsMaxPickupDelay, storeItems;
    public final List<String> itemsDisabledWorlds;
    public final FastEnumArray<Material> blacklistedItems, whitelistedItems;
    public final int itemsChunkLimit;
    public final String itemsCustomName, itemsNamesToggleCommand;
    public final NameBuilder<StackedItem> itemsNameBuilder;
    public final FastEnumMap<Material, Integer> itemsMergeRadius;
    public final FastEnumMap<Material, Integer> itemsLimits;
    public final List<ParticleWrapper> itemsParticles;
    public final long itemsStackInterval;

    //Entities settings
    public final boolean entitiesStackingEnabled, entitiesParticlesEnabled, linkedEntitiesEnabled, nerfedEntitiesTeleport,
            stackDownEnabled, keepFireEnabled, mythicMobsCustomNameEnabled, stackAfterBreed, smartBreedingEnabled,
            smartBreedingConsumeEntireInventory, entitiesHideNames, entitiesNamesToggleEnabled, entitiesFastKill,
            eggLayMultiply, scuteMultiply, entitiesClearEquipment, spawnCorpses, entitiesOneShotEnabled, storeEntities,
            superiorSkyblockHook, multiplyDrops, multiplyExp, spreadDamage, entitiesFillVehicles;
    public final long entitiesStackInterval;
    public final String entitiesCustomName, entitiesNamesToggleCommand;
    public final NameBuilder<StackedEntity> entitiesNameBuilder;
    public final Sound entitiesExpPickupSound;
    public final int linkedEntitiesMaxDistance, entitiesChunkLimit;
    public final Fast2EnumsArray<EntityType, SpawnCause> blacklistedEntities, whitelistedEntities, entitiesNerfedWhitelist,
            entitiesNerfedBlacklist, stackDownTypes, keepLowestHealth, entitiesAutoExpPickup, entitiesOneShotWhitelist;
    public final Fast3EnumsArray<EntityType, SpawnCause, EntityDamageEvent.DamageCause> entitiesInstantKills;
    public final List<String> entitiesDisabledWorlds, entitiesDisabledRegions, entitiesNerfedWorlds, entitiesOneShotTools,
            entitiesFilteredTransforms;
    public final List<Pattern> blacklistedEntitiesNames;
    public final Fast2EnumsMap<EntityType, SpawnCause, Integer> entitiesMergeRadius, entitiesLimits,
            minimumRequiredEntities, defaultUnstack;
    public final List<ParticleWrapper> entitiesParticles;

    //Spawners settings
    public final boolean spawnersStackingEnabled, perSpawnerLimit, spawnersParticlesEnabled, chunkMergeSpawners,
            silkTouchSpawners, explosionsDropSpawner, explosionsDropToInventory, dropToInventory, shiftGetWholeSpawnerStack,
            getStackedItem, dropSpawnerWithoutSilk, spawnersMineRequireSilk, floatingSpawnerNames, spawnersPlacementPermission,
            spawnersShiftPlaceStack, changeUsingEggs, eggsStackMultiply, nextSpawnerPlacement, onlyOneSpawner, inventoryTweaksEnabled,
            sneakingOpenMenu, amountsMenuEnabled, upgradeMenuEnabled, manageMenuEnabled, spawnersOverrideEnabled,
            spawnerUpgradesMultiplyStackAmount, listenPaperPreSpawnEvent, spawnersUnstackedCustomName;
    public final int explosionsBreakChance, explosionsBreakPercentage, explosionsBreakMinimum, explosionsAmountPercentage,
            explosionsAmountMinimum, silkTouchBreakChance, silkTouchMinimumLevel, spawnersChunkLimit;
    public final List<String> spawnersDisabledWorlds, spawnerItemLore, silkWorlds, explosionsWorlds;
    public final FastEnumArray<EntityType> blacklistedSpawners, whitelistedSpawners;
    public final String spawnersCustomName, spawnerItemName, inventoryTweaksPermission, inventoryTweaksCommand;
    public final NameBuilder<StackedSpawner> spawnersNameBuilder;
    public final FastEnumMap<EntityType, Integer> spawnersMergeRadius, spawnersLimits;
    public final List<ParticleWrapper> spawnersParticles;
    public final FastEnumMap<EntityType, Pair<Double, Boolean>> spawnersBreakCharge, spawnersPlaceCharge;

    //Barrels settings
    public final boolean barrelsStackingEnabled, barrelsParticlesEnabled, chunkMergeBarrels, explosionsBreakBarrelStack,
            barrelsToggleCommand, barrelsPlaceInventory, forceCauldron, barrelsAutoPickup, dropStackedItem, barrelsShiftPlaceStack;
    public final int barrelsChunkLimit;
    public final String barrelsCustomName, barrelsToggleCommandSyntax, barrelsPlaceInventoryTitle, barrelsRequiredPermission;
    public final NameBuilder<StackedBarrel> barrelsNameBuilder;
    public final List<String> barrelsDisabledWorlds;
    public final FastEnumArray<Material> blacklistedBarrels, whitelistedBarrels;
    public final FastEnumMap<Material, Integer> barrelsMergeRadius, barrelsLimits;
    public final List<ParticleWrapper> barrelsParticles;

    //Buckets settings
    public final boolean bucketsStackerEnabled;
    public final List<String> bucketsBlacklistedNames;
    public final int bucketsMaxStack;

    //Stews settings
    public final boolean stewsStackingEnabled;
    public final int stewsMaxStack;
    private YamlConfiguration particlesYaml = null;

    public SettingsContainer(WildStackerPlugin plugin, YamlConfiguration config) {

        long startTime = System.currentTimeMillis();
        
        giveItemName = ChatColor.translateAlternateColorCodes('&', config.getString("give-item-name", "&6x{0} &f&o{1} {2}"));
        SPAWNERS_PATTERN = Pattern.compile(giveItemName
                .replace("{0}", "(.*)")
                .replace("{1}", "(.*)")
                .replace("{2}", "(.*)")
        );
        inspectTool = new ItemBuilder(Material.valueOf(config.getString("inspect-tool.type")), config.getInt("inspect-tool.data", 0))
                .withName(config.getString("inspect-tool.name"))
                .withLore(config.getStringList("inspect-tool.lore")).build();
        simulateTool = new ItemBuilder(Material.valueOf(config.getString("inspect-tool.type")), config.getInt("inspect-tool.data", 0))
                .withName(config.getString("simulate-tool.name"))
                .withLore(config.getStringList("simulate-tool.lore")).build();
        deleteInvalidWorlds = config.getBoolean("database.delete-invalid-worlds", false);

        databaseType = config.getString("database.type", "SQLite").toUpperCase(Locale.ENGLISH);
        databaseMySQLAddress = config.getString("database.address", "localhost");
        databaseMySQLPort = config.getInt("database.port", 3306);
        databaseMySQLDBName = config.getString("database.db-name", "wildstacker");
        databaseMySQLUsername = config.getString("database.user-name", "root");
        databaseMySQLPassword = config.getString("database.password", "");
        databaseMySQLPrefix = config.getString("database.prefix", "");
        databaseMySQLSSL = config.getBoolean("database.useSSL", false);
        databaseMySQLPublicKeyRetrieval = config.getBoolean("database.allowPublicKeyRetrieval", true);
        databaseMySQLWaitTimeout = config.getLong("database.waitTimeout", 600000);
        databaseMySQLMaxLifetime = config.getLong("database.maxLifetime", 1800000);


        killTaskInterval = config.getLong("kill-task.interval", 300);
        killTaskStackedEntities = config.getBoolean("kill-task.stacked-entities", true);
        killTaskUnstackedEntities = config.getBoolean("kill-task.unstacked-entities", true);
        killTaskStackedItems = config.getBoolean("kill-task.stacked-items", true);
        killTaskUnstackedItems = config.getBoolean("kill-task.unstacked-items", true);
        killTaskSyncClearLagg = config.getBoolean("kill-task.sync-clear-lagg", false);
        killTaskTimeCommand = config.getString("kill-task.time-command", "stacker timeleft");
        killTaskEntitiesWhitelist = Fast2EnumsArray.fromList(config.getStringList("kill-task.kill-entities.whitelist"),
                EntityType.class, SpawnCause.class);
        killTaskEntitiesBlacklist = Fast2EnumsArray.fromList(config.getStringList("kill-task.kill-entities.blacklist"),
                EntityType.class, SpawnCause.class);
        killTaskEntitiesWorlds = config.getStringList("kill-task.kill-entities.worlds");
        killTaskItemsWhitelist = FastEnumArray.fromList(config.getStringList("kill-task.kill-items.whitelist"), Material.class);
        killTaskItemsBlacklist = FastEnumArray.fromList(config.getStringList("kill-task.kill-items.blacklist"), Material.class);
        killTaskItemsWorlds = config.getStringList("kill-task.kill-items.worlds");
        customNames = new HashMap<>();
        loadCustomNames(plugin);

        itemsStackingEnabled = config.getBoolean("items.enabled", true);
        itemsMergeRadius = FastEnumMap.fromSection(config.getConfigurationSection("items.merge-radius"), Material.class);
        itemsParticlesEnabled = config.getBoolean("items.particles", true);
        itemsParticles = getParticles(plugin, "items");
        itemsDisabledWorlds = config.getStringList("items.disabled-worlds");
        itemsLimits = FastEnumMap.fromSection(config.getConfigurationSection("items.limits"), Material.class);
        itemsUnstackedCustomName = config.getBoolean("items.unstacked-custom-name", false);
        itemsFixStackEnabled = config.getBoolean("items.fix-stack", false);
        blacklistedItems = FastEnumArray.fromList(config.getStringList("items.blacklist"), Material.class);
        whitelistedItems = FastEnumArray.fromList(config.getStringList("items.whitelist"), Material.class);
        itemsChunkLimit = config.getInt("items.chunk-limit", 0);
        itemsCustomName = ChatColor.translateAlternateColorCodes('&', config.getString("items.custom-name", "&6&lx{0} {1}"));
        //noinspection unchecked
        itemsNameBuilder = new NameBuilder<>(itemsCustomName,
                new NamePlaceholder<>("{0}", stackedItem -> stackedItem.getStackAmount() + ""),
                new NamePlaceholder<>("{1}", stackedItem -> ((WStackedItem) stackedItem).getCachedDisplayName()),
                new NamePlaceholder<>("{2}", stackedItem -> ((WStackedItem) stackedItem).getCachedDisplayName().toUpperCase())
        );
        itemsDisplayEnabled = config.getBoolean("items.item-display", false);
        itemsNamesToggleEnabled = config.getBoolean("items.names-toggle.enabled", false);
        itemsNamesToggleCommand = config.getString("items.names-toggle.command", "stacker names item");
        itemsSoundEnabled = config.getBoolean("items.pickup-sound", true);
        itemsMaxPickupDelay = config.getBoolean("items.max-pickup-delay", false);
        itemsStackInterval = config.getLong("items.stack-interval", 0L);
        storeItems = config.getBoolean("items.store-items", true);

        entitiesStackingEnabled = config.getBoolean("entities.enabled", true);
        entitiesMergeRadius = Fast2EnumsMap.fromSectionToInt(config.getConfigurationSection("entities.merge-radius"),
                EntityType.class, SpawnCause.class);
        entitiesParticlesEnabled = config.getBoolean("entities.particles", true);
        entitiesParticles = getParticles(plugin, "entities");
        entitiesStackInterval = config.getLong("entities.stack-interval", 0);
        entitiesDisabledWorlds = config.getStringList("entities.disabled-worlds");
        entitiesLimits = Fast2EnumsMap.fromSectionToInt(config.getConfigurationSection("entities.limits"),
                EntityType.class, SpawnCause.class);
        minimumRequiredEntities = Fast2EnumsMap.fromSectionToInt(config.getConfigurationSection("entities.minimum-required"),
                EntityType.class, SpawnCause.class);
        entitiesCustomName = ChatColor.translateAlternateColorCodes('&', config.getString("entities.custom-name", "&d&lx{0} {1}"));
        //noinspection unchecked
        entitiesNameBuilder = new NameBuilder<>(entitiesCustomName,
                new NamePlaceholder<>("{0}", stackedEntity -> stackedEntity.getStackAmount() + ""),
                new NamePlaceholder<>("{1}", stackedEntity -> ((WStackedEntity) stackedEntity).getCachedDisplayName()),
                new NamePlaceholder<>("{2}", stackedEntity -> ((WStackedEntity) stackedEntity).getCachedDisplayName().toUpperCase()),
                new NamePlaceholder<>("{3}", stackedEntity -> stackedEntity.getUpgrade().getDisplayName())
        );
        entitiesChunkLimit = config.getInt("entities.chunk-limit", 0);
        entitiesDisabledRegions = config.getStringList("entities.disabled-regions");
        linkedEntitiesEnabled = config.getBoolean("entities.linked-entities.enabled", true);
        nerfedEntitiesTeleport = config.getBoolean("entities.nerfed-entities.teleport", false);
        linkedEntitiesMaxDistance = config.getInt("entities.linked-entities.max-distance", 10);
        blacklistedEntities = Fast2EnumsArray.fromList(config.getStringList("entities.blacklist"),
                EntityType.class, SpawnCause.class);
        whitelistedEntities = Fast2EnumsArray.fromList(config.getStringList("entities.whitelist"),
                EntityType.class, SpawnCause.class);
        blacklistedEntitiesNames = config.getStringList("entities.name-blacklist").stream()
                .map(line -> Pattern.compile(ChatColor.translateAlternateColorCodes('&', line)))
                .collect(Collectors.toList());
        entitiesInstantKills = Fast3EnumsArray.fromList(config.getStringList("entities.instant-kill"),
                EntityType.class, SpawnCause.class, EntityDamageEvent.DamageCause.class);
        entitiesNerfedWhitelist = Fast2EnumsArray.fromList(config.getStringList("entities.nerfed-entities.whitelist"),
                EntityType.class, SpawnCause.class);
        entitiesNerfedBlacklist = Fast2EnumsArray.fromList(config.getStringList("entities.nerfed-entities.blacklist"),
                EntityType.class, SpawnCause.class);
        entitiesNerfedWorlds = config.getStringList("entities.nerfed-entities.worlds");
        stackDownEnabled = config.getBoolean("entities.stack-down.enabled", true);
        stackDownTypes = Fast2EnumsArray.fromList(config.getStringList("entities.stack-down.stack-down-types"),
                EntityType.class, SpawnCause.class);
        keepFireEnabled = config.getBoolean("entities.keep-fire", true);
        mythicMobsCustomNameEnabled = config.getBoolean("entities.mythic-mobs-custom-name", true);
        keepLowestHealth = Fast2EnumsArray.fromList(config.getStringList("entities.keep-lowest-health"),
                EntityType.class, SpawnCause.class);
        stackAfterBreed = config.getBoolean("entities.stack-after-breed", true);
        smartBreedingEnabled = config.getBoolean("entities.smart-breeding.enabled", false);
        smartBreedingConsumeEntireInventory = config.getBoolean("entities.smart-breeding.consume-entire-inventory", false);
        entitiesHideNames = config.getBoolean("entities.hide-names", false);
        entitiesNamesToggleEnabled = config.getBoolean("entities.names-toggle.enabled", false);
        entitiesNamesToggleCommand = config.getString("entities.names-toggle.command", "stacker names entity");
        entitiesFastKill = config.getBoolean("entities.fast-kill", true);
        defaultUnstack = Fast2EnumsMap.fromSectionToInt(config.getConfigurationSection("entities.default-unstack"),
                EntityType.class, SpawnCause.class);
        entitiesAutoExpPickup = Fast2EnumsArray.fromList(config.getStringList("entities.auto-exp-pickup"),
                EntityType.class, SpawnCause.class);
        Sound entitiesExpPickupSound;
        try {
            entitiesExpPickupSound = Sound.valueOf(config.getString("entities.exp-pickup-sound"));
        } catch (Exception ignored) {
            entitiesExpPickupSound = null;
        }
        this.entitiesExpPickupSound = entitiesExpPickupSound;
        eggLayMultiply = config.getBoolean("entities.egg-lay-multiply", true);
        scuteMultiply = config.getBoolean("entities.scute-multiply", true);
        entitiesClearEquipment = config.getBoolean("entities.clear-equipment", false);
        spawnCorpses = config.getBoolean("entities.spawn-corpses", true);
        entitiesOneShotEnabled = config.getBoolean("entities.one-shot.enabled", false);
        entitiesOneShotTools = config.getStringList("entities.one-shot.tools");
        entitiesOneShotWhitelist = Fast2EnumsArray.fromList(config.getStringList("entities.one-shot.whitelist"),
                EntityType.class, SpawnCause.class);
        storeEntities = config.getBoolean("entities.store-entities", true);
        superiorSkyblockHook = config.getBoolean("entities.superiorskyblock-hook", false);
        multiplyDrops = config.getBoolean("entities.multiply-drops", true);
        multiplyExp = config.getBoolean("entities.multiply-exp", true);
        spreadDamage = config.getBoolean("entities.spread-damage", false);
        entitiesFilteredTransforms = config.getStringList("entities.filtered-transforms");
        entitiesFillVehicles = config.getBoolean("entities.entities-fill-vehicles");

        spawnersStackingEnabled = config.getBoolean("spawners.enabled", true);
        spawnersMergeRadius = FastEnumMap.fromSection(config.getConfigurationSection("spawners.merge-radius"), EntityType.class);
        perSpawnerLimit = config.getBoolean("spawners.per-spawner-limit", false);
        spawnersParticlesEnabled = config.getBoolean("spawners.particles", true);
        spawnersParticles = getParticles(plugin, "spawners");
        spawnersDisabledWorlds = config.getStringList("spawners.disabled-worlds");
        spawnersLimits = FastEnumMap.fromSection(config.getConfigurationSection("spawners.limits"), EntityType.class);
        chunkMergeSpawners = config.getBoolean("spawners.chunk-merge", false);
        blacklistedSpawners = FastEnumArray.fromList(config.getStringList("spawners.blacklist"), EntityType.class);
        whitelistedSpawners = FastEnumArray.fromList(config.getStringList("spawners.whitelist"), EntityType.class);
        spawnersChunkLimit = config.getInt("spawners.chunk-limit", 0);
        spawnersCustomName = ChatColor.translateAlternateColorCodes('&', config.getString("spawners.custom-name", "&9&lx{0} {1}"));
        //noinspection unchecked
        spawnersNameBuilder = new NameBuilder<>(spawnersCustomName,
                new NamePlaceholder<>("{0}", stackedSpawner -> stackedSpawner.getStackAmount() + ""),
                new NamePlaceholder<>("{1}", stackedSpawner -> ((WStackedSpawner) stackedSpawner).getCachedDisplayName()),
                new NamePlaceholder<>("{2}", stackedSpawner -> ((WStackedSpawner) stackedSpawner).getCachedDisplayName().toUpperCase()),
                new NamePlaceholder<>("{3}", stackedSpawner -> stackedSpawner.getUpgrade().getDisplayName())
        );
        spawnerItemName = ChatColor.translateAlternateColorCodes('&', config.getString("spawners.spawner-item.name", "&e{0} &fSpawner"));
        spawnerItemLore = config.getStringList("spawners.spawner-item.lore").stream().map(line ->
                ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        silkTouchSpawners = config.getBoolean("spawners.silk-touch.enabled", true);
        dropToInventory = config.getBoolean("spawners.silk-touch.drop-to-inventory", true);
        silkWorlds = config.getStringList("spawners.silk-touch.worlds");
        dropSpawnerWithoutSilk = config.getBoolean("spawners.silk-touch.drop-without-silk", false);
        silkTouchBreakChance = config.getInt("spawners.silk-touch.break-chance", 100);
        silkTouchMinimumLevel = config.getInt("spawners.silk-touch.minimum-level", 1);
        explosionsDropSpawner = config.getBoolean("spawners.explosions.enabled", true);
        explosionsDropToInventory = config.getBoolean("spawners.explosions.drop-to-inventory", false);
        explosionsWorlds = config.getStringList("spawners.explosions.worlds");
        explosionsBreakChance = config.getInt("spawners.explosions.break-chance", 100);
        explosionsBreakPercentage = config.getInt("spawners.explosions-break-percentage", 100);
        explosionsBreakMinimum = config.getInt("spawners.explosions-break-minimum", 1);
        explosionsAmountPercentage = config.getInt("spawners.explosions-amount-percentage", 100);
        explosionsAmountMinimum = config.getInt("spawners.explosions-amount-minimum", 1);
        spawnersMineRequireSilk = config.getBoolean("spawners.mine-require-silk", false);
        shiftGetWholeSpawnerStack = config.getBoolean("spawners.shift-get-whole-stack", true);
        getStackedItem = config.getBoolean("spawners.drop-stacked-item", true);
        floatingSpawnerNames = config.getBoolean("spawners.floating-names", false);
        spawnersPlacementPermission = config.getBoolean("spawners.placement-permission", false);
        spawnersShiftPlaceStack = config.getBoolean("spawners.shift-place-stack", true);
        spawnersBreakCharge = new FastEnumMap<>(EntityType.class);
        for (String key : config.getConfigurationSection("spawners.break-charge").getKeys(false)) {
            ConfigurationSection mobSection = config.getConfigurationSection("spawners.break-charge." + key);

            double amount = mobSection.getDouble("price", 0.0);
            if (amount <= 0)
                continue;

            if (key.equalsIgnoreCase("ALL")) {
                spawnersBreakCharge.putGlobal(new Pair<>(amount, mobSection.getBoolean("multiply-stack-amount", false)));
            } else {
                EntityType entityType;

                try {
                    entityType = EntityType.valueOf(key.toUpperCase());
                } catch (Exception ex) {
                    continue;
                }

                spawnersBreakCharge.put(entityType, new Pair<>(amount, mobSection.getBoolean("multiply-stack-amount", false)));
            }
        }
        spawnersPlaceCharge = new FastEnumMap<>(EntityType.class);
        for (String key : config.getConfigurationSection("spawners.place-charge").getKeys(false)) {
            ConfigurationSection mobSection = config.getConfigurationSection("spawners.place-charge." + key);

            double amount = mobSection.getDouble("price", 0.0);
            if (amount <= 0)
                continue;

            if (key.equalsIgnoreCase("ALL")) {
                spawnersPlaceCharge.putGlobal(new Pair<>(amount, mobSection.getBoolean("multiply-stack-amount", false)));
            } else {
                EntityType entityType;

                try {
                    entityType = EntityType.valueOf(key.toUpperCase());
                } catch (Exception ex) {
                    continue;
                }

                spawnersPlaceCharge.put(entityType, new Pair<>(amount, mobSection.getBoolean("multiply-stack-amount", false)));
            }
        }
        changeUsingEggs = config.getBoolean("spawners.change-using-eggs", true);
        eggsStackMultiply = config.getBoolean("spawners.eggs-stack-multiply", true);
        nextSpawnerPlacement = config.getBoolean("spawners.next-spawner-placement", true);
        onlyOneSpawner = config.getBoolean("spawners.only-one-spawner", true);
        inventoryTweaksEnabled = config.getBoolean("spawners.inventory-tweaks.enabled", true);
        inventoryTweaksPermission = config.getString("spawners.inventory-tweaks.permission", "");
        inventoryTweaksCommand = config.getString("spawners.inventory-tweaks.toggle-command", "stacker inventorytweaks,stacker it");
        spawnersOverrideEnabled = spawnersStackingEnabled && config.getBoolean("spawners.spawners-override.enabled");
        sneakingOpenMenu = config.getBoolean("spawners.manage-menu.sneaking-open-menu");
        amountsMenuEnabled = config.getBoolean("spawners.manage-menu.amounts-menu");
        upgradeMenuEnabled = config.getBoolean("spawners.manage-menu.upgrade-menu");
        manageMenuEnabled = amountsMenuEnabled || upgradeMenuEnabled;
        spawnerUpgradesMultiplyStackAmount = config.getBoolean("spawners.spawner-upgrades.multiply-stack-amount", true);

        boolean listenPaperPreSpawnEvent;
        try {
            Class.forName("com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent");
            listenPaperPreSpawnEvent = config.getBoolean("spawners.listen-paper-pre-spawn-event");
        } catch (ClassNotFoundException error) {
            listenPaperPreSpawnEvent = false;
        }

        this.listenPaperPreSpawnEvent = listenPaperPreSpawnEvent;
        spawnersUnstackedCustomName = config.getBoolean("spawners.unstacked-custom-name");

        barrelsStackingEnabled = ServerVersion.isAtLeast(ServerVersion.v1_8) && config.getBoolean("barrels.enabled", true);
        barrelsMergeRadius = FastEnumMap.fromSection(config.getConfigurationSection("barrels.merge-radius"), Material.class);
        barrelsParticlesEnabled = config.getBoolean("barrels.particles", true);
        barrelsParticles = getParticles(plugin, "barrels");
        barrelsDisabledWorlds = config.getStringList("barrels.disabled-worlds");
        barrelsLimits = FastEnumMap.fromSection(config.getConfigurationSection("barrels.limits"), Material.class);
        chunkMergeBarrels = config.getBoolean("barrels.chunk-merge", false);
        barrelsCustomName = ChatColor.translateAlternateColorCodes('&', config.getString("barrels.custom-name", "&9&lx{0} {1}"));
        //noinspection unchecked
        barrelsNameBuilder = new NameBuilder<>(barrelsCustomName,
                new NamePlaceholder<>("{0}", stackedBarrel -> stackedBarrel.getStackAmount() + ""),
                new NamePlaceholder<>("{1}", stackedBarrel -> ((WStackedBarrel) stackedBarrel).getCachedDisplayName()),
                new NamePlaceholder<>("{2}", stackedBarrel -> ((WStackedBarrel) stackedBarrel).getCachedDisplayName().toUpperCase())
        );
        blacklistedBarrels = FastEnumArray.fromList(config.getStringList("barrels.blacklist"), Material.class);
        whitelistedBarrels = FastEnumArray.fromList(config.getStringList("barrels.whitelist"), Material.class);
        barrelsChunkLimit = config.getInt("barrels.chunk-limit", 0);
        explosionsBreakBarrelStack = config.getBoolean("barrels.explosions-break-stack", true);
        barrelsToggleCommand = config.getBoolean("barrels.toggle-command.enabled", false);
        barrelsToggleCommandSyntax = config.getString("barrels.toggle-command.command", "stacker toggle");
        barrelsPlaceInventory = config.getBoolean("barrels.place-inventory.enabled", true);
        barrelsPlaceInventoryTitle = ChatColor.translateAlternateColorCodes('&',
                config.getString("barrels.place-inventory.title", "Add items here ({0})"));
        forceCauldron = config.getBoolean("barrels.force-cauldron", false);
        barrelsRequiredPermission = config.getString("barrels.required-permission", "");
        barrelsAutoPickup = config.getBoolean("barrels.auto-pickup", false);
        dropStackedItem = config.getBoolean("barrels.drop-stacked-item", false);
        barrelsShiftPlaceStack = config.getBoolean("barrels.shift-place-stack", false);

        bucketsStackerEnabled = config.getBoolean("buckets.enabled", true);
        bucketsBlacklistedNames = config.getStringList("buckets.name-blacklist").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        bucketsMaxStack = config.getInt("buckets.max-stack", 16);

        stewsStackingEnabled = config.getBoolean("stews.enabled", true);
        stewsMaxStack = config.getInt("stews.max-stack", 16);

        for (StackCheck check : StackCheck.values()) {
            check.setEnabled(config.getBoolean("entities.stack-checks." + check.name(), false));
        }

        for (StackSplit split : StackSplit.values()) {
            split.setEnabled(config.getBoolean("entities.stack-split." + split.name(), false));
        }

        WildStackerPlugin.log(" - Stacking drops is " + getBoolean(itemsStackingEnabled));
        WildStackerPlugin.log(" - Stacking entities is " + getBoolean(entitiesStackingEnabled));
        WildStackerPlugin.log(" - Stacking spawners is " + getBoolean(spawnersStackingEnabled));
        WildStackerPlugin.log(" - Stacking barrels is " + getBoolean(barrelsStackingEnabled));

        SpawnersManageMenu.loadMenu();
        SpawnerAmountsMenu.loadMenu();
        SpawnerUpgradeMenu.loadMenu();

        WildStackerPlugin.log("Loading configuration done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    private String getBoolean(boolean bool) {
        return bool ? "enabled" : "disabled";
    }


    private void loadCustomNames(WildStackerPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "custom-names.yml");

        if (!file.exists())
            plugin.saveResource("custom-names.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        if (cfg.getBoolean("enabled", true)) {
            for (String key : cfg.getConfigurationSection("").getKeys(false))
                customNames.put(key, ChatColor.translateAlternateColorCodes('&', cfg.getString(key)));
        }
    }

    private List<ParticleWrapper> getParticles(WildStackerPlugin plugin, String sectionPath) {
        if (particlesYaml == null) {
            File file = new File(plugin.getDataFolder(), "particles.yml");

            if (!file.exists())
                plugin.saveResource("particles.yml", false);

            particlesYaml = YamlConfiguration.loadConfiguration(file);
        }

        List<ParticleWrapper> particleWrappers = new ArrayList<>();
        ConfigurationSection section = particlesYaml.getConfigurationSection(sectionPath);

        if (section != null) {
            for (String key : section.getKeys(false)) {
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
                } catch (IllegalArgumentException ex) {
                    WildStackerPlugin.log("Particle " + sectionPath + "." + key + " is missing 'type'.");
                }
            }
        }

        return particleWrappers;
    }



}
