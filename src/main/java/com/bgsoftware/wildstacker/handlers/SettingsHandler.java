package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackSplit;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.menu.SpawnerAmountsMenu;
import com.bgsoftware.wildstacker.menu.SpawnerUpgradeMenu;
import com.bgsoftware.wildstacker.menu.SpawnersManageMenu;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.upgrades.WSpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.data.structures.Fast2EnumsArray;
import com.bgsoftware.wildstacker.utils.data.structures.Fast2EnumsMap;
import com.bgsoftware.wildstacker.utils.data.structures.Fast3EnumsArray;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumMap;
import com.bgsoftware.wildstacker.utils.entity.StackCheck;
import com.bgsoftware.wildstacker.utils.files.FileUtils;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import com.bgsoftware.wildstacker.utils.names.NameBuilder;
import com.bgsoftware.wildstacker.utils.names.NamePlaceholder;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.particles.ParticleWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class SettingsHandler {

    private static final ReflectField<Object> MOB_SPAWNER_ABSTRACT = new ReflectField<>("net.minecraft.server.VERSION.TileEntityMobSpawner", Object.class, "a").removeFinal();

    public final Pattern SPAWNERS_PATTERN;
    public final String[] CONFIG_IGNORED_SECTIONS = { "merge-radius", "limits", "minimum-required", "default-unstack",
            "break-slots", "manage-menu", "break-charge", "place-charge", "spawners-override.spawn-conditions", "spawner-upgrades.ladders" };

    //Global settings
    public final String giveItemName;
    public final ItemStack inspectTool, simulateTool;
    public final boolean deleteInvalidWorlds, killTaskStackedEntities, killTaskUnstackedEntities,
            killTaskStackedItems, killTaskUnstackedItems, killTaskSyncClearLagg;
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
    public final FastEnumMap<Material, Integer> itemsMergeRadius, itemsLimits;
    public final float itemsSoundVolume, itemsSoundPitch;
    public final List<ParticleWrapper> itemsParticles;
    public final long itemsStackInterval;

    //Entities settings
    public final boolean entitiesStackingEnabled, entitiesParticlesEnabled, linkedEntitiesEnabled, nerfedEntitiesTeleport,
            stackDownEnabled, keepFireEnabled, mythicMobsCustomNameEnabled, stackAfterBreed, smartBreeding,
            entitiesHideNames, entitiesNamesToggleEnabled, nextStackKnockback, eggLayMultiply, scuteMultiply,
            entitiesClearEquipment, spawnCorpses, entitiesOneShotEnabled, storeEntities, superiorSkyblockHook,
            multiplyDrops, multiplyExp, spreadDamage;
    public final long entitiesStackInterval;
    public final String entitiesCustomName, entitiesNamesToggleCommand;
    public final NameBuilder<StackedEntity> entitiesNameBuilder;
    public final Sound entitiesExpPickupSound;
    public final int linkedEntitiesMaxDistance, entitiesChunkLimit;
    public final Fast2EnumsArray<EntityType, SpawnCause> blacklistedEntities, whitelistedEntities, entitiesNerfedWhitelist,
            entitiesNerfedBlacklist, stackDownTypes, keepLowestHealth, entitiesAutoExpPickup, entitiesOneShotWhitelist;
    public final Fast3EnumsArray<EntityType, SpawnCause, EntityDamageEvent.DamageCause> entitiesInstantKills;
    public final List<String> entitiesDisabledWorlds, entitiesDisabledRegions, entitiesNerfedWorlds, entitiesOneShotTools;
    public final List<Pattern> blacklistedEntitiesNames;
    public final Fast2EnumsMap<EntityType, SpawnCause, Integer> entitiesMergeRadius, entitiesLimits,
            minimumRequiredEntities, defaultUnstack;
    public final List<ParticleWrapper> entitiesParticles;

    //Spawners settings
    public final boolean spawnersStackingEnabled, perSpawnerLimit, spawnersParticlesEnabled, chunkMergeSpawners,
            silkTouchSpawners, explosionsDropSpawner, explosionsDropToInventory, dropToInventory, shiftGetWholeSpawnerStack,
            getStackedItem, dropSpawnerWithoutSilk, spawnersMineRequireSilk, floatingSpawnerNames, spawnersPlacementPermission,
            spawnersShiftPlaceStack, changeUsingEggs, eggsStackMultiply, nextSpawnerPlacement, onlyOneSpawner, inventoryTweaksEnabled,
            amountsMenuEnabled, upgradeMenuEnabled, manageMenuEnabled, spawnersOverrideEnabled, spawnerUpgradesMultiplyStackAmount;
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

    public SettingsHandler(WildStackerPlugin plugin){
        WildStackerPlugin.log("Loading configuration started...");
        long startTime = System.currentTimeMillis();
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        dataConvertor(cfg);

        try {
            cfg.syncWithConfig(file, plugin.getResource("config.yml"), CONFIG_IGNORED_SECTIONS);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        giveItemName = ChatColor.translateAlternateColorCodes('&', cfg.getString("give-item-name", "&6x{0} &f&o{1} {2}"));
        SPAWNERS_PATTERN = Pattern.compile(giveItemName
                .replace("{0}", "(.*)")
                .replace("{1}", "(.*)")
                .replace("{2}", "(.*)")
        );
        inspectTool = new ItemBuilder(Material.valueOf(cfg.getString("inspect-tool.type")), cfg.getInt("inspect-tool.data", 0))
                .withName(cfg.getString("inspect-tool.name"))
                .withLore(cfg.getStringList("inspect-tool.lore")).build();
        simulateTool = new ItemBuilder(Material.valueOf(cfg.getString("inspect-tool.type")), cfg.getInt("inspect-tool.data", 0))
                .withName(cfg.getString("simulate-tool.name"))
                .withLore(cfg.getStringList("simulate-tool.lore")).build();
        deleteInvalidWorlds = cfg.getBoolean("database.delete-invalid-worlds", false);
        killTaskInterval = cfg.getLong("kill-task.interval", 300);
        killTaskStackedEntities = cfg.getBoolean("kill-task.stacked-entities", true);
        killTaskUnstackedEntities = cfg.getBoolean("kill-task.unstacked-entities", true);
        killTaskStackedItems = cfg.getBoolean("kill-task.stacked-items", true);
        killTaskUnstackedItems = cfg.getBoolean("kill-task.unstacked-items", true);
        killTaskSyncClearLagg = cfg.getBoolean("kill-task.sync-clear-lagg", false);
        killTaskEntitiesWhitelist = Fast2EnumsArray.fromList(cfg.getStringList("kill-task.kill-entities.whitelist"),
                EntityType.class, SpawnCause.class);
        killTaskEntitiesBlacklist = Fast2EnumsArray.fromList(cfg.getStringList("kill-task.kill-entities.blacklist"),
                EntityType.class, SpawnCause.class);
        killTaskEntitiesWorlds = cfg.getStringList("kill-task.kill-entities.worlds");
        killTaskItemsWhitelist = FastEnumArray.fromList(cfg.getStringList("kill-task.kill-items.whitelist"), Material.class);
        killTaskItemsBlacklist = FastEnumArray.fromList(cfg.getStringList("kill-task.kill-items.blacklist"), Material.class);
        killTaskItemsWorlds = cfg.getStringList("kill-task.kill-items.worlds");
        customNames = new HashMap<>();
        loadCustomNames(plugin);

        itemsStackingEnabled = cfg.getBoolean("items.enabled", true);
        itemsMergeRadius = FastEnumMap.fromSection(cfg.getConfigurationSection("items.merge-radius"), Material.class);
        itemsParticlesEnabled = cfg.getBoolean("items.particles", true);
        itemsParticles = getParticles(plugin, "items");
        itemsDisabledWorlds = cfg.getStringList("items.disabled-worlds");
        itemsLimits = FastEnumMap.fromSection(cfg.getConfigurationSection("items.limits"), Material.class);
        itemsUnstackedCustomName = cfg.getBoolean("items.unstacked-custom-name", false);
        itemsFixStackEnabled = cfg.getBoolean("items.fix-stack", false);
        blacklistedItems = FastEnumArray.fromList(cfg.getStringList("items.blacklist"), Material.class);
        whitelistedItems = FastEnumArray.fromList(cfg.getStringList("items.whitelist"), Material.class);
        itemsChunkLimit = cfg.getInt("items.chunk-limit", 0);
        itemsCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("items.custom-name", "&6&lx{0} {1}"));
        //noinspection unchecked
        itemsNameBuilder = new NameBuilder<>(itemsCustomName,
                new NamePlaceholder<>("{0}", stackedItem -> stackedItem.getStackAmount() + ""),
                new NamePlaceholder<>("{1}", stackedItem -> ((WStackedItem) stackedItem).getCachedDisplayName()),
                new NamePlaceholder<>("{2}", stackedItem -> ((WStackedItem) stackedItem).getCachedDisplayName().toUpperCase())
        );
        itemsDisplayEnabled = cfg.getBoolean("items.item-display", false);
        itemsNamesToggleEnabled = cfg.getBoolean("items.names-toggle.enabled", false);
        itemsNamesToggleCommand = cfg.getString("items.names-toggle.command", "stacker names item");
        itemsSoundEnabled = cfg.getBoolean("items.pickup-sound.enabled", true);
        itemsSoundVolume = (float) cfg.getDouble("items.pickup-sound.volume");
        itemsSoundPitch = (float) cfg.getDouble("items.pickup-sound.pitch");
        itemsMaxPickupDelay = cfg.getBoolean("items.max-pickup-delay", false);
        itemsStackInterval = cfg.getLong("items.stack-interval", 0L);
        storeItems = cfg.getBoolean("items.store-items", true);

        entitiesStackingEnabled = cfg.getBoolean("entities.enabled", true);
        entitiesMergeRadius = Fast2EnumsMap.fromSectionToInt(cfg.getConfigurationSection("entities.merge-radius"),
                EntityType.class, SpawnCause.class);
        entitiesParticlesEnabled = cfg.getBoolean("entities.particles", true);
        entitiesParticles = getParticles(plugin, "entities");
        entitiesStackInterval = cfg.getLong("entities.stack-interval", 0);
        entitiesDisabledWorlds = cfg.getStringList("entities.disabled-worlds");
        entitiesLimits = Fast2EnumsMap.fromSectionToInt(cfg.getConfigurationSection("entities.limits"),
                EntityType.class, SpawnCause.class);
        minimumRequiredEntities = Fast2EnumsMap.fromSectionToInt(cfg.getConfigurationSection("entities.minimum-required"),
                EntityType.class, SpawnCause.class);
        entitiesCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("entities.custom-name", "&d&lx{0} {1}"));
        //noinspection unchecked
        entitiesNameBuilder = new NameBuilder<>(entitiesCustomName,
                new NamePlaceholder<>("{0}", stackedEntity -> stackedEntity.getStackAmount() + ""),
                new NamePlaceholder<>("{1}", stackedEntity -> ((WStackedEntity) stackedEntity).getCachedDisplayName()),
                new NamePlaceholder<>("{2}", stackedEntity -> ((WStackedEntity) stackedEntity).getCachedDisplayName().toUpperCase()),
                new NamePlaceholder<>("{3}", stackedEntity -> stackedEntity.getUpgrade().getDisplayName())
        );
        entitiesChunkLimit = cfg.getInt("entities.chunk-limit", 0);
        entitiesDisabledRegions = cfg.getStringList("entities.disabled-regions");
        linkedEntitiesEnabled = cfg.getBoolean("entities.linked-entities.enabled", true);
        nerfedEntitiesTeleport = cfg.getBoolean("entities.nerfed-entities.teleport", false);
        linkedEntitiesMaxDistance = cfg.getInt("entities.linked-entities.max-distance", 10);
        blacklistedEntities = Fast2EnumsArray.fromList(cfg.getStringList("entities.blacklist"),
                EntityType.class, SpawnCause.class);
        whitelistedEntities = Fast2EnumsArray.fromList(cfg.getStringList("entities.whitelist"),
                EntityType.class, SpawnCause.class);
        blacklistedEntitiesNames = cfg.getStringList("entities.name-blacklist").stream()
                .map(line -> Pattern.compile(ChatColor.translateAlternateColorCodes('&', line)))
                .collect(Collectors.toList());
        entitiesInstantKills = Fast3EnumsArray.fromList(cfg.getStringList("entities.instant-kill"),
                EntityType.class, SpawnCause.class, EntityDamageEvent.DamageCause.class);
        entitiesNerfedWhitelist = Fast2EnumsArray.fromList(cfg.getStringList("entities.nerfed-entities.whitelist"),
                EntityType.class, SpawnCause.class);
        entitiesNerfedBlacklist = Fast2EnumsArray.fromList(cfg.getStringList("entities.nerfed-entities.blacklist"),
                EntityType.class, SpawnCause.class);
        entitiesNerfedWorlds = cfg.getStringList("entities.nerfed-entities.worlds");
        stackDownEnabled = cfg.getBoolean("entities.stack-down.enabled", true);
        stackDownTypes = Fast2EnumsArray.fromList(cfg.getStringList("entities.stack-down.stack-down-types"),
                EntityType.class, SpawnCause.class);
        keepFireEnabled = cfg.getBoolean("entities.keep-fire", true);
        mythicMobsCustomNameEnabled = cfg.getBoolean("entities.mythic-mobs-custom-name", true);
        keepLowestHealth = Fast2EnumsArray.fromList(cfg.getStringList("entities.keep-lowest-health"),
                EntityType.class, SpawnCause.class);
        stackAfterBreed = cfg.getBoolean("entities.stack-after-breed", true);
        smartBreeding = cfg.getBoolean("entities.smart-breeding", false);
        entitiesHideNames = cfg.getBoolean("entities.hide-names", false);
        entitiesNamesToggleEnabled = cfg.getBoolean("entities.names-toggle.enabled", false);
        entitiesNamesToggleCommand = cfg.getString("entities.names-toggle.command", "stacker names entity");
        nextStackKnockback = cfg.getBoolean("entities.next-stack-knockback", true);
        defaultUnstack = Fast2EnumsMap.fromSectionToInt(cfg.getConfigurationSection("entities.default-unstack"),
                EntityType.class, SpawnCause.class);
        entitiesAutoExpPickup = Fast2EnumsArray.fromList(cfg.getStringList("entities.auto-exp-pickup"),
                EntityType.class, SpawnCause.class);
        Sound entitiesExpPickupSound;
        try{
            entitiesExpPickupSound = Sound.valueOf(cfg.getString("entities.exp-pickup-sound"));
        }catch (Exception ignored){
            entitiesExpPickupSound = null;
        }
        this.entitiesExpPickupSound = entitiesExpPickupSound;
        eggLayMultiply = cfg.getBoolean("entities.egg-lay-multiply", true);
        scuteMultiply = cfg.getBoolean("entities.scute-multiply", true);
        entitiesClearEquipment = cfg.getBoolean("entities.clear-equipment", false);
        spawnCorpses = cfg.getBoolean("entities.spawn-corpses", true);
        entitiesOneShotEnabled = cfg.getBoolean("entities.one-shot.enabled", false);
        entitiesOneShotTools = cfg.getStringList("entities.one-shot.tools");
        entitiesOneShotWhitelist = Fast2EnumsArray.fromList(cfg.getStringList("entities.one-shot.whitelist"),
                EntityType.class, SpawnCause.class);
        storeEntities = cfg.getBoolean("entities.store-entities", true);
        superiorSkyblockHook = cfg.getBoolean("entities.superiorskyblock-hook", false);
        multiplyDrops = cfg.getBoolean("entities.multiply-drops", true);
        multiplyExp = cfg.getBoolean("entities.multiply-exp", true);
        spreadDamage = cfg.getBoolean("entities.spread-damage", false);

        spawnersStackingEnabled = cfg.getBoolean("spawners.enabled", true);
        spawnersMergeRadius = FastEnumMap.fromSection(cfg.getConfigurationSection("spawners.merge-radius"), EntityType.class);
        perSpawnerLimit = cfg.getBoolean("spawners.per-spawner-limit", false);
        spawnersParticlesEnabled = cfg.getBoolean("spawners.particles", true);
        spawnersParticles = getParticles(plugin, "spawners");
        spawnersDisabledWorlds = cfg.getStringList("spawners.disabled-worlds");
        spawnersLimits = FastEnumMap.fromSection(cfg.getConfigurationSection("spawners.limits"), EntityType.class);
        chunkMergeSpawners = cfg.getBoolean("spawners.chunk-merge", false);
        blacklistedSpawners = FastEnumArray.fromList(cfg.getStringList("spawners.blacklist"), EntityType.class);
        whitelistedSpawners = FastEnumArray.fromList(cfg.getStringList("spawners.whitelist"), EntityType.class);
        spawnersChunkLimit = cfg.getInt("spawners.chunk-limit", 0);
        spawnersCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("spawners.custom-name", "&9&lx{0} {1}"));
        //noinspection unchecked
        spawnersNameBuilder = new NameBuilder<>(spawnersCustomName,
                new NamePlaceholder<>("{0}", stackedSpawner -> stackedSpawner.getStackAmount() + ""),
                new NamePlaceholder<>("{1}", stackedSpawner -> ((WStackedSpawner) stackedSpawner).getCachedDisplayName()),
                new NamePlaceholder<>("{2}", stackedSpawner -> ((WStackedSpawner) stackedSpawner).getCachedDisplayName().toUpperCase()),
                new NamePlaceholder<>("{3}", stackedSpawner -> stackedSpawner.getUpgrade().getDisplayName())
        );
        spawnerItemName = ChatColor.translateAlternateColorCodes('&', cfg.getString("spawners.spawner-item.name", "&e{0} &fSpawner"));
        spawnerItemLore = cfg.getStringList("spawners.spawner-item.lore").stream().map(line ->
                ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        silkTouchSpawners = cfg.getBoolean("spawners.silk-touch.enabled", true);
        dropToInventory = cfg.getBoolean("spawners.silk-touch.drop-to-inventory", true);
        silkWorlds = cfg.getStringList("spawners.silk-touch.worlds");
        dropSpawnerWithoutSilk = cfg.getBoolean("spawners.silk-touch.drop-without-silk", false);
        silkTouchBreakChance = cfg.getInt("spawners.silk-touch.break-chance", 100);
        silkTouchMinimumLevel = cfg.getInt("spawners.silk-touch.minimum-level", 1);
        explosionsDropSpawner = cfg.getBoolean("spawners.explosions.enabled", true);
        explosionsDropToInventory = cfg.getBoolean("spawners.explosions.drop-to-inventory", false);
        explosionsWorlds = cfg.getStringList("spawners.explosions.worlds");
        explosionsBreakChance = cfg.getInt("spawners.explosions.break-chance", 100);
        explosionsBreakPercentage = cfg.getInt("spawners.explosions-break-percentage", 100);
        explosionsBreakMinimum = cfg.getInt("spawners.explosions-break-minimum", 1);
        explosionsAmountPercentage = cfg.getInt("spawners.explosions-amount-percentage", 100);
        explosionsAmountMinimum = cfg.getInt("spawners.explosions-amount-minimum", 1);
        spawnersMineRequireSilk = cfg.getBoolean("spawners.mine-require-silk", false);
        shiftGetWholeSpawnerStack = cfg.getBoolean("spawners.shift-get-whole-stack", true);
        getStackedItem = cfg.getBoolean("spawners.drop-stacked-item", true);
        floatingSpawnerNames = cfg.getBoolean("spawners.floating-names", false);
        spawnersPlacementPermission = cfg.getBoolean("spawners.placement-permission", false);
        spawnersShiftPlaceStack = cfg.getBoolean("spawners.shift-place-stack", true);
        spawnersBreakCharge = new FastEnumMap<>(EntityType.class);
        for(String key : cfg.getConfigurationSection("spawners.break-charge").getKeys(false)){
            ConfigurationSection mobSection = cfg.getConfigurationSection("spawners.break-charge." + key);
            EntityType entityType;

            try{
                entityType = EntityType.valueOf(key.toUpperCase());
            }catch (Exception ex){
                continue;
            }

            double amount = mobSection.getDouble("price", 0.0);
            if(amount > 0) {
                spawnersBreakCharge.put(entityType, new Pair<>(amount, mobSection.getBoolean("multiply-stack-amount", false)));
            }
        }
        spawnersPlaceCharge = new FastEnumMap<>(EntityType.class);
        for(String key : cfg.getConfigurationSection("spawners.place-charge").getKeys(false)){
            ConfigurationSection mobSection = cfg.getConfigurationSection("spawners.place-charge." + key);
            EntityType entityType;

            try{
                entityType = EntityType.valueOf(key.toUpperCase());
            }catch (Exception ex){
                continue;
            }

            double amount = mobSection.getDouble("price", 0.0);
            if(amount > 0) {
                spawnersPlaceCharge.put(entityType, new Pair<>(amount, mobSection.getBoolean("multiply-stack-amount", false)));
            }
        }
        changeUsingEggs = cfg.getBoolean("spawners.change-using-eggs", true);
        eggsStackMultiply = cfg.getBoolean("spawners.eggs-stack-multiply", true);
        nextSpawnerPlacement = cfg.getBoolean("spawners.next-spawner-placement", true);
        onlyOneSpawner = cfg.getBoolean("spawners.only-one-spawner", true);
        inventoryTweaksEnabled = cfg.getBoolean("spawners.inventory-tweaks.enabled", true);
        inventoryTweaksPermission = cfg.getString("spawners.inventory-tweaks.permission", "");
        inventoryTweaksCommand = cfg.getString("spawners.inventory-tweaks.toggle-command", "stacker inventorytweaks,stacker it");
        spawnersOverrideEnabled = spawnersStackingEnabled && MOB_SPAWNER_ABSTRACT.isValid() && cfg.getBoolean("spawners-override.enabled");
        if(spawnersOverrideEnabled) {
            plugin.getNMSSpawners().registerSpawnConditions();
            for (String entityTypeRaw : cfg.getConfigurationSection("spawners.spawners-override.spawn-conditions").getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(entityTypeRaw);
                    plugin.getSystemManager().clearSpawnConditions(entityType);
                    for (String spawnConditionId : cfg.getStringList("spawners.spawners-override.spawn-conditions." + entityTypeRaw)) {
                        Optional<SpawnCondition> spawnConditionOptional = plugin.getSystemManager().getSpawnCondition(spawnConditionId);

                        if (!spawnConditionOptional.isPresent()) {
                            WildStackerPlugin.log("Invalid spawn condition: " + spawnConditionId);
                            continue;
                        }

                        plugin.getSystemManager().addSpawnCondition(spawnConditionOptional.get(), entityType);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        amountsMenuEnabled = cfg.getBoolean("spawners.manage-menu.amounts-menu");
        upgradeMenuEnabled = cfg.getBoolean("spawners.manage-menu.upgrade-menu");
        manageMenuEnabled = amountsMenuEnabled || upgradeMenuEnabled;
        spawnerUpgradesMultiplyStackAmount = cfg.getBoolean("spawners.spawner-upgrades.multiply-stack-amount", true);
        plugin.getUpgradesManager().removeAllUpgrades();
        for(String ladder : cfg.getConfigurationSection("spawners.spawner-upgrades.ladders").getKeys(false)){
            ConfigurationSection ladderSection = cfg.getConfigurationSection("spawners.spawner-upgrades.ladders." + ladder);
            List<String> allowedEntities = ladderSection.getStringList("entities");

            for(String upgradeName : ladderSection.getKeys(false)){
                if(ladderSection.isConfigurationSection(upgradeName)) {
                    ConfigurationSection upgrade = ladderSection.getConfigurationSection(upgradeName);
                    try {
                        SpawnerUpgrade spawnerUpgrade;

                        if (upgradeName.equalsIgnoreCase("default")) {
                            spawnerUpgrade = plugin.getUpgradesManager().createDefault(allowedEntities);
                        } else {
                            spawnerUpgrade = plugin.getUpgradesManager().createUpgrade(upgradeName, upgrade.getInt("id", 0));
                            spawnerUpgrade.setDisplayName(upgrade.getString("display", ""));
                            spawnerUpgrade.setCost(upgrade.getDouble("cost", 0D));
                            spawnerUpgrade.setAllowedEntities(allowedEntities);
                        }

                        String nextUpgrade = upgrade.getString("next-upgrade");
                        if(nextUpgrade != null)
                            ((WSpawnerUpgrade) spawnerUpgrade).setNextUpgrade(ladderSection.getInt(nextUpgrade + ".id", 0));

                        if(upgrade.isConfigurationSection("icon")) {
                            spawnerUpgrade.setIcon(FileUtils.getItemStack("spawner-upgrades.yml",
                                    upgrade.getConfigurationSection("icon")).build());
                        }

                        spawnerUpgrade.setMinSpawnDelay(upgrade.getInt("min-spawn-delay", 200));
                        spawnerUpgrade.setMaxSpawnDelay(upgrade.getInt("max-spawn-delay", 800));
                        spawnerUpgrade.setSpawnCount(upgrade.getInt("spawn-count", 4));
                        spawnerUpgrade.setMaxNearbyEntities(upgrade.getInt("max-nearby-entities", 6));
                        spawnerUpgrade.setRequiredPlayerRange(upgrade.getInt("required-player-range", 16));
                        spawnerUpgrade.setSpawnRange(upgrade.getInt("spawn-range", 4));
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }

        barrelsStackingEnabled = ServerVersion.isAtLeast(ServerVersion.v1_8) && cfg.getBoolean("barrels.enabled", true);
        barrelsMergeRadius = FastEnumMap.fromSection(cfg.getConfigurationSection("barrels.merge-radius"), Material.class);
        barrelsParticlesEnabled = cfg.getBoolean("barrels.particles", true);
        barrelsParticles = getParticles(plugin, "barrels");
        barrelsDisabledWorlds = cfg.getStringList("barrels.disabled-worlds");
        barrelsLimits = FastEnumMap.fromSection(cfg.getConfigurationSection("barrels.limits"), Material.class);
        chunkMergeBarrels = cfg.getBoolean("barrels.chunk-merge", false);
        barrelsCustomName = ChatColor.translateAlternateColorCodes('&', cfg.getString("barrels.custom-name", "&9&lx{0} {1}"));
        //noinspection unchecked
        barrelsNameBuilder = new NameBuilder<>(barrelsCustomName,
                new NamePlaceholder<>("{0}", stackedBarrel -> stackedBarrel.getStackAmount() + ""),
                new NamePlaceholder<>("{1}", stackedBarrel -> ((WStackedBarrel) stackedBarrel).getCachedDisplayName()),
                new NamePlaceholder<>("{2}", stackedBarrel -> ((WStackedBarrel) stackedBarrel).getCachedDisplayName().toUpperCase())
        );
        blacklistedBarrels = FastEnumArray.fromList(cfg.getStringList("barrels.blacklist"), Material.class);
        whitelistedBarrels = FastEnumArray.fromList(cfg.getStringList("barrels.whitelist"), Material.class);
        barrelsChunkLimit = cfg.getInt("barrels.chunk-limit", 0);
        explosionsBreakBarrelStack = cfg.getBoolean("barrels.explosions-break-stack", true);
        barrelsToggleCommand = cfg.getBoolean("barrels.toggle-command.enabled", false);
        barrelsToggleCommandSyntax = cfg.getString("barrels.toggle-command.command", "stacker toggle");
        barrelsPlaceInventory = cfg.getBoolean("barrels.place-inventory.enabled", true);
        barrelsPlaceInventoryTitle = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("barrels.place-inventory.title", "Add items here ({0})"));
        forceCauldron = cfg.getBoolean("barrels.force-cauldron", false);
        barrelsRequiredPermission = cfg.getString("barrels.required-permission", "");
        barrelsAutoPickup = cfg.getBoolean("barrels.auto-pickup", false);
        dropStackedItem = cfg.getBoolean("barrels.drop-stacked-item", false);
        barrelsShiftPlaceStack = cfg.getBoolean("barrels.shift-place-stack", false);

        bucketsStackerEnabled = cfg.getBoolean("buckets.enabled", true);
        bucketsBlacklistedNames = cfg.getStringList("buckets.name-blacklist").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        bucketsMaxStack = cfg.getInt("buckets.max-stack", 16);

        stewsStackingEnabled = cfg.getBoolean("stews.enabled", true);
        stewsMaxStack = cfg.getInt("stews.max-stack", 16);

        for(StackCheck check : StackCheck.values()) {
            check.setEnabled(cfg.getBoolean("entities.stack-checks." + check.name(), false));
        }

        for(StackSplit split : StackSplit.values()) {
            split.setEnabled(cfg.getBoolean("entities.stack-split." + split.name(), false));
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

    private String getBoolean(boolean bool){
        return bool ? "enabled" : "disabled";
    }

    private void loadCustomNames(WildStackerPlugin plugin){
        File file = new File(plugin.getDataFolder(), "custom-names.yml");

        if(!file.exists())
            plugin.saveResource("custom-names.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        if(cfg.getBoolean("enabled", true)){
            for(String key : cfg.getConfigurationSection("").getKeys(false))
                customNames.put(key, ChatColor.translateAlternateColorCodes('&', cfg.getString(key)));
        }
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
        if(cfg.contains("kill-task.whitelist"))
            cfg.set("kill-task.entities.whitelist", cfg.getStringList("kill-task.whitelist"));
        if(cfg.contains("kill-task.blacklist"))
            cfg.set("kill-task.entities.blacklist", cfg.getStringList("kill-task.blacklist"));
        if(cfg.contains("kill-task.worlds"))
            cfg.set("kill-task.entities.worlds", cfg.getStringList("kill-task.worlds"));
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
        if(cfg.contains("spawners.silk-spawners")) {
            cfg.set("spawners.silk-touch", cfg.getConfigurationSection("spawners.silk-spawners"));
            cfg.set("spawners.silk-spawners", null);
        }
        if(cfg.contains("spawners.silk-spawners.custom-name"))
            cfg.set("spawners.spawner-item.name", cfg.getString("spawners.silk-spawners.custom-name"));
        if(cfg.contains("spawners.silk-spawners.custom-lore"))
            cfg.set("spawners.spawner-item.lore", cfg.getStringList("spawners.silk-spawners.custom-lore"));
        if(cfg.contains("spawners.get-stacked-item"))
            cfg.set("drop-stacked-item", cfg.getBoolean("spawners.get-stacked-item"));
        if(cfg.contains("spawners.drop-without-silk"))
            cfg.set("spawners.silk-touch.drop-without-silk", cfg.getBoolean("spawners.drop-without-silk"));
        if(cfg.contains("spawners.silk-touch-break-chance"))
            cfg.set("spawners.silk-touch.break-chance", cfg.getInt("spawners.silk-touch-break-chance"));
        if(cfg.contains("spawners.silk-spawners.explosions-drop-spawner"))
            cfg.set("spawners.explosions.enabled", cfg.getBoolean("spawners.silk-spawners.explosions-drop-spawner"));
        if(cfg.contains("spawners.explosions-drop-to-inventory"))
            cfg.set("spawners.explosions.drop-to-inventory", cfg.getBoolean("spawners.explosions-drop-to-inventory"));
        if(cfg.contains("spawners.explosions-break-chance"))
            cfg.set("spawners.explosions.break-chance", cfg.getBoolean("spawners.explosions-break-chance"));
        if(cfg.contains("spawners.explosions-break-chance"))
            cfg.set("spawners.explosions.break-chance", cfg.getBoolean("spawners.explosions-break-chance"));
        if(cfg.contains("kill-task.entities"))
            cfg.set("kill-task.kill-entities", cfg.getConfigurationSection("kill-task.entities"));
        if(cfg.contains("kill-task.items"))
            cfg.set("kill-task.kill-items", cfg.getConfigurationSection("kill-task.items"));
        if(cfg.contains("entities.minimum-limits"))
            cfg.set("entities.minimum-required", cfg.getConfigurationSection("entities.minimum-limits"));
        if(cfg.isInt("items.merge-radius"))
            cfg.set("items.merge-radius.all", cfg.getInt("items.merge-radius"));
        if(cfg.isInt("entities.merge-radius"))
            cfg.set("entities.merge-radius.all", cfg.getInt("entities.merge-radius"));
        if(cfg.isInt("spawners.merge-radius"))
            cfg.set("spawners.merge-radius.all", cfg.getInt("spawners.merge-radius"));
        if(cfg.isInt("barrels.merge-radius"))
            cfg.set("barrels.merge-radius.all", cfg.getInt("barrels.merge-radius"));
        if(cfg.isBoolean("spawners.explosions-break-stack"))
            cfg.set("spawners.explosions-break-percentage", cfg.getBoolean("spawners.explosions-break-stack") ? 100 : -1);
        if(!cfg.contains("spawners.spawner-upgrades.ladders")){
            ConfigurationSection laddersSection = cfg.getConfigurationSection("spawners.spawner-upgrades");
            cfg.set("spawners.spawner-upgrades", null);
            cfg.set("spawners.spawner-upgrades.ladders", laddersSection);
        }
        if(cfg.contains("spawners.spawn-conditions")){
            ConfigurationSection spawnConditions = cfg.getConfigurationSection("spawners.spawn-conditions");
            cfg.set("spawners.spawn-conditions", null);
            cfg.set("spawners.spawners-override.spawn-conditions", spawnConditions);
        }
    }

    public static void reload(){
        WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
        plugin.setSettings(new SettingsHandler(plugin));
    }

}
