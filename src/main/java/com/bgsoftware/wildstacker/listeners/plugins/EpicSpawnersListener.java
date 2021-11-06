package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class EpicSpawnersListener implements Listener {

    public static void register(WildStackerPlugin plugin) {
        PluginHooks.isEpicSpawnersEnabled = true;
        String version = Bukkit.getPluginManager().getPlugin("EpicSpawners").getDescription().getVersion();
        Listener listener;

        try {
            if (version.startsWith("5")) {
                listener = (Listener) Class.forName("com.bgsoftware.wildstacker.listeners.plugins.EpicSpawners5Listener").newInstance();
            } else if (version.startsWith("6")) {
                listener = (Listener) Class.forName("com.bgsoftware.wildstacker.listeners.plugins.EpicSpawners6Listener").newInstance();
            } else {
                listener = new EpicSpawners7Listener();
            }
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class EpicSpawners7Listener implements Listener {

        @EventHandler
        public void onSpawnerSpawn(SpawnerSpawnEvent e) {
            if (!EntityUtils.isStackable(e.getEntity()))
                return;

            StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
            stackedEntity.setSpawnCause(SpawnCause.EPIC_SPAWNERS);

            StackedSpawner stackedSpawner = WStackedSpawner.of(e.getSpawner().getCreatureSpawner());

            //It takes 1 tick for EpicSpawners to set the metadata for the mobs.
            Executor.sync(() -> stackedEntity.runSpawnerStackAsync(stackedSpawner, null), 2L);
        }
    }

}
