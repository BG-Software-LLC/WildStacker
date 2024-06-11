package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class EpicSpawners5Hook {

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onSpawnerSpawn(SpawnerSpawnEvent e) {
                if (!EntityUtils.isStackable(e.getEntity()))
                    return;

                StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
                stackedEntity.setSpawnCause(SpawnCause.EPIC_SPAWNERS);

                StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawner().getCreatureSpawner());

                //It takes 1 tick for EpicSpawners to set the metadata for the mobs.
                Scheduler.runTask(e.getEntity(), () -> stackedEntity.runSpawnerStackAsync(stackedSpawner, null), 2L);
            }
        }, plugin);
    }

}
