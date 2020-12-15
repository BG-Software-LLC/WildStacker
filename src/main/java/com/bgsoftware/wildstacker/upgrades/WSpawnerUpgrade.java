package com.bgsoftware.wildstacker.upgrades;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

import java.util.List;

public final class WSpawnerUpgrade implements SpawnerUpgrade {

    private final String name;
    private final int id;

    private double cost;
    private String displayName = "";
    private FastEnumArray<EntityType> allowedEntities;

    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    public WSpawnerUpgrade(String name, int id){
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? "" : ChatColor.translateAlternateColorCodes('&', displayName);
    }

    @Override
    public boolean isEntityAllowed(EntityType entityType) {
        return allowedEntities == null || allowedEntities.size() == 0 || allowedEntities.contains(entityType);
    }

    @Override
    public void setAllowedEntities(List<String> allowedEntities) {
        this.allowedEntities = allowedEntities == null ? null : FastEnumArray.fromList(allowedEntities, EntityType.class);
    }

    @Override
    public int getMinSpawnDelay() {
        return minSpawnDelay;
    }

    @Override
    public void setMinSpawnDelay(int minSpawnDelay) {
        this.minSpawnDelay = minSpawnDelay;
    }

    @Override
    public int getMaxSpawnDelay() {
        return maxSpawnDelay;
    }

    @Override
    public void setMaxSpawnDelay(int maxSpawnDelay) {
        this.maxSpawnDelay = maxSpawnDelay;
    }

    @Override
    public int getSpawnCount() {
        return spawnCount;
    }

    @Override
    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    @Override
    public int getMaxNearbyEntities() {
        return maxNearbyEntities;
    }

    @Override
    public void setMaxNearbyEntities(int maxNearbyEntities) {
        this.maxNearbyEntities = maxNearbyEntities;
    }

    @Override
    public int getRequiredPlayerRange() {
        return requiredPlayerRange;
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        this.requiredPlayerRange = requiredPlayerRange;
    }

    @Override
    public int getSpawnRange() {
        return spawnRange;
    }

    @Override
    public void setSpawnRange(int spawnRange) {
        this.spawnRange = spawnRange;
    }

}
