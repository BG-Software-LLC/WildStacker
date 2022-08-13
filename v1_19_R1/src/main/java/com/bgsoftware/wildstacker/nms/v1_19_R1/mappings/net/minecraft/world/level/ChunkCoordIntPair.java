package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.level;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;
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
            remappedName = "e")
    public int getX() {
        return handle.e;
    }

    @Remap(classPath = "net.minecraft.world.level.ChunkPos",
            name = "z",
            type = Remap.Type.FIELD,
            remappedName = "f")
    public int getZ() {
        return handle.f;
    }

}
