package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import me.derpy.bosses.events.BossSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class MoreBossesListener implements Listener {

    @EventHandler
    public void onBossSpawn(BossSpawnEvent e){
        if(EntityUtils.isStackable(e.getEntity()))
            WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.MORE_BOSSES);
    }

}
