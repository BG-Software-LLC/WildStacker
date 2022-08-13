package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.util;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;

public class RandomSource extends MappedObject<net.minecraft.util.RandomSource> {

    public RandomSource(net.minecraft.util.RandomSource handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.util.RandomSource",
            name = "nextDouble",
            type = Remap.Type.METHOD,
            remappedName = "j")
    public double nextDouble() {
        return handle.j();
    }

    @Remap(classPath = "net.minecraft.util.RandomSource",
            name = "nextInt",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public int nextInt(int bound) {
        return handle.a(bound);
    }

    @Remap(classPath = "net.minecraft.util.RandomSource",
            name = "nextFloat",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public float nextFloat() {
        return handle.i();
    }

}
