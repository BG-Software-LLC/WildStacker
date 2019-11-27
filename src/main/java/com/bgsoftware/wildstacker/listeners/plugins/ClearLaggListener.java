package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class ClearLaggListener implements Listener {

    private WildStackerPlugin instance;

    public ClearLaggListener(WildStackerPlugin instance){
        this.instance = instance;
    }

    @EventHandler
    public void onClearLaggTask(EntityRemoveEvent e){
        if (instance.getSettings().killTaskSyncClearLagg) {
            instance.getSystemManager().performKillAll(
                    entity -> entity.getWorld().equals(e.getWorld()),
                    item -> item.getWorld().equals(e.getWorld()),
                    true
            );
        }
    }

}
