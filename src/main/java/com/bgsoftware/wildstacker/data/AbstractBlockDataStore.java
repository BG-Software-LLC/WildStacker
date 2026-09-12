package com.bgsoftware.wildstacker.data;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.data.structures.Chunk2ObjectMap;
import com.bgsoftware.wildstacker.utils.data.structures.Location2ObjectMap;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public abstract class AbstractBlockDataStore<T extends StackedObject<?>, U extends Location2ObjectMap.ILocationEntity> {

    private final Location2ObjectMap<T> store = new Location2ObjectMap<>();
    private final ReadWriteLock storeLock = new ReentrantReadWriteLock();

    private final Chunk2ObjectMap<Set<T>> storeByChunks = new Chunk2ObjectMap<>();
    private final ReadWriteLock storeByChunksLock = new ReentrantReadWriteLock();

    private final Chunk2ObjectMap<Set<U>> unloadedStore = new Chunk2ObjectMap<>();
    private final ReadWriteLock unloadedStoreLock = new ReentrantReadWriteLock();
    private int unloadedStoreSize = 0;

    public int size() {
        return this.store.size();
    }

    public int sizeUnloaded() {
        return this.unloadedStoreSize;
    }

    @Nullable
    public T get(Location location) {
        if (this.store.isEmpty())
            return null;

        try {
            this.storeLock.readLock().lock();
            return this.store.get(location);
        } finally {
            this.storeLock.readLock().unlock();
        }
    }

    public void store(Location location, T object) {
        try {
            this.storeLock.writeLock().lock();
            this.store.put(location, object);
            storeChunkInternal(location, object);
        } finally {
            this.storeLock.writeLock().unlock();
        }
    }

    private void storeChunkInternal(Location location, T object) {
        String worldName = location.getWorld().getName();
        long packedPos = ChunkPosition.getPackedPos(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        try {
            this.storeByChunksLock.writeLock().lock();
            this.storeByChunks.computeIfAbsent(worldName, packedPos, LinkedHashSet::new)
                    .add(object);
        } finally {
            this.storeByChunksLock.writeLock().unlock();
        }
    }

    @Nullable
    public T remove(Location location) {
        if (this.store.isEmpty())
            return null;

        try {
            this.storeLock.writeLock().lock();
            T removed = this.store.remove(location);
            if (removed != null)
                removeChunkInternal(location, removed);
            return removed;
        } finally {
            this.storeLock.writeLock().unlock();
        }
    }

    private void removeChunkInternal(Location location, T object) {
        String worldName = location.getWorld().getName();
        long packedPos = ChunkPosition.getPackedPos(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        try {
            this.storeByChunksLock.writeLock().lock();
            Set<T> chunkObjects = this.storeByChunks.get(worldName, packedPos);
            if (chunkObjects != null && chunkObjects.remove(object) && chunkObjects.isEmpty()) {
                this.storeByChunks.remove(worldName, packedPos);
            }
        } finally {
            this.storeByChunksLock.writeLock().unlock();
        }
    }

    public void storeUnloaded(U unloaded) {
        String worldName = unloaded.getWorldName();
        long packedPos = ChunkPosition.getPackedPos(unloaded.getX() >> 4, unloaded.getZ() >> 4);
        this.unloadedStore.computeIfAbsent(worldName, packedPos, LinkedHashSet::new).add(unloaded);
        ++this.unloadedStoreSize;
    }


    public void loadUnloaded(String worldName, int chunkX, int chunkZ, Consumer<U> unloadedConsumer) {
        if (this.unloadedStore.isEmpty())
            return;

        long packedPos = ChunkPosition.getPackedPos(chunkX, chunkZ);

        Set<U> unloadedData;
        try {
            this.unloadedStoreLock.writeLock().lock();
            unloadedData = this.unloadedStore.remove(worldName, packedPos);
        } finally {
            this.unloadedStoreLock.writeLock().unlock();
        }

        if (unloadedData != null && !unloadedData.isEmpty()) {
            unloadedData.forEach(unloadedConsumer);
            this.unloadedStoreSize -= unloadedData.size();
        }
    }

    public void iterateUnloaded(Consumer<U> unloadedConsumer) {
        if (this.unloadedStore.isEmpty())
            return;

        try {
            this.unloadedStoreLock.readLock().lock();
            this.unloadedStore.values().forEach(chunkObjects -> chunkObjects.forEach(unloadedConsumer));
        } finally {
            this.unloadedStoreLock.readLock().unlock();
        }
    }

    public void removeUnloaded(U object) {
        if (this.unloadedStore.isEmpty())
            return;

        String worldName = object.getWorldName();
        long packedPos = ChunkPosition.getPackedPos(object.getX() >> 4, object.getZ() >> 4);

        try {
            this.unloadedStoreLock.writeLock().lock();
            Set<U> unloadedData = this.unloadedStore.get(worldName, packedPos);
            if (unloadedData != null && unloadedData.remove(object) && unloadedData.isEmpty())
                this.unloadedStore.remove(worldName, packedPos);
        } finally {
            this.unloadedStoreLock.writeLock().unlock();
        }

    }

    public List<T> getAll() {
        if (this.store.isEmpty())
            return Collections.emptyList();

        List<T> all = new LinkedList<>();
        collect(all);
        return all;
    }

    public List<T> getAllFromChunk(String worldName, int x, int z) {
        if (this.storeByChunks.isEmpty())
            return Collections.emptyList();

        List<T> all = new LinkedList<>();
        collectFromChunk(worldName, x, z, all);
        return all;
    }

    /**
     * Returns cached objects in the same world within the inclusive XYZ block bounds.
     */
    public List<T> collectInRange(Location location, int range) {
        if (this.store.isEmpty() || range < 0)
            return Collections.emptyList();

        String worldName = location.getWorld().getName();
        long minX = (long) location.getBlockX() - range, minY = (long) location.getBlockY() - range, minZ = (long) location.getBlockZ() - range;
        long maxX = (long) location.getBlockX() + range, maxY = (long) location.getBlockY() + range, maxZ = (long) location.getBlockZ() + range;

        int minChunkX = (int) (minX >> 4), maxChunkX = (int) (maxX >> 4);
        int minChunkZ = (int) (minZ >> 4), maxChunkZ = (int) (maxZ >> 4);
        long chunksInRange = ((long) maxChunkX - minChunkX + 1) * ((long) maxChunkZ - minChunkZ + 1);

        List<T> nearby = new ArrayList<>();
        // Use the chunk index while there are fewer chunk lookups (including empty chunks)
        // than cached objects to check. This avoids excessive lookups for large radii.
        if (chunksInRange < size()) {
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    collectFromChunk(worldName, chunkX, chunkZ, nearby);
                }
            }
        } else {
            collect(nearby);
        }

        nearby.removeIf(object -> {
            Location loc = object.getLocation();
            return !worldName.equals(loc.getWorld().getName()) ||
                    loc.getBlockX() < minX || loc.getBlockX() > maxX ||
                    loc.getBlockY() < minY || loc.getBlockY() > maxY ||
                    loc.getBlockZ() < minZ || loc.getBlockZ() > maxZ;
        });
        return nearby;
    }

    public void collect(List<? super T> list) {
        if (this.store.isEmpty())
            return;

        try {
            this.storeLock.readLock().lock();
            list.addAll(this.store.values());
        } finally {
            this.storeLock.readLock().unlock();
        }
    }

    public void collectFromChunk(String worldName, int x, int z, List<? super T> list) {
        if (this.storeByChunks.isEmpty())
            return;

        long packedPos = ChunkPosition.getPackedPos(x, z);

        try {
            this.storeByChunksLock.readLock().lock();
            Set<T> chunkObjects = this.storeByChunks.get(worldName, packedPos);
            if (chunkObjects != null)
                list.addAll(chunkObjects);
        } finally {
            this.storeByChunksLock.readLock().unlock();
        }
    }

}
