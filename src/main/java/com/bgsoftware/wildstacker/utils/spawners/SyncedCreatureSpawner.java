package com.bgsoftware.wildstacker.utils.spawners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.block.CreatureSpawner;

public interface SyncedCreatureSpawner extends CreatureSpawner {

    WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    static SyncedCreatureSpawner of(CreatureSpawner creatureSpawner){
        return plugin.getNMSAdapter().createSyncedSpawner(creatureSpawner);
    }

    int getRequiredPlayerRange();

}
