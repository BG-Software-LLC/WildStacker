package com.bgsoftware.wildstacker.nms.v1_7_R4.serializer;

import net.minecraft.server.v1_7_R4.MobEffectList;

public final class MobEffectCustomData extends MobEffectList {

    private MobEffectList vanillaEffect;

    public MobEffectCustomData(int id) {
        super(id, false, 16262179);
    }

    public static MobEffectCustomData newEffect(int id) {
        try {
            new MobEffectCustomData(id);
        } catch (Exception ignored) {
        }
        return (MobEffectCustomData) MobEffectList.byId[id];
    }

    public MobEffectCustomData withVanillaEffect(MobEffectList vanillaEffect) {
        this.vanillaEffect = vanillaEffect;
        return this;
    }

    public MobEffectList getVanillaEffect() {
        return vanillaEffect;
    }

    @Override
    public MobEffectCustomData b(String s) {
        return (MobEffectCustomData) super.b(s);
    }

}
