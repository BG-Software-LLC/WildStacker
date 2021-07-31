package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import net.splodgebox.elitebosses.events.BossSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class EliteBossesListener implements Listener {

    @EventHandler
    public void onBossSpawn(BossSpawnEvent e) {
        if (EntityUtils.isStackable(e.getBoss()))
            WStackedEntity.of(e.getBoss()).setSpawnCause(SpawnCause.ELITE_BOSSES);
    }

}
