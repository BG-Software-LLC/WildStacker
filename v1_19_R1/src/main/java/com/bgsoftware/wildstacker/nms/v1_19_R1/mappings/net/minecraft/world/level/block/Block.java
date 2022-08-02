package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.level.block;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;
import net.minecraft.world.level.block.state.IBlockData;

public class Block extends MappedObject<net.minecraft.world.level.block.Block> {

    public Block(net.minecraft.world.level.block.Block handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.block.Block",
            name = "defaultBlockState",
            type = Remap.Type.METHOD,
            remappedName = "m")
    public static IBlockData defaultBlockState(net.minecraft.world.level.block.Block block) {
        return block.m();
    }

}
