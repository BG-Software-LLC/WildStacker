package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class ClearLaggListener implements Listener {

    private final WildStackerPlugin plugin;

    private long lastTime = 0;

    public ClearLaggListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onClearLaggTask(EntityRemoveEvent e){
        e.getEntityList().forEach(entity -> {
            if(EntityUtils.isStackable(entity))
                WStackedEntity.of(entity).remove();
        });
        if (plugin.getSettings().killTaskSyncClearLagg && System.currentTimeMillis() - lastTime > 1000) {
            lastTime = System.currentTimeMillis();
            plugin.getSystemManager().performKillAll(true);
        }
    }

}
