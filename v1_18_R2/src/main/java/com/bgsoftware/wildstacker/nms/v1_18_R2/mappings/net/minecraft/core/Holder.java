package com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.core;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.MappedObject;
import net.minecraft.tags.TagKey;

public class Holder<T> extends MappedObject<net.minecraft.core.Holder.c<T>> {

    public Holder(net.minecraft.core.Holder.c<T> handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.core.Holder$Reference",
            name = "is",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public boolean is(TagKey<T> tag) {
        return handle.a(tag);
    }

}
