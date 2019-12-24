package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import me.UnioDex.UnioSpawners.api.events.SpawnerSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class UnioSpawnersListener implements Listener {

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (!EntityUtils.isStackable(e.getEntity()))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
        stackedEntity.setSpawnCause(SpawnCause.EPIC_SPAWNERS);

        if (e.getSpawner() != null) {
            StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawner().getCreatureSpawner());

            //It takes 1 tick for UnioSpawners to set the metadata for the mobs.
            Executor.sync(() -> stackedEntity.runSpawnerStackAsync(stackedSpawner, null), 2L);
        }
    }

}
