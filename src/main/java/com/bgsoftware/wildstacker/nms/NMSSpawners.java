package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import org.bukkit.Chunk;
import org.bukkit.block.CreatureSpawner;

public interface NMSSpawners {

    void updateStackedSpawners(Chunk chunk);

    void updateStackedSpawner(StackedSpawner stackedSpawner);

    void registerSpawnConditions();

    SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner);

}
