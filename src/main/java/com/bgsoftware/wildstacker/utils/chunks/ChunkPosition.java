package com.bgsoftware.wildstacker.utils.chunks;

import com.bgsoftware.wildstacker.api.objects.UnloadedStackedObject;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Objects;

public final class ChunkPosition {

    private final String world;
    private final int x, z;

    private long pairedXZ = -1;

    public ChunkPosition(Location location) {
        this(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public ChunkPosition(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public ChunkPosition(UnloadedStackedObject unloadedStackedObject) {
        this(unloadedStackedObject.getWorldName(), unloadedStackedObject.getX() >> 4, unloadedStackedObject.getZ() >> 4);
    }

    public ChunkPosition(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public long asPair() {
        if (this.pairedXZ < 0)
            pairedXZ = ((this.z & 0xFFFFFFFFL) << 32) | (this.x & 0xFFFFFFFFL);

        return pairedXZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x &&
                z == that.z &&
                world.equals(that.world);
    }
}
