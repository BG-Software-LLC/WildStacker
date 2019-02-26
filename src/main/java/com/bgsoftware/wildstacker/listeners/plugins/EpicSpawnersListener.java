package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class EpicSpawnersListener implements Listener {

    private WildStackerPlugin instance;

    public EpicSpawnersListener(WildStackerPlugin instance){
        this.instance = instance;
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent e){
        if(!(e.getEntity() instanceof LivingEntity))
            return;

        if(instance.getSettings().blacklistedEntities.contains(e.getEntityType().name()) ||
                instance.getSettings().blacklistedEntitiesSpawnReasons.contains("SPAWNER"))
            return;

        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
        StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawner().getCreatureSpawner());

        //It takes 1 tick for EpicSpawners to set the metadata for the mobs.
        Bukkit.getScheduler().runTaskLater(instance, () -> stackedEntity.trySpawnerStack(stackedSpawner), 2L);
    }

}
