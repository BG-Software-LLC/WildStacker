package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private final WildStackerPlugin plugin;

    public static boolean loadedData = false;

    public ChunksListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e){
        if(loadedData)
            plugin.getSystemManager().handleChunkUnload(e.getChunk());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        if(loadedData)
            plugin.getSystemManager().handleChunkLoad(e.getChunk());
    }

}
