package com.bgsoftware.wildstacker.stacker.scheduler;

import com.bgsoftware.wildstacker.stacker.WStackedObject;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class StackerSchedulerManager<T extends WStackedObject<?>> {

    private final Map<UUID, Map<Long, SchedulerEntry>> schedulers = new HashMap<>();
    private boolean stopped = false;

    public StackerSchedulerManager() {

    }

    @Nullable
    public StackerScheduler<T> getSchedulerForChunk(Location location) {
        ensureActive("Called getSchedulerForChunk on an already stopped scheduler manager.");

        UUID worldUUID = location.getWorld().getUID();

        Map<Long, SchedulerEntry> worldSchedulers = schedulers.get(worldUUID);
        if (worldSchedulers == null)
            return null;

        SchedulerEntry schedulerEntry = worldSchedulers.get(pair(location.getBlockX() >> 4, location.getBlockZ() >> 4));

        return schedulerEntry == null ? null : schedulerEntry.scheduler;
    }

    public void onChunkLoad(Chunk chunk) {
        ensureActive("Called onChunkLoad on an already stopped scheduler manager.");

        Map<Long, SchedulerEntry> worldSchedulers = schedulers.get(chunk.getWorld().getUID());

        if (worldSchedulers == null) {
            worldSchedulers = new HashMap<>();
            schedulers.put(chunk.getWorld().getUID(), worldSchedulers);
            long chunkCoords = pair(chunk.getX(), chunk.getZ());
            worldSchedulers.put(chunkCoords, new SchedulerEntry(chunkCoords, new StackerScheduler<>()));
            return;
        }

        long chunkCoords = pair(chunk.getX(), chunk.getZ());
        if (worldSchedulers.get(chunkCoords) != null)
            return;


        // We look for nearby schedulers of the chunk.
        SchedulerEntry chunkEntry = null;

        for (int chunkX = -1; chunkX <= 1; ++chunkX) {
            for (int chunkZ = -1; chunkZ <= 1; ++chunkZ) {
                SchedulerEntry nearbyChunkEntry = worldSchedulers.get(pair(chunk.getX() + chunkX, chunk.getZ() + chunkZ));
                if (nearbyChunkEntry != null && !nearbyChunkEntry.scheduler.isStopped()) {
                    chunkEntry = nearbyChunkEntry;
                    break;
                }
            }
        }

        if (chunkEntry == null) {
            chunkEntry = new SchedulerEntry(chunkCoords, new StackerScheduler<>());
        }

        // We change all neighbors to have the same scheduler.
        for (int chunkX = -1; chunkX <= 1; ++chunkX) {
            for (int chunkZ = -1; chunkZ <= 1; ++chunkZ) {
                long nearbyChunkCoords = pair(chunk.getX() + chunkX, chunk.getZ() + chunkZ);
                SchedulerEntry nearbyChunkEntry = worldSchedulers.get(nearbyChunkCoords);
                // We want to update the nearby chunk with the new scheduler
                if (nearbyChunkEntry == null) {
                    worldSchedulers.put(nearbyChunkCoords, new SchedulerEntry(nearbyChunkCoords, chunkEntry.scheduler));
                    chunkEntry.scheduler.addRefCount();
                } else if (nearbyChunkEntry.scheduler != chunkEntry.scheduler) {
                    StackerScheduler<T> oldScheduler = nearbyChunkEntry.setScheduler(chunkEntry.scheduler);
                    if (!oldScheduler.isStopped()) {
                        oldScheduler.mergeInto(chunkEntry.scheduler);
                    }
                }
            }
        }
    }

    public void onChunkUnload(Chunk chunk) {
        ensureActive("Called onChunkUnload on an already stopped scheduler manager.");

        Map<Long, SchedulerEntry> worldSchedulers = schedulers.get(chunk.getWorld().getUID());
        if (worldSchedulers != null) {
            SchedulerEntry schedulerEntry = worldSchedulers.remove(pair(chunk.getX(), chunk.getZ()));
            if (schedulerEntry != null)
                schedulerEntry.scheduler.removeRefCount();
        }
    }

    public void checkSchedulers() {
        ensureActive("Called checkSchedulers on an already stopped scheduler manager.");

        this.schedulers.values().forEach(worldSchedulers -> {
            Iterator<Map.Entry<Long, SchedulerEntry>> worldSchedulersIterator = worldSchedulers.entrySet().iterator();
            while (worldSchedulersIterator.hasNext()) {
                Map.Entry<Long, SchedulerEntry> entry = worldSchedulersIterator.next();
                SchedulerEntry schedulerEntry = entry.getValue();
                if (schedulerEntry.scheduler.checkInactive()) {
                    worldSchedulersIterator.remove();
                }
            }
        });
    }

    public void stopSchedulers() {
        if (stopped)
            return;

        this.stopped = true;
        this.schedulers.values().forEach(worldSchedulers -> worldSchedulers.values().forEach(schedulerEntry -> {
            schedulerEntry.scheduler.stop();
        }));
    }

    private void ensureActive(String message) {
        if (this.stopped)
            throw new IllegalStateException(message);
    }

    private static long pair(int x, int z) {
        return ((long) z << 32 | x);
    }

    private static String chunkCoords(World world, long pair) {
        int chunkX = (int) pair;
        int chunkZ = (int) (pair >> 32);
        return world.getName() + ", " + chunkX + ", " + chunkZ;
    }

    private class SchedulerEntry {

        private final long chunkCoords;
        private StackerScheduler<T> scheduler;

        SchedulerEntry(long chunkCoords, StackerScheduler<T> scheduler) {
            this.chunkCoords = chunkCoords;
            this.scheduler = scheduler;
        }

        public StackerScheduler<T> setScheduler(StackerScheduler<T> newScheduler) {
            StackerScheduler<T> oldScheduler = this.scheduler;
            this.scheduler = newScheduler;
            return oldScheduler;
        }
    }

}
