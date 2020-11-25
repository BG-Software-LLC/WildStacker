package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public final class WorldsListener implements Listener {

    private final WildStackerPlugin plugin;

    public WorldsListener(WildStackerPlugin plugin){
        this.plugin = plugin;
        for(World world : Bukkit.getWorlds())
            plugin.getNMSAdapter().startEntityListen(world);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldInit(WorldInitEvent e){
        plugin.getNMSAdapter().startEntityListen(e.getWorld());
    }

}
