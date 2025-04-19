package com.bgsoftware.wildstacker.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.wildstacker.Manager;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.config.section.*;
import com.bgsoftware.wildstacker.errors.ManagerLoadException;
import com.bgsoftware.wildstacker.logging.Log;
import com.bgsoftware.wildstacker.utils.files.FileUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@SuppressWarnings("WeakerAccess")
public class SettingsManagerImpl extends Manager implements SettingsManager {

    private static final String[] IGNORED_SECTIONS = new String[]{"merge-radius", "limits", "minimum-required", "default-unstack",
            "break-slots", "manage-menu", "break-charge", "place-charge", "spawners-override.spawn-conditions",
            "spawner-upgrades.ladders"};

    private final DatabaseSection database = new DatabaseSection();
    private final GlobalSection global = new GlobalSection();
    private final ItemsSection items = new ItemsSection();
    private final EntitiesSection entities = new EntitiesSection();
    private final SpawnersSection spawners = new SpawnersSection();
    private final BarrelsSection barrels = new BarrelsSection();
    private final BucketsSection buckets = new BucketsSection();
    private final StewsSection stews = new StewsSection();
    private final KillTaskSection killTask = new KillTaskSection();
    private final SpawnerUpgradeSection spawnerUpgrades = new SpawnerUpgradeSection();
    private final NameOverridesSection nameOverrides = new NameOverridesSection();
    private final StackChecksSection stackChecks = new StackChecksSection();
    private final StackSplitsSection stackSplits = new StackSplitsSection();

    public SettingsManagerImpl(WildStackerPlugin plugin) {
        super(plugin);
    }


    @Override
    public void loadData() throws ManagerLoadException {
        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        convertData(cfg);

        try {
            cfg.syncWithConfig(file, plugin.getResource("config.yml"), IGNORED_SECTIONS);
        } catch (Exception error) {
            Log.error(error, file, "An unexpected error occurred while loading config file:");
        }

        loadContainerFromConfig(cfg);
    }

    private void loadContainerFromConfig(YamlConfiguration cfg) throws ManagerLoadException {
        SettingsContainer container = new SettingsContainer(plugin, cfg);
        this.global.setContainer(container);
        this.database.setContainer(container);
        this.items.setContainer(container);
        this.entities.setContainer(container);
        this.spawners.setContainer(container);
        this.barrels.setContainer(container);
        this.buckets.setContainer(container);
        this.stews.setContainer(container);
        this.killTask.setContainer(container);
        this.spawnerUpgrades.setContainer(container);
        this.nameOverrides.setContainer(container);
        this.stackChecks.setContainer(container);
        this.stackSplits.setContainer(container);
    }

