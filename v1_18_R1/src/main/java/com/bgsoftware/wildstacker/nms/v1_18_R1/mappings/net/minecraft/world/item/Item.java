package com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.net.minecraft.world.item;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.MappedObject;

public class Item extends MappedObject<net.minecraft.world.item.Item> {

    public Item(net.minecraft.world.item.Item handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.item.Item",
            name = "canBeDepleted",
            type = Remap.Type.METHOD,
            remappedName = "n")
    public boolean canBeDepleted() {
        return handle.n();
    }

}
