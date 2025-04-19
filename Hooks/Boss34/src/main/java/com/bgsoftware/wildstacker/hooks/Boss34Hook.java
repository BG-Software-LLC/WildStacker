package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.boss.api.event.BossSpawnEvent;

@SuppressWarnings("unused")
public final class Boss34Hook {

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onBossSpawn(BossSpawnEvent e) {
                if (EntityUtils.isStackable(e.getEntity()))
                    Executor.sync(() -> WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.BOSS), 2L);
            }
        }, plugin);
    }

    private static class NewBossListener implements Listener {

        @EventHandler
        public void onBossSpawn(BossSpawnEvent e) {
            if (EntityUtils.isStackable(e.getEntity()))
                Executor.sync(() -> WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.BOSS), 2L);
        }

    }

}
