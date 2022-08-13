package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.level.block.entity;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.core.BlockPosition;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.server.level.MobSpawnerAbstract;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.level.World;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.INamableTileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;

public class TileEntity extends MappedObject<net.minecraft.world.level.block.entity.TileEntity> {

    public TileEntity(net.minecraft.world.level.block.entity.TileEntity handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.BlockEntity",
            name = "getLevel",
            type = Remap.Type.METHOD,
            remappedName = "k")
    public World getWorld() {
        return new World(handle.k());
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.BlockEntity",
            name = "getBlockPos",
            type = Remap.Type.METHOD,
            remappedName = "p")
    public BlockPosition getBlockPos() {
        return new BlockPosition(handle.p());
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.SpawnerBlockEntity",
            name = "getSpawner",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public MobSpawnerAbstract getSpawner() {
        return new MobSpawnerAbstract(((TileEntityMobSpawner) handle).d());
    }

    @Remap(classPath = "net.minecraft.world.Nameable",
            name = "hasCustomName",
            type = Remap.Type.METHOD,
            remappedName = "Y")
    public boolean hasCustomName() {
        return ((INamableTileEntity) handle).Y();
    }

    @Remap(classPath = "net.minecraft.world.Nameable",
            name = "getCustomName",
            type = Remap.Type.METHOD,
            remappedName = "Z")
    public IChatBaseComponent getCustomName() {
        return ((INamableTileEntity) handle).Z();
    }

}
