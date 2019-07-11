package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class MythicMobsListener implements Listener {

    private WildStackerPlugin plugin;

    public MythicMobsListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onMythicMobSpawn(MythicMobSpawnEvent e){
        StackedEntity stackedEntity = WStackedEntity.of(e.getEntity());
        if(plugin.getSettings().entitiesStackingEnabled && stackedEntity.isWhitelisted() && !stackedEntity.isBlacklisted() && !stackedEntity.isWorldDisabled()) {
            stackedEntity.setSpawnCause(SpawnCause.MYTHIC_MOBS);
        }else {
            plugin.getDataHandler().CACHED_SPAWN_CAUSE_ENTITIES.put(stackedEntity.getUniqueId(), SpawnCause.MYTHIC_MOBS);
        }
    }

}
