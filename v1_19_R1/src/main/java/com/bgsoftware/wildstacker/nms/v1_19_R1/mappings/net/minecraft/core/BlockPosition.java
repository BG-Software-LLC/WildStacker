package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.core;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;
import net.minecraft.core.EnumDirection;

public class BlockPosition extends MappedObject<net.minecraft.core.BlockPosition> {

    public BlockPosition(int x, int y, int z) {
        super(new net.minecraft.core.BlockPosition(x, y, z));
    }

    public BlockPosition(double x, double y, double z) {
        super(new net.minecraft.core.BlockPosition(x, y, z));
    }

    public BlockPosition(net.minecraft.core.BlockPosition handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.core.Vec3i",
            name = "getX",
            type = Remap.Type.METHOD,
            remappedName = "u")
    public int getX() {
        return handle.u();
    }

    @Remap(classPath = "net.minecraft.core.Vec3i",
            name = "getY",
            type = Remap.Type.METHOD,
            remappedName = "v")
    public int getY() {
        return handle.v();
    }

    @Remap(classPath = "net.minecraft.core.Vec3i",
            name = "getZ",
            type = Remap.Type.METHOD,
            remappedName = "w")
    public int getZ() {
        return handle.w();
    }

    @Remap(classPath = "net.minecraft.core.BlockPos",
            name = "below",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public net.minecraft.core.BlockPosition below() {
        return handle.c();
    }

    @Remap(classPath = "net.minecraft.core.BlockPos",
            name = "above",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public net.minecraft.core.BlockPosition above() {
        return handle.b();
    }

    @Remap(classPath = "net.minecraft.core.BlockPos",
            name = "mutable",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public BlockPosition mutable() {
        return new BlockPosition(handle.i());
    }

    @Remap(classPath = "net.minecraft.core.BlockPos$MutableBlockPos",
            name = "move",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public void move(EnumDirection enumDirection) {
        ((net.minecraft.core.BlockPosition.MutableBlockPosition) handle).c(enumDirection);
    }

}
