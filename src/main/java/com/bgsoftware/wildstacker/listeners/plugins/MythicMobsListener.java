package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class MythicMobsListener implements Listener {

    @EventHandler
    public void onMythicMobSpawn(MythicMobSpawnEvent e){
        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
        stackedEntity.setSpawnCause(SpawnCause.MYTHIC_MOBS);
    }

}
