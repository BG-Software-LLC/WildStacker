package com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.entity.ai;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.MappedObject;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BehaviorController<E extends EntityLiving> extends MappedObject<net.minecraft.world.entity.ai.BehaviorController<E>> {

    public BehaviorController(net.minecraft.world.entity.ai.BehaviorController<E> handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.ai.Brain",
            name = "eraseMemory",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void eraseMemory(MemoryModuleType<?> memoryModuleType) {
        handle.b(memoryModuleType);
    }

    @Remap(classPath = "net.minecraft.world.entity.ai.Brain",
            name = "hasMemoryValue",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public boolean hasMemoryValue(MemoryModuleType<?> memoryModuleType) {
        return handle.a(memoryModuleType);
    }

    @Remap(classPath = "net.minecraft.world.entity.ai.Brain",
            name = "setMemoryWithExpiry",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public <U> void setMemoryWithExpiry(MemoryModuleType<U> memoryModuleType, U value, long expiry) {
        handle.a(memoryModuleType, value, expiry);
    }

}
