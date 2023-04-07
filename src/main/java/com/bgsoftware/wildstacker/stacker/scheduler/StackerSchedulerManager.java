package com.bgsoftware.wildstacker.stacker.scheduler;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.stacker.IScheduledStackedObject;
import com.bgsoftware.wildstacker.utils.Holder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StackerSchedulerManager<T extends IScheduledStackedObject> {

    private final Map<UUID, Map<Long, Holder<StackerScheduler<T>>>> schedulers = new HashMap<>();
    private BukkitTask cleanTask;
    private boolean stopped = false;

    public StackerSchedulerManager(WildStackerPlugin plugin) {
        this.cleanTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkSchedulers, 20 * 300L, 20 * 300L);
    }

    public Holder<StackerScheduler<T>> getSchedulerForChunk(Location location) {
        ensureActive("Called getSchedulerForChunk on an already stopped scheduler manager.");
        return getSchedulerForChunk(location, true);
    }

    private Holder<StackerScheduler<T>> getSchedulerForChunk(Location location, boolean runChunkLoadIfNotExists) {
        UUID worldUUID = location.getWorld().getUID();

        Map<Long, Holder<StackerScheduler<T>>> worldSchedulers = schedulers.get(worldUUID);

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        if (worldSchedulers != null) {
            Holder<StackerScheduler<T>> schedulerHolder = worldSchedulers.get(pair(chunkX, chunkZ));
            if (schedulerHolder != null && !schedulerHolder.getHandle().isStopped())
                return schedulerHolder;
        }

        if (!runChunkLoadIfNotExists)
            throw new IllegalStateException("Tried to get chunk scheduler on a chunk that does not exist: " + location.getWorld().getName() + ", " + chunkX + ", " + chunkZ);

        onChunkLoadInternal(location.getWorld(), chunkX, chunkZ);

        return getSchedulerForChunk(location, false);
    }

    public void onChunkLoad(Chunk chunk) {
        ensureActive("Called onChunkLoad on an already stopped scheduler manager.");
        onChunkLoadInternal(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    private void onChunkLoadInternal(World world, int chunkX, int chunkZ) {
        Map<Long, Holder<StackerScheduler<T>>> worldSchedulers = schedulers.get(world.getUID());

        long chunkCoords = pair(chunkX, chunkZ);

        if (worldSchedulers == null) {
            worldSchedulers = new HashMap<>();
            schedulers.put(world.getUID(), worldSchedulers);
            Holder<StackerScheduler<T>> scheduler = new Holder<>(new StackerScheduler<>());
            scheduler.addHolder(chunkCoords);
            worldSchedulers.put(chunkCoords, scheduler);
            return;
        }

        Holder<StackerScheduler<T>> chunkScheduler = worldSchedulers.get(chunkCoords);

        if (chunkScheduler != null) {
            if (!chunkScheduler.getHandle().isStopped())
                return;
            chunkScheduler = null;
        }

        // We look for nearby schedulers of the chunk.
        for (int chunkXOffset = -1; chunkXOffset <= 1; ++chunkXOffset) {
            for (int chunkZOffset = -1; chunkZOffset <= 1; ++chunkZOffset) {
                long nearbyChunkCoords = pair(chunkX + chunkXOffset, chunkZ + chunkZOffset);
                Holder<StackerScheduler<T>> nearbyChunkScheduler = worldSchedulers.get(nearbyChunkCoords);
                if (nearbyChunkScheduler != null && !nearbyChunkScheduler.getHandle().isStopped()) {
                    chunkScheduler = nearbyChunkScheduler;
                    break;
                }
            }
        }

        if (chunkScheduler == null) {
            chunkScheduler = new Holder<>(new StackerScheduler<>());
        }

        // We change all neighbors to have the same scheduler.
        for (int chunkXOffset = -1; chunkXOffset <= 1; ++chunkXOffset) {
            for (int chunkZOffset = -1; chunkZOffset <= 1; ++chunkZOffset) {
                long nearbyChunkCoords = pair(chunkX + chunkXOffset, chunkZ + chunkZOffset);
                Holder<StackerScheduler<T>> nearbyChunkScheduler = worldSchedulers.get(nearbyChunkCoords);
                // We want to update the nearby chunk with the new scheduler
                if (nearbyChunkScheduler == null) {
                    worldSchedulers.put(nearbyChunkCoords, chunkScheduler);
                    chunkScheduler.addHolder(nearbyChunkCoords);
                } else if (nearbyChunkScheduler != chunkScheduler) {
                    for (long holder : nearbyChunkScheduler.getHolders()) {
                        worldSchedulers.put(holder, chunkScheduler);
                        chunkScheduler.addHolder(holder);
                    }

                    if (!nearbyChunkScheduler.getHandle().isStopped()) {
                        nearbyChunkScheduler.getHandle().mergeInto(chunkScheduler.getHandle());
                    }
                }
            }
        }
    }

    public void onChunkUnload(Chunk chunk) {
        ensureActive("Called onChunkUnload on an already stopped scheduler manager.");

        Map<Long, Holder<StackerScheduler<T>>> worldSchedulers = schedulers.get(chunk.getWorld().getUID());
        if (worldSchedulers != null) {
            long chunkCoords = pair(chunk.getX(), chunk.getZ());
            Holder<StackerScheduler<T>> oldScheduler = worldSchedulers.remove(chunkCoords);
            if (oldScheduler != null)
                oldScheduler.removeHolder(chunkCoords);
        }
    }

    private void checkSchedulers() {
        ensureActive("Called checkSchedulers on an already stopped scheduler manager.");

        this.schedulers.values().forEach(worldSchedulers -> {
            Iterator<Map.Entry<Long, Holder<StackerScheduler<T>>>> worldSchedulersIterator = worldSchedulers.entrySet().iterator();
            while (worldSchedulersIterator.hasNext()) {
                Map.Entry<Long, Holder<StackerScheduler<T>>> entry = worldSchedulersIterator.next();
                Holder<StackerScheduler<T>> scheduler = entry.getValue();
                if (scheduler.getHandle().isStopped()) {
                    worldSchedulersIterator.remove();
                    scheduler.removeHolder(entry.getKey());
                }
            }
        });
    }

    public int getScheduledSchedulers() {
        Set<StackerScheduler<T>> schedulers = new LinkedHashSet<>();

        this.schedulers.values().forEach(worldSchedulers -> worldSchedulers.values().forEach(schedulerHolder -> {
            schedulers.add(schedulerHolder.getHandle());
        }));

        int count = 0;
        for (StackerScheduler<T> scheduler : schedulers) {
            if (scheduler.isScheduling())
                ++count;
        }

        return count;
    }

    public int getActiveSchedulers() {
        Set<StackerScheduler<T>> schedulers = new LinkedHashSet<>();

        this.schedulers.values().forEach(worldSchedulers -> worldSchedulers.values().forEach(schedulerHolder -> {
            schedulers.add(schedulerHolder.getHandle());
        }));

        int count = 0;
        for (StackerScheduler<T> scheduler : schedulers) {
            if (!scheduler.isStopped())
                ++count;
        }

        return count;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public void stopSchedulers() {
        if (stopped)
            return;

        this.stopped = true;

        if (this.cleanTask != null) {
            this.cleanTask.cancel();
            this.cleanTask = null;
        }

        this.schedulers.values().forEach(worldSchedulers -> worldSchedulers.values().forEach(
                schedulerHolder -> schedulerHolder.getHandle().stop()));
    }

    private void ensureActive(String message) {
        if (this.stopped)
            throw new IllegalStateException(message);
    }

    private static long pair(int x, int z) {
        return ((long) z << 32 | x);
    }

}
