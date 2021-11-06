package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.wildstacker.hooks.SuperiorSkyblockHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class SuperiorSkyblockListener implements Listener {

    @EventHandler
    public void onPluginInit(PluginInitializeEvent e){
        SuperiorSkyblockHook.registerIslandFlag();
    }

}
