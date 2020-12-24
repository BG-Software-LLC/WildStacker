package com.bgsoftware.wildstacker.utils.spawners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import org.bukkit.block.CreatureSpawner;

public interface SyncedCreatureSpawner extends CreatureSpawner {

    WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    static SyncedCreatureSpawner of(CreatureSpawner creatureSpawner){
        return creatureSpawner instanceof SyncedCreatureSpawner ? (SyncedCreatureSpawner) creatureSpawner :
                plugin.getNMSAdapter().createSyncedSpawner(creatureSpawner);
    }

    void updateSpawner(SpawnerUpgrade spawnerUpgrade);

    SpawnerCachedData readData();

}
