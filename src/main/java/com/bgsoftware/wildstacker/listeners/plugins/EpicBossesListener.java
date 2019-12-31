package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.songoda.epicbosses.events.BossSkillEvent;
import com.songoda.epicbosses.events.BossSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class EpicBossesListener implements Listener {

    public EpicBossesListener(){
        PluginHooks.isEpicBossesEnabled = true;
    }

    @EventHandler
    public void onBossSpawn(BossSpawnEvent e){
        LivingEntity livingEntity = e.getActiveBossHolder().getLivingEntity();
        if(EntityUtils.isStackable(livingEntity))
            WStackedEntity.of(livingEntity).setSpawnCause(SpawnCause.EPIC_BOSSES);
    }

    @EventHandler
    public void onBossSkill(BossSkillEvent e){
        if(!e.getSkill().getDisplayName().equals("Minions"))
            return;

        e.getActiveBossHolder().getActiveMinionHolderMap().values().forEach(activeMinionHolder ->
            activeMinionHolder.getLivingEntityMap().keySet().forEach(position -> {
                LivingEntity livingEntity = activeMinionHolder.getLivingEntity(position);
                if(EntityUtils.isStackable(livingEntity))
                    WStackedEntity.of(livingEntity).setSpawnCause(SpawnCause.EPIC_BOSSES_MINION);
            }));
    }

}
