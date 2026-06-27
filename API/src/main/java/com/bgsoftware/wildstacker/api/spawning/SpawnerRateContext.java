package com.bgsoftware.wildstacker.api.spawning;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public final class SpawnerRateContext {

    private final StackedSpawner spawner;
    private final Location location;
    private final EntityType entityType;
    private final int stackAmount;
    private final int baseTickDelay;
    private final int currentSpawnDelay;

    public SpawnerRateContext(StackedSpawner spawner, Location location, EntityType entityType,
                              int stackAmount, int baseTickDelay, int currentSpawnDelay) {
        this.spawner = spawner;
        this.location = location;
        this.entityType = entityType;
        this.stackAmount = stackAmount;
        this.baseTickDelay = baseTickDelay;
        this.currentSpawnDelay = currentSpawnDelay;
    }

    public StackedSpawner getSpawner() {
        return spawner;
    }

    public Location getLocation() {
        return location;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getStackAmount() {
        return stackAmount;
    }

    public int getBaseTickDelay() {
        return baseTickDelay;
    }

    public int getCurrentSpawnDelay() {
        return currentSpawnDelay;
    }

}
