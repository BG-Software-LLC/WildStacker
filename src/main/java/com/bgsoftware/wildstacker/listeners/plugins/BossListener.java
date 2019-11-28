package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mineacademy.boss.api.event.BossSpawnEvent;

public final class BossListener implements Listener {

    @EventHandler
    public void onBossSpawn(BossSpawnEvent e){
        if(EntityUtils.isStackable(e.getEntity()))
            Executor.sync(() -> WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.BOSS), 2L);
    }

}
