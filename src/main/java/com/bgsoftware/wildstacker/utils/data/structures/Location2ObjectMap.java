package com.bgsoftware.wildstacker.utils.data.structures;

import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Location2ObjectMap<V> {

    private static final boolean IS_TALL_WORLD = ServerVersion.isAtLeast(ServerVersion.v1_18);

    private final Chunk2ObjectMap<ChunkMap<V>> backendMap = new Chunk2ObjectMap<>();
    private int size = 0;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public int size() {
        return read(() -> this.size);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsKey(Location location) {
        return get(location) != null;
    }

    public boolean containsKey(ILocationEntity entity) {
        return get(entity) != null;
    }

    @Nullable
    public V get(Location location) {
        World world = location.getWorld();
        if (world == null)
            return null;

        String worldName = world.getName();

        return get(worldName, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Nullable
    public V get(ILocationEntity entity) {
        return get(entity.getWorldName(), entity.getX(), entity.getY(), entity.getZ());
    }

    @Nullable
    private V get(String worldName, int x, int y, int z) {
        if (worldName == null)
            return null;

        long chunkPair = computeChunkPair(x >> 4, z >> 4);

        return read(() -> {
            ChunkMap<V> chunkMap = this.backendMap.get(worldName, chunkPair);
            if (chunkMap == null)
                return null;

            return chunkMap.get(x & 0xF, y, z & 0xF);
        });
    }

    @Nullable
    public V put(Location location, V value) {
        World world = location.getWorld();
        Preconditions.checkArgument(world != null, "cannot insert location with null world");
        return put(world.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), value);
    }

    @Nullable
    public V put(ILocationEntity entity, V value) {
        return put(entity.getWorldName(), entity.getX(), entity.getY(), entity.getZ(), value);
    }

    @Nullable
    private V put(String worldName, int x, int y, int z, V value) {
        Preconditions.checkArgument(worldName != null, "cannot insert location with null world");

        long chunkPair = computeChunkPair(x >> 4, z >> 4);

        return write(() -> {
            ChunkMap<V> chunkMap = this.backendMap.computeIfAbsent(worldName, chunkPair, ChunkMap::new);

            V oldValue = chunkMap.put(x & 0xF, y, z & 0xF, value);

            if (oldValue == null)
                ++this.size;

            return oldValue;
        });
    }

    public V remove(Location location) {
        World world = location.getWorld();
        if (world == null)
            return null;

        return remove(world.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public V remove(ILocationEntity entity) {
        return remove(entity.getWorldName(), entity.getX(), entity.getY(), entity.getZ());
    }

    private V remove(String worldName, int x, int y, int z) {
        if (worldName == null)
            return null;

        long chunkPair = computeChunkPair(x >> 4, z >> 4);

        return write(() -> {
            ChunkMap<V> chunkMap = this.backendMap.get(worldName, chunkPair);
            if (chunkMap == null)
                return null;

            V oldValue = onRemoveInnerLock(chunkMap.remove(x & 0xF, y, z & 0xF));
            if (oldValue == null)
                return null;

            if (chunkMap.isEmpty()) {
                this.backendMap.remove(worldName, chunkPair);
            }

            return oldValue;
        });
    }

    public Collection<V> remove(ChunkPosition chunkPosition) {
        return write(() -> {
            ChunkMap<V> chunkMap = this.backendMap.remove(chunkPosition);
            if (chunkMap == null)
                return Collections.emptyList();

            return chunkMap.backendData.values();
        });
    }

    @Nullable
    private V onRemoveInnerLock(@Nullable V removedValue) {
        if (removedValue != null)
            --this.size;
        return removedValue;
    }

    public void clear() {
        write(() -> {
            this.backendMap.clear();
            this.size = 0;
        });
    }

    public void forEach(ChunkPosition chunkPosition, Consumer<V> consumer) {
        List<V> values = read(() -> {
            ChunkMap<V> chunkMap = this.backendMap.get(chunkPosition.getWorld(), chunkPosition.pack());
            if (chunkMap == null)
                return Collections.emptyList();

            return new LinkedList<>(chunkMap.backendData.values());
        });

        for (V v : values) {
            consumer.accept(v);
        }
    }

    public Collection<V> values() {
        return read(() -> {
            if (this.backendMap.isEmpty())
                return Collections.emptyList();

            List<V> values = new LinkedList<>();
            this.backendMap.values().forEach(chunkMap -> values.addAll(chunkMap.backendData.values()));
            return values;
        });
    }

    public void collect(Collection<? super V> collection) {
        read(() -> this.backendMap.values().forEach(
                chunkMap -> collection.addAll(chunkMap.backendData.values())));
    }

    public <T> void collect(Collection<? super T> collection, Function<V, T> mapper) {
        read(() -> this.backendMap.values().forEach(chunkMap -> chunkMap.backendData.values().forEach(
                value -> collection.add(mapper.apply(value)))));
    }

    private static long computeChunkPair(int chunkX, int chunkZ) {
        return ((chunkZ & 0xFFFFFFFFL) << 32) | (chunkX & 0xFFFFFFFFL);
    }

    private static int computeBlockIndex(int relativeX, int blockY, int relativeZ) {
        Preconditions.checkArgument(relativeX >= 0 && relativeX <= 0xF, "invalid relativeX: " + relativeX);
        Preconditions.checkArgument(relativeZ >= 0 && relativeZ <= 0xF, "invalid blockZ: " + relativeZ);

        int worldMinHeight = IS_TALL_WORLD ? 0 : -64;

        short relativeY = (short) (blockY - worldMinHeight);
        return (relativeY << 8) | (relativeX << 4) | relativeZ;
    }

    public interface ILocationEntity {

        String getWorldName();

        int getX();

        int getY();

        int getZ();

    }

    private static class ChunkMap<V> {

        private final Map<Integer, V> backendData = new LinkedHashMap<>();

        @Nullable
        public V get(int relativeX, int blockY, int relativeZ) {
            int blockIdx = computeBlockIndex(relativeX, blockY, relativeZ);
            return get(blockIdx);
        }

        @Nullable
        public V get(int blockIdx) {
            return this.backendData.get(blockIdx);
        }

        @Nullable
        public V put(int relativeX, int blockY, int relativeZ, V value) {
            int blockIdx = computeBlockIndex(relativeX, blockY, relativeZ);
            return put(blockIdx, value);
        }

        @Nullable
        public V put(int blockIdx, V value) {
            return this.backendData.put(blockIdx, value);
        }

        @Nullable
        public V remove(int relativeX, int blockY, int relativeZ) {
            int blockIdx = computeBlockIndex(relativeX, blockY, relativeZ);
            return this.remove(blockIdx);
        }

        @Nullable
        public V remove(int blockIdx) {
            return this.backendData.remove(blockIdx);
        }

        public boolean isEmpty() {
            return this.backendData.isEmpty();
        }

    }

    private <R> R read(Supplier<R> supplier) {
        try {
            this.lock.readLock().lock();
            return supplier.get();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private void read(Runnable runnable) {
        try {
            this.lock.readLock().lock();
            runnable.run();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private <R> R write(Supplier<R> supplier) {
        try {
            this.lock.writeLock().lock();
            return supplier.get();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private void write(Runnable runnable) {
        try {
            this.lock.writeLock().lock();
            runnable.run();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

}
