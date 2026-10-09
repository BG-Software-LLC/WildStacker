package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import org.bukkit.Chunk;
import org.bukkit.Location;

public interface NMSSpawners {

    void updateStackedSpawners(Chunk chunk);

    void updateStackedSpawner(StackedSpawner stackedSpawner);

    void registerSpawnConditions();

    void setSpawnerDelay(Location location, int spawnDelay);

    void updateSpawner(Location location, SpawnerUpgrade spawnerUpgrade);

    SpawnerCachedData readData(Location location);

}
