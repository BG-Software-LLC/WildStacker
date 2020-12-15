package com.bgsoftware.wildstacker.api.handlers;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;

import java.util.List;

public interface UpgradesManager {

    /**
     * Create a new spawner upgrade with a specific name.
     * If the upgrade already exists, it will be returned instead of new one being created.
     * @param name The name of the upgrade.
     * @param id The id of the upgrade
     */
    SpawnerUpgrade createUpgrade(String name, int id);

    /**
     * Get an upgrade by its name.
     * @param name The name of the upgrade.
     */
    SpawnerUpgrade getUpgrade(String name);

    /**
     * Get an upgrade by its id.
     * @param id The id of the upgrade.
     */
    SpawnerUpgrade getUpgrade(int id);

    /**
     * Get the default upgrade of spawners.
     * This simulates vanilla's spawner settings.
     */
    SpawnerUpgrade getDefaultUpgrade();

    /**
     * Get all the upgrades available.
     */
    List<SpawnerUpgrade> getAllUpgrades();

    /**
     * Remove an upgrade from cache.
     * @param spawnerUpgrade The upgrade to remove.
     */
    void removeUpgrade(SpawnerUpgrade spawnerUpgrade);

    /**
     * Remove all upgrades from cache.
     */
    void removeAllUpgrades();

}
