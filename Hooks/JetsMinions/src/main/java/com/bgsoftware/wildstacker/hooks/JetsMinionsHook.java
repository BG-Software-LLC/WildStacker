package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.stacker.entities.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import me.jet315.minions.events.SlayerSlayEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class JetsMinionsHook {

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onSlayerAction(SlayerSlayEvent e) {
                e.getEntitiesToKill().removeIf(livingEntity -> EntityUtils.isStackable(livingEntity) &&
                        WStackedEntity.of(livingEntity).hasFlag(EntityFlag.DEAD_ENTITY));
            }
        }, plugin);
    }

}
