package com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.resources;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.MappedObject;

public class MinecraftKey extends MappedObject<net.minecraft.resources.MinecraftKey> {

    public MinecraftKey(String val) {
        super(new net.minecraft.resources.MinecraftKey(val));
    }
    public MinecraftKey(net.minecraft.resources.MinecraftKey handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.resources.ResourceLocation",
            name = "getPath",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public String getPath() {
        return handle.a();
    }

}
