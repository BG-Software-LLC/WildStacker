package com.bgsoftware.wildstacker.nms.v1_19;

import net.minecraft.server.level.ServerLevel;

public class NMSWorldConfigImpl extends com.bgsoftware.wildstacker.nms.v1_19.AbstractNMSWorldConfig {

    @Override
    protected double getVillagerInfectionChance(ServerLevel serverLevel) {
        return serverLevel.paperConfig().entities.behavior.zombieVillagerInfectionChance;
    }

}
