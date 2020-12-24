package com.bgsoftware.wildstacker.utils.spawners;

public final class SpawnerCachedData {

    private final int minSpawnDelay, maxSpawnDelay, spawnCount, maxNearbyEntities, requiredPlayerRange, spawnRange, ticksLeft;
    private final String failureReason;

    public SpawnerCachedData(int minSpawnDelay, int maxSpawnDelay, int spawnCount, int maxNearbyEntities,
                             int requiredPlayerRange, int spawnRange, int ticksLeft){
        this(minSpawnDelay, maxSpawnDelay, spawnCount, maxNearbyEntities, requiredPlayerRange,
                spawnRange, ticksLeft, "");
    }

    public SpawnerCachedData(int minSpawnDelay, int maxSpawnDelay, int spawnCount, int maxNearbyEntities,
                             int requiredPlayerRange, int spawnRange, int ticksLeft, String failureReason){
        this.minSpawnDelay = minSpawnDelay;
        this.maxSpawnDelay = maxSpawnDelay;
        this.spawnCount = spawnCount;
        this.maxNearbyEntities = maxNearbyEntities;
        this.requiredPlayerRange = requiredPlayerRange;
        this.spawnRange = spawnRange;
        this.ticksLeft = ticksLeft;
        this.failureReason = failureReason == null ? "" : failureReason;
    }

    public int getMinSpawnDelay() {
        return minSpawnDelay;
    }

    public int getMaxSpawnDelay() {
        return maxSpawnDelay;
    }

    public int getSpawnCount() {
        return spawnCount;
    }

    public int getMaxNearbyEntities() {
        return maxNearbyEntities;
    }

    public int getRequiredPlayerRange() {
        return requiredPlayerRange;
    }

    public int getSpawnRange() {
        return spawnRange;
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    public String getFailureReason() {
        return failureReason;
    }

}
