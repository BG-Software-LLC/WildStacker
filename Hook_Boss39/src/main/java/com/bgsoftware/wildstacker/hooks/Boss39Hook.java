package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.boss.api.event.BossPostSpawnEvent;

@SuppressWarnings("unused")
public final class Boss39Hook {

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onBossSpawn(BossPostSpawnEvent e) {
                if (EntityUtils.isStackable(e.getEntity()))
                    Executor.sync(() -> WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.BOSS), 2L);
            }
        }, plugin);
    }

}
