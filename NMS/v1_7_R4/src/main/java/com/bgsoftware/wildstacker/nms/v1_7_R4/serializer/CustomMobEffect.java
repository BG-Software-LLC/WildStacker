package com.bgsoftware.wildstacker.nms.v1_7_R4.serializer;

import net.minecraft.server.v1_7_R4.MobEffect;

public final class CustomMobEffect extends MobEffect {

    private final int customId;

    public CustomMobEffect(MobEffectCustomData mobEffectCustomData, int value) {
        super(mobEffectCustomData.getVanillaEffect().id, Integer.MAX_VALUE, value, false);
        customId = mobEffectCustomData.id;
    }

    public int getCustomId() {
        return customId;
    }
}
