package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class BossListener {

    public static void register(WildStackerPlugin plugin){
        try{
            Class.forName("org.mineacademy.boss.api.event.BossSpawnEvent");
            plugin.getServer().getPluginManager().registerEvents(new OldBossListener(), plugin);
        }catch(Throwable ex){
            plugin.getServer().getPluginManager().registerEvents(new NewBossListener(), plugin);
        }
    }

    private static class OldBossListener implements Listener {

        @EventHandler
        public void onBossSpawn(org.mineacademy.boss.api.event.BossSpawnEvent e) {
            if (EntityUtils.isStackable(e.getEntity()))
                Executor.sync(() -> WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.BOSS), 2L);
        }

    }

    private static class NewBossListener implements Listener {

        @EventHandler
        public void onBossSpawn(org.mineacademy.boss.lib.boss.api.event.BossSpawnEvent e) {
            if (EntityUtils.isStackable(e.getEntity()))
                Executor.sync(() -> WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.BOSS), 2L);
        }

    }

}
