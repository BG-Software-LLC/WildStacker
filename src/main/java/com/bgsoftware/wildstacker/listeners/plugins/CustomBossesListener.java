package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import net.aminecraftdev.custombosses.events.BossSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class CustomBossesListener implements Listener {

    @EventHandler
    public void onBossSpawn(BossSpawnEvent e){
        StackedEntity stackedEntity = WStackedEntity.of(e.getBoss());
        stackedEntity.setSpawnCause(SpawnCause.CUSTOM_BOSSES);
    }

}