    public void registerSpawnConditions() {
        if (this.spawners.isOverrideEnabled()) {
            plugin.getNMSSpawners().registerSpawnConditions();
            for (String entityTypeRaw : plugin.getConfig().getConfigurationSection("spawners.spawners-override.spawn-conditions").getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(entityTypeRaw);
                    plugin.getSystemManager().clearSpawnConditions(entityType);
                    for (String spawnConditionId : plugin.getConfig().getStringList("spawners.spawners-override.spawn-conditions." + entityTypeRaw)) {
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
    }

    public void updateUpgrades() {
        plugin.getUpgradesManager().removeAllUpgrades();
        for (String ladder : plugin.getConfig().getConfigurationSection("spawners.spawner-upgrades.ladders").getKeys(false)) {
            SpawnerUpgrade lastKnownUpgrade = null;
            int nextUpgradeId = -1;

            ConfigurationSection ladderSection = plugin.getConfig().getConfigurationSection("spawners.spawner-upgrades.ladders." + ladder);
            List<String> allowedEntities = ladderSection.getStringList("entities");

            for (String upgradeName : ladderSection.getKeys(false)) {
                if (ladderSection.isConfigurationSection(upgradeName)) {
                    ConfigurationSection upgrade = ladderSection.getConfigurationSection(upgradeName);
                    try {
                        SpawnerUpgrade spawnerUpgrade;

                        if (lastKnownUpgrade == null) {
                            spawnerUpgrade = plugin.getUpgradesManager().createDefault(upgradeName, upgrade.getInt("id", 0), allowedEntities);
                        } else {
                            spawnerUpgrade = plugin.getUpgradesManager().createUpgrade(upgradeName, upgrade.getInt("id", 0));
                            spawnerUpgrade.setAllowedEntities(allowedEntities);
                        }

                        spawnerUpgrade.setDisplayName(upgrade.getString("display", ""));
                        spawnerUpgrade.setCost(upgrade.getDouble("cost", 0D));

                        if (lastKnownUpgrade != null && nextUpgradeId == upgrade.getInt("id", 0))
                            lastKnownUpgrade.setNextUpgrade(spawnerUpgrade);

                        String nextUpgrade = upgrade.getString("next-upgrade");
                        if (nextUpgrade != null)
                            nextUpgradeId = ladderSection.getInt(nextUpgrade + ".id", 0);

                        if (upgrade.isConfigurationSection("icon")) {
                            spawnerUpgrade.setIcon(FileUtils.getItemStack("spawner-upgrades.yml",
                                    upgrade.getConfigurationSection("icon")).build());
                        }

                        spawnerUpgrade.setMinSpawnDelay(upgrade.getInt("min-spawn-delay", 200));
                        spawnerUpgrade.setMaxSpawnDelay(upgrade.getInt("max-spawn-delay", 800));
                        spawnerUpgrade.setSpawnCount(upgrade.getInt("spawn-count", 4));
                        spawnerUpgrade.setMaxNearbyEntities(upgrade.getInt("max-nearby-entities", 6));
                        spawnerUpgrade.setRequiredPlayerRange(upgrade.getInt("required-player-range", 16));
                        spawnerUpgrade.setSpawnRange(upgrade.getInt("spawn-range", 4));

                        lastKnownUpgrade = spawnerUpgrade;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public Database getDatabase() {
        return this.database;
    }

    @Override
    public Global getGlobal() {
        return this.global;
    }

    @Override
    public Items getItems() {
        return this.items;
    }

    @Override
    public Entities getEntities() {
        return this.entities;
    }

    @Override
    public Spawners getSpawners() {
        return this.spawners;
    }

    @Override
    public Barrels getBarrels() {
        return this.barrels;
    }

    @Override
    public Buckets getBuckets() {
        return this.buckets;
    }

    @Override
    public Stews getStews() {
        return this.stews;
    }

    @Override
    public KillTask getKillTask() {
        return this.killTask;
    }

    @Override
    public SpawnerUpgrades getSpawnerUpgrades() {
        return this.spawnerUpgrades;
    }

    @Override
    public NameOverrides getNameOverrides() {
        return this.nameOverrides;
    }

    @Override
    public StackChecks getStackChecks() {
        return this.stackChecks;
    }

    @Override
    public StackSplits getStackSplits() {
        return this.stackSplits;
    }

    public String[] getIgnoredSections() {
        return IGNORED_SECTIONS;
    }

    private void convertData(YamlConfiguration cfg) {
        if (cfg.contains("items.kill-all"))
            cfg.set("kill-task.stacked-items", cfg.getBoolean("items.kill-all"));
        if (cfg.contains("items.check-range"))
            cfg.set("items.merge-radius", cfg.getLong("items.check-range"));
        if (cfg.contains("items.save-interval"))
            cfg.set("save-interval", cfg.getLong("items.save-interval"));
        if (cfg.contains("items.blocked-materials"))
            cfg.set("items.blacklist", cfg.getStringList("items.blocked-materials"));
        if (cfg.contains("entities.check-range"))
            cfg.set("entities.merge-radius", cfg.getLong("entities.check-range"));
        if (cfg.contains("entities.reason-blacklist"))
            cfg.set("entities.spawn-blacklist", cfg.getStringList("entities.reason-blacklist"));
        if (cfg.contains("entities.kill-all.interval"))
            cfg.set("kill-task.interval", cfg.getLong("entities.kill-all.interval"));
        if (cfg.contains("entities.kill-all.clear-lagg"))
            cfg.set("kill-task.sync-clear-lagg", cfg.getBoolean("entities.kill-all.clear-lagg"));
        if (cfg.contains("kill-task.whitelist"))
            cfg.set("kill-task.entities.whitelist", cfg.getStringList("kill-task.whitelist"));
        if (cfg.contains("kill-task.blacklist"))
            cfg.set("kill-task.entities.blacklist", cfg.getStringList("kill-task.blacklist"));
        if (cfg.contains("kill-task.worlds"))
            cfg.set("kill-task.entities.worlds", cfg.getStringList("kill-task.worlds"));
        if (cfg.isBoolean("entities.keep-lowest-health")) {
            if (cfg.getBoolean("entities.keep-lowest-health"))
                cfg.set("entities.keep-lowest-health", Collections.singletonList("all"));
            else
                cfg.set("entities.keep-lowest-health", new ArrayList<>());
        }
        if (cfg.contains("spawners.holograms.custom-name"))
            cfg.set("spawners.custom-name", cfg.getString("spawners.holograms.custom-name"));
        if (cfg.contains("spawners.holograms.enabled") && !cfg.getBoolean("spawners.holograms.enabled"))
            cfg.set("spawners.custom-name", "");
        if (cfg.contains("items.custom-display"))
            cfg.set("items.custom-display", null);
        if (cfg.getConfigurationSection("spawners.break-menu") == null)
            cfg.createSection("spawners.break-menu");
        if (cfg.isBoolean("spawners.place-inventory"))
            cfg.set("spawners.place-inventory.enabled", cfg.getBoolean("spawners.place-inventory"));
        if (cfg.isBoolean("buckets-stacker"))
            cfg.set("buckets-stacker.enabled", cfg.getBoolean("buckets-stacker"));
        if (cfg.contains("entities.spawn-blacklist") || !cfg.getBoolean("mythic-mobs-stack", true)) {
            List<String> blacklisted = cfg.getStringList("entities.blacklist");
            if (cfg.contains("entities.spawn-blacklist"))
                blacklisted.addAll(cfg.getStringList("entities.spawn-blacklist"));
            if (!cfg.getBoolean("mythic-mobs-stack", true))
                blacklisted.add("MYTHIC_MOBS");
            cfg.set("entities.blacklist", blacklisted);
        }
        if (cfg.isBoolean("barrels.place-inventory"))
            cfg.set("barrels.place-inventory.enabled", cfg.getBoolean("barrels.place-inventory"));
        if (cfg.contains("items.buckets-stacker.enabled"))
            cfg.set("buckets.enabled", cfg.getBoolean("items.buckets-stacker.enabled"));
        if (cfg.contains("items.buckets-stacker.name-blacklist"))
            cfg.set("buckets.name-blacklist", cfg.getStringList("items.buckets-stacker.name-blacklist"));
        if (cfg.contains("items.buckets-stacker.max-stack"))
            cfg.set("buckets.max-stack", cfg.getInt("items.buckets-stacker.max-stack"));
        if (cfg.contains("entities.nerfed-spawning"))
            cfg.set("entities.nerfed-entities.whitelist", cfg.getStringList("entities.nerfed-spawning"));
        if (cfg.contains("entities.nerfed-worlds"))
            cfg.set("entities.nerfed-entities.worlds", cfg.getStringList("entities.nerfed-worlds"));
        if (cfg.contains("spawners.break-charge.amount")) {
            List<String> mobs = cfg.getStringList("spawners.break-charge.whitelist");
            if (mobs.isEmpty()) {
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
        if (cfg.contains("spawners.place-charge.amount")) {
            List<String> mobs = cfg.getStringList("spawners.place-charge.whitelist");
            if (mobs.isEmpty()) {
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
        if (cfg.contains("spawners.silk-spawners")) {
            cfg.set("spawners.silk-touch", cfg.getConfigurationSection("spawners.silk-spawners"));
            cfg.set("spawners.silk-spawners", null);
        }
        if (cfg.contains("spawners.silk-spawners.custom-name"))
            cfg.set("spawners.spawner-item.name", cfg.getString("spawners.silk-spawners.custom-name"));
        if (cfg.contains("spawners.silk-spawners.custom-lore"))
            cfg.set("spawners.spawner-item.lore", cfg.getStringList("spawners.silk-spawners.custom-lore"));
        if (cfg.contains("spawners.get-stacked-item"))
            cfg.set("drop-stacked-item", cfg.getBoolean("spawners.get-stacked-item"));
        if (cfg.contains("spawners.drop-without-silk"))
            cfg.set("spawners.silk-touch.drop-without-silk", cfg.getBoolean("spawners.drop-without-silk"));
        if (cfg.contains("spawners.silk-touch-break-chance"))
            cfg.set("spawners.silk-touch.break-chance", cfg.getInt("spawners.silk-touch-break-chance"));
        if (cfg.contains("spawners.silk-spawners.explosions-drop-spawner"))
            cfg.set("spawners.explosions.enabled", cfg.getBoolean("spawners.silk-spawners.explosions-drop-spawner"));
        if (cfg.contains("spawners.explosions-drop-to-inventory"))
            cfg.set("spawners.explosions.drop-to-inventory", cfg.getBoolean("spawners.explosions-drop-to-inventory"));
        if (cfg.contains("spawners.explosions-break-chance"))
            cfg.set("spawners.explosions.break-chance", cfg.getBoolean("spawners.explosions-break-chance"));
        if (cfg.contains("spawners.explosions-break-chance"))
            cfg.set("spawners.explosions.break-chance", cfg.getBoolean("spawners.explosions-break-chance"));
        if (cfg.contains("kill-task.entities"))
            cfg.set("kill-task.kill-entities", cfg.getConfigurationSection("kill-task.entities"));
        if (cfg.contains("kill-task.items"))
            cfg.set("kill-task.kill-items", cfg.getConfigurationSection("kill-task.items"));
        if (cfg.contains("entities.minimum-limits"))
            cfg.set("entities.minimum-required", cfg.getConfigurationSection("entities.minimum-limits"));
        if (cfg.isInt("items.merge-radius"))
            cfg.set("items.merge-radius.all", cfg.getInt("items.merge-radius"));
        if (cfg.isInt("entities.merge-radius"))
            cfg.set("entities.merge-radius.all", cfg.getInt("entities.merge-radius"));
        if (cfg.isInt("spawners.merge-radius"))
            cfg.set("spawners.merge-radius.all", cfg.getInt("spawners.merge-radius"));
        if (cfg.isInt("barrels.merge-radius"))
            cfg.set("barrels.merge-radius.all", cfg.getInt("barrels.merge-radius"));
        if (cfg.isBoolean("spawners.explosions-break-stack"))
            cfg.set("spawners.explosions-break-percentage", cfg.getBoolean("spawners.explosions-break-stack") ? 100 : -1);
        if (!cfg.contains("spawners.spawner-upgrades.ladders")) {
            ConfigurationSection laddersSection = cfg.getConfigurationSection("spawners.spawner-upgrades");
            cfg.set("spawners.spawner-upgrades", null);
            cfg.set("spawners.spawner-upgrades.ladders", laddersSection);
        }
        if (cfg.contains("spawners.spawn-conditions")) {
            ConfigurationSection spawnConditions = cfg.getConfigurationSection("spawners.spawn-conditions");
            cfg.set("spawners.spawn-conditions", null);
            cfg.set("spawners.spawners-override.spawn-conditions", spawnConditions);
        }
        if (cfg.contains("entities.next-stack-knockback")) {
            cfg.set("entities.fast-kill", !cfg.getBoolean("entities.next-stack-knockback"));
            cfg.set("entities.next-stack-knockback", null);
        }
        if (cfg.contains("items.pickup-sound.enabled")) {
            cfg.set("items.pickup-sound", cfg.getBoolean("items.pickup-sound.enabled"));
        }
        if (cfg.isBoolean("entities.smart-breeding")) {
            cfg.set("entities.smart-breeding.enabled", cfg.getBoolean("entities.smart-breeding"));
        }
    }


}
