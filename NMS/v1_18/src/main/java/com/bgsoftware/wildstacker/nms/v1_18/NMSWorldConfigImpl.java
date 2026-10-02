package com.bgsoftware.wildstacker.nms.v1_18;

import net.minecraft.server.level.ServerLevel;

public class NMSWorldConfigImpl extends com.bgsoftware.wildstacker.nms.v1_18.AbstractNMSWorldConfig {

    @Override
    protected double getVillagerInfectionChance(ServerLevel serverLevel) {
        return serverLevel.paperConfig.zombieVillagerInfectionChance;
    }

}
