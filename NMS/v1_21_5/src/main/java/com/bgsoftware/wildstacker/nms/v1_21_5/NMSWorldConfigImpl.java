package com.bgsoftware.wildstacker.nms.v1_21_5;

import net.minecraft.server.level.ServerLevel;

public class NMSWorldConfigImpl extends com.bgsoftware.wildstacker.nms.v1_21_5.AbstractNMSWorldConfig {

    @Override
    protected double getVillagerInfectionChance(ServerLevel serverLevel) {
        return serverLevel.paperConfig().entities.behavior.zombieVillagerInfectionChance.or(-1.0D);
    }

}
