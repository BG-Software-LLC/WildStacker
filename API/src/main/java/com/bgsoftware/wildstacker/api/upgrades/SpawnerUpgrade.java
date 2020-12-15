package com.bgsoftware.wildstacker.api.upgrades;

import org.bukkit.entity.EntityType;

import java.util.List;

public interface SpawnerUpgrade {

    /**
     * Get the name of the upgrade.
     */
    String getName();

    /**
     * Get the id of the upgrade.
     */
    int getId();

    /**
     * Get the cost of the upgrade.
     */
    double getCost();

    /**
     * Set the cost of the upgrade.
     * If you want this upgrade to be free of charge, use 0 or below.
     * @param cost The cost to set.
     */
    void setCost(double cost);

    /**
     * Get the display name of the upgrade.
     */
    String getDisplayName();

    /**
     * Set the display name of the upgrade.
     * @param displayName The display name.
     */
    void setDisplayName(String displayName);

    /**
     * Check if an entity is allowed to be upgraded to this upgrade.
     * @param entityType The type of the entity.
     */
    boolean isEntityAllowed(EntityType entityType);

    /**
     * Set all the allowed entities for this upgrade.
     * If the list is null or empty, all the entities will be allowed.
     * @param allowedEntities The allowed entities.
     */
    void setAllowedEntities(List<String> allowedEntities);

    /**
     * Get the minimum spawn delay of spawners.
     */
    int getMinSpawnDelay();

    /**
     * Set the minimum spawn delay of spawners.
     * @param minSpawnDelay The minimum spawn delay.
     */
    void setMinSpawnDelay(int minSpawnDelay);

    /**
     * Get the maximum spawn delay of spawners.
     */
    int getMaxSpawnDelay();

    /**
     * Set the maximum spawn delay of spwaners.
     * @param maxSpawnDelay The maximum spawn delay.
     */
    void setMaxSpawnDelay(int maxSpawnDelay);

    /**
     * Get the entities spawn count of spawners.
     */
    int getSpawnCount();

    /**
     * Set the maximum spawn count of spawners.
     * @param spawnCount The spawn count.
     */
    void setSpawnCount(int spawnCount);

    /**
     * Get the maximum nearby entities allowed around spawners.
     */
    int getMaxNearbyEntities();

    /**
     * Set the maximum nearby entities allowed around spawners.
     * @param maxNearbyEntities The maximum allowed nearby entities.
     */
    void setMaxNearbyEntities(int maxNearbyEntities);

    /**
     * Get the required player range from spawners.
     */
    int getRequiredPlayerRange();

    /**
     * Set the required player range from spawners.
     * @param requiredPlayerRange The required player range.
     */
    void setRequiredPlayerRange(int requiredPlayerRange);

    /**
     * Get the spawn range of spawners.
     */
    int getSpawnRange();

    /**
     * Set the spawn range of spawners.
     * @param spawnRange The spawn range.
     */
    void setSpawnRange(int spawnRange);

}
