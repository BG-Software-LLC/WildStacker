package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import org.bukkit.Chunk;
import org.bukkit.block.CreatureSpawner;

public interface NMSSpawners {

    void updateStackedSpawners(Chunk chunk);

    void updateStackedSpawner(StackedSpawner stackedSpawner);

    void registerSpawnConditions();

    SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner);

    void updateSpawner(CreatureSpawner creatureSpawner, SpawnerUpgrade spawnerUpgrade);

    SpawnerCachedData readData(CreatureSpawner creatureSpawner);

}
