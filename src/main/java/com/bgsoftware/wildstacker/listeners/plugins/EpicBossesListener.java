package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.songoda.epicbosses.events.BossSkillEvent;
import com.songoda.epicbosses.events.BossSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class EpicBossesListener implements Listener {

    private WildStackerPlugin plugin;

    public EpicBossesListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBossSpawn(BossSpawnEvent e){
        StackedEntity stackedEntity = WStackedEntity.of(e.getActiveBossHolder().getLivingEntity());
        if(plugin.getSettings().entitiesStackingEnabled && stackedEntity.isWhitelisted() && !stackedEntity.isBlacklisted() && !stackedEntity.isWorldDisabled()) {
            stackedEntity.setSpawnCause(SpawnCause.EPIC_BOSSES);
        }else {
            plugin.getDataHandler().CACHED_SPAWN_CAUSE_ENTITIES.put(stackedEntity.getUniqueId(), SpawnCause.EPIC_BOSSES);
        }
    }

    @EventHandler
    public void onBossSkill(BossSkillEvent e){
        if(!e.getSkill().getDisplayName().equals("Minions"))
            return;

        e.getActiveBossHolder().getActiveMinionHolderMap().values().forEach(activeMinionHolder -> {
            activeMinionHolder.getLivingEntityMap().keySet().forEach(position -> {
                LivingEntity livingEntity = activeMinionHolder.getLivingEntity(position);
                if(livingEntity != null)
                    WStackedEntity.of(livingEntity).setSpawnCause(SpawnCause.EPIC_BOSSES_MINION);
            });
        });
    }

}
