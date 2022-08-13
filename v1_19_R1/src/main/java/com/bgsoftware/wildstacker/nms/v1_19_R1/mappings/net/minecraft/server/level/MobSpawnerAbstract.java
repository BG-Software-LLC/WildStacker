package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.server.level;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;
import net.minecraft.world.entity.EntityTypes;

public class MobSpawnerAbstract extends MappedObject<net.minecraft.world.level.MobSpawnerAbstract> {

    public MobSpawnerAbstract(net.minecraft.world.level.MobSpawnerAbstract handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "setEntityId",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setEntityId(EntityTypes<?> entityTypes) {
        handle.a(entityTypes);
    }

}
