package com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.net.minecraft.tags;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.MappedObject;

public class Tag<T> extends MappedObject<net.minecraft.tags.Tag<T>> {

    public Tag(net.minecraft.tags.Tag<T> handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.tags.Tag",
            name = "contains",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public boolean contains(T value) {
        return handle.a(value);
    }

}
