package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class ClearLaggListener implements Listener {

    private WildStackerPlugin instance;

    private long lastTime = 0;

    public ClearLaggListener(WildStackerPlugin instance){
        this.instance = instance;
    }

    @EventHandler
    public void onClearLaggTask(EntityRemoveEvent e){
        if (instance.getSettings().clearLaggHookEnabled && System.currentTimeMillis() - lastTime > 1000) {
            lastTime = System.currentTimeMillis();
            instance.getSystemManager().performKillAll();
        }
    }

}
