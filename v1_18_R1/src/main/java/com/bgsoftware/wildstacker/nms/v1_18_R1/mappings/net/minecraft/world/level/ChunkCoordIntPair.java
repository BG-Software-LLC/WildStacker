package com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.net.minecraft.world.level;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.MappedObject;
import net.minecraft.core.BlockPosition;

public class ChunkCoordIntPair extends MappedObject<net.minecraft.world.level.ChunkCoordIntPair> {

    public ChunkCoordIntPair(BlockPosition position) {
        this(new net.minecraft.world.level.ChunkCoordIntPair(position));
    }

    public ChunkCoordIntPair(net.minecraft.world.level.ChunkCoordIntPair handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.ChunkPos",
            name = "x",
            type = Remap.Type.FIELD,
            remappedName = "c")
    public int getX() {
        return handle.c;
    }

    @Remap(classPath = "net.minecraft.world.level.ChunkPos",
            name = "z",
            type = Remap.Type.FIELD,
            remappedName = "d")
    public int getZ() {
        return handle.d;
    }

}
