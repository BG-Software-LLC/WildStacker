package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import static com.bgsoftware.wildstacker.handlers.SystemHandler.CHUNK_FULL_STAGE;

public final class ChunksListener implements Listener {

    private final WildStackerPlugin plugin;

    public ChunksListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e) {
        plugin.getSystemManager().handleChunkUnload(e.getChunk(), CHUNK_FULL_STAGE);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        plugin.getSystemManager().handleChunkLoad(e.getChunk(), CHUNK_FULL_STAGE);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent e) {
        for (Chunk chunk : e.getWorld().getLoadedChunks()) {
            plugin.getSystemManager().handleChunkUnload(chunk, CHUNK_FULL_STAGE);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent e) {
        for (Chunk chunk : e.getWorld().getLoadedChunks()) {
            plugin.getSystemManager().handleChunkLoad(chunk, CHUNK_FULL_STAGE);
        }
    }

}
