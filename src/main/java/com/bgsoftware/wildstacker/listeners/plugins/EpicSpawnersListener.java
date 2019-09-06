package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Executor;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class EpicSpawnersListener implements Listener {

    private static WildStackerPlugin plugin;

    public static void register(WildStackerPlugin plugin){
        EpicSpawnersListener.plugin = plugin;
        if(Bukkit.getPluginManager().getPlugin("EpicSpawners").getDescription().getVersion().startsWith("6"))
            Bukkit.getPluginManager().registerEvents(new EpicSpawners6Listener(), plugin);
        else
            Bukkit.getPluginManager().registerEvents(new EpicSpawners5Listener(), plugin);
    }

    private static class EpicSpawners6Listener implements Listener{

        @EventHandler
        public void onSpawnerSpawn(SpawnerSpawnEvent e){
            if(!(e.getEntity() instanceof LivingEntity))
                return;

            StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

            boolean canBeStacked = plugin.getSettings().entitiesStackingEnabled && stackedEntity.isWhitelisted() && !stackedEntity.isBlacklisted() && !stackedEntity.isWorldDisabled();

            if(!canBeStacked) {
                plugin.getDataHandler().CACHED_SPAWN_CAUSE_ENTITIES.put(stackedEntity.getUniqueId(), SpawnCause.EPIC_SPAWNERS);
            }

            else{
                stackedEntity.setSpawnCause(SpawnCause.EPIC_SPAWNERS);

                StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawner().getCreatureSpawner());

                //It takes 1 tick for EpicSpawners to set the metadata for the mobs.
                Executor.sync(() -> stackedEntity.runSpawnerStackAsync(stackedSpawner, null), 2L);
            }
        }
    }

    private static class EpicSpawners5Listener implements Listener{

        private Method getSpawnerMethod;

        EpicSpawners5Listener(){
            try {
                getSpawnerMethod = SpawnerSpawnEvent.class.getMethod("getSpawner");
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        @EventHandler
        public void onSpawnerSpawn(SpawnerSpawnEvent e){
            if(!(e.getEntity() instanceof LivingEntity))
                return;

            StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());

            boolean canBeStacked = plugin.getSettings().entitiesStackingEnabled && stackedEntity.isWhitelisted() && !stackedEntity.isBlacklisted() && !stackedEntity.isWorldDisabled();

            if(!canBeStacked) {
                plugin.getDataHandler().CACHED_SPAWN_CAUSE_ENTITIES.put(stackedEntity.getUniqueId(), SpawnCause.EPIC_SPAWNERS);
            }

            else{
                stackedEntity.setSpawnCause(SpawnCause.EPIC_SPAWNERS);

                com.songoda.epicspawners.api.spawner.Spawner epicSpawnersSpawner = getSpawner(e);

                if(epicSpawnersSpawner != null) {
                    StackedSpawner stackedSpawner = WStackedSpawner.of(epicSpawnersSpawner.getCreatureSpawner());

                    //It takes 1 tick for EpicSpawners to set the metadata for the mobs.
                    Executor.sync(() -> stackedEntity.runSpawnerStackAsync(stackedSpawner, null), 2L);
                }
            }
        }

        private com.songoda.epicspawners.api.spawner.Spawner getSpawner(SpawnerSpawnEvent e){
            try {
                return (com.songoda.epicspawners.api.spawner.Spawner) getSpawnerMethod.invoke(e);
            }catch(Exception ex){
                ex.printStackTrace();
                return null;
            }
        }

    }

}
