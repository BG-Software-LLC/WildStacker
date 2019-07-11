package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Executor;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class EpicSpawnersListener implements Listener {

    private WildStackerPlugin plugin;

    public EpicSpawnersListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent e){
        if(!(e.getEntity() instanceof LivingEntity))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

        boolean canBeStacked = plugin.getSettings().entitiesStackingEnabled && stackedEntity.isWhitelisted() && !stackedEntity.isBlacklisted() && !stackedEntity.isWorldDisabled();

        if(!canBeStacked) {
            plugin.getDataHandler().CACHED_SPAWN_CAUSE_ENTITIES.put(stackedEntity.getUniqueId(), SpawnCause.MYTHIC_MOBS);
        }

        else{
            stackedEntity.setSpawnCause(SpawnCause.EPIC_SPAWNERS);

            StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawner().getCreatureSpawner());

            //It takes 1 tick for EpicSpawners to set the metadata for the mobs.
            Executor.sync(() -> stackedEntity.trySpawnerStack(stackedSpawner), 2L);
        }
    }

}
