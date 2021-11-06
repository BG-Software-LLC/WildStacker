package com.bgsoftware.wildstacker.api.handlers;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import org.bukkit.entity.EntityType;

import java.util.List;

public interface UpgradesManager {

    /**
     * Create a new spawner upgrade with a specific name.
     * If the upgrade already exists, it will be returned instead of new one being created.
     *
     * @param name The name of the upgrade.
     * @param id   The id of the upgrade
     */
    SpawnerUpgrade createUpgrade(String name, int id);

    /**
     * Create a new default upgrade.
     * This upgrade will be given to spawners by default.
     * If this upgrade doesn't have a next upgrade, then spawners won't be able to be upgraded.
     *
     * @param allowedEntities A list of entities that will have this upgrade.
     *                        If the list is empty, then all the entities will receive this upgrade.
     */
    SpawnerUpgrade createDefault(List<String> allowedEntities);

    /**
     * Get an upgrade by its name.
     *
     * @param name The name of the upgrade.
     */
    SpawnerUpgrade getUpgrade(String name);

    /**
     * Get an upgrade by its id.
     *
     * @param id The id of the upgrade.
     */
    SpawnerUpgrade getUpgrade(int id);

    /**
     * Get the default upgrade of spawners.
     * This simulates vanilla's spawner settings.
     *
     * @param entityType The entity type of the spawner. Set to null if you want the global default.
     */
    SpawnerUpgrade getDefaultUpgrade(EntityType entityType);

    /**
     * Get all the upgrades available.
     * This doesn't include the default upgrades.
     */
    List<SpawnerUpgrade> getAllUpgrades();

    /**
     * Remove an upgrade from cache.
     *
     * @param spawnerUpgrade The upgrade to remove.
     */
    void removeUpgrade(SpawnerUpgrade spawnerUpgrade);

    /**
     * Remove all upgrades from cache.
     */
    void removeAllUpgrades();

}
