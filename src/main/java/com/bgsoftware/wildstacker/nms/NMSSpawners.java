package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import org.bukkit.block.CreatureSpawner;

public interface NMSSpawners {

    boolean updateStackedSpawner(StackedSpawner stackedSpawner);

    void registerSpawnConditions();

    SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner);

}
