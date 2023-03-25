package com.bgsoftware.wildstacker.nms.v1_7_R4.world;

import net.minecraft.server.v1_7_R4.Chunk;
import net.minecraft.server.v1_7_R4.MathHelper;
import net.minecraft.server.v1_7_R4.World;

public final class BlockPosition {

    public final int x;
    public final int y;
    public final int z;

    public BlockPosition(double x, double y, double z) {
        this(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Chunk getChunk(World world) {
        return world.getChunkAtWorldCoords(MathHelper.floor(x), MathHelper.floor(z));
    }

}
