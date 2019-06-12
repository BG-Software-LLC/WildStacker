package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private WildStackerPlugin plugin;

    public ChunksListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    public void onChunkLoad(ChunkLoadEvent e){
//        plugin.getDataHandler().loadChunkData(e.getChunk());
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    public void onChunkUnload(ChunkUnloadEvent e){
//        plugin.getDataHandler().saveChunkData(e.getChunk(), true, true);
//    }

}
