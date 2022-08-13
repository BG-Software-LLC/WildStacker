package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.server.level;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ChunkProviderServer extends MappedObject<net.minecraft.server.level.ChunkProviderServer> {

    public ChunkProviderServer(net.minecraft.server.level.ChunkProviderServer handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerChunkCache",
            name = "broadcast",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void broadcast(Entity entity, Packet<?> packet) {
        handle.b(entity, packet);
    }

}
