package com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.net.minecraft.world.entity;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.MappedObject;

public class SaddleStorage extends MappedObject<net.minecraft.world.entity.SaddleStorage> {

    public SaddleStorage(net.minecraft.world.entity.SaddleStorage handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.ItemBasedSteering",
            name = "setSaddle",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setSaddle(boolean hasStorage) {
        handle.a(hasStorage);
    }

}
