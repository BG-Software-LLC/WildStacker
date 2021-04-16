package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import me.jet315.minions.events.SlayerSlayEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class JetsMinionsListener implements Listener {

    @EventHandler
    public void onSlayerAction(SlayerSlayEvent e){
        e.getEntitiesToKill().removeIf(livingEntity -> EntityUtils.isStackable(livingEntity) &&
                WStackedEntity.of(livingEntity).hasFlag(EntityFlag.DEAD_ENTITY));
    }

}
