package com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.item;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.MappedObject;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.core.Holder;

public class Item extends MappedObject<net.minecraft.world.item.Item> {

    public Item(net.minecraft.world.item.Item handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.item.Item",
            name = "canBeDepleted",
            type = Remap.Type.METHOD,
            remappedName = "o")
    public boolean canBeDepleted() {
        return handle.o();
    }

    @Remap(classPath = "net.minecraft.world.item.Item",
            name = "builtInRegistryHolder",
            type = Remap.Type.METHOD,
            remappedName = "k")
    public Holder<net.minecraft.world.item.Item> builtInRegistryHolder() {
        return new Holder<>(handle.k());
    }

}
