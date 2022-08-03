package com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.server.level;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.MappedObject;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.nbt.NBTTagCompound;

public class MobSpawnerData extends MappedObject<net.minecraft.world.level.MobSpawnerData> {

    public MobSpawnerData(net.minecraft.world.level.MobSpawnerData handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.SpawnData",
            name = "getEntityToSpawn",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public net.minecraft.nbt.NBTTagCompound getEntityToSpawnNoMapping() {
        return handle.a();
    }

    public NBTTagCompound getEntityToSpawn() {
        return new NBTTagCompound(getEntityToSpawnNoMapping());
    }

}