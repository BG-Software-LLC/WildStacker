package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import net.aminecraftdev.custombosses.events.BossSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class CustomBossesListener implements Listener {

    private WildStackerPlugin plugin;

    public CustomBossesListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBossSpawn(BossSpawnEvent e){
        StackedEntity stackedEntity = WStackedEntity.of(e.getBoss());
        if(plugin.getSettings().entitiesStackingEnabled && stackedEntity.isWhitelisted() && !stackedEntity.isBlacklisted() && !stackedEntity.isWorldDisabled()) {
            stackedEntity.setSpawnCause(SpawnCause.CUSTOM_BOSSES);
        }else {
            plugin.getDataHandler().CACHED_SPAWN_CAUSE_ENTITIES.put(stackedEntity.getUniqueId(), SpawnCause.CUSTOM_BOSSES);
        }
    }

}
