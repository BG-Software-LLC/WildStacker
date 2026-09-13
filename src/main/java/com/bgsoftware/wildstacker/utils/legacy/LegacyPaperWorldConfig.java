package com.bgsoftware.wildstacker.utils.legacy;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import org.bukkit.World;

public final class LegacyPaperWorldConfig {

    private static final ReflectMethod<Object> WORLD_GET_HANDLE = new ReflectMethod<>(
            new ClassInfo("CraftWorld", ClassInfo.PackageType.CRAFTBUKKIT).findClass(), "getHandle");
    private static final ReflectField<Object> WORLD_PAPER_CONFIG = new ReflectField<>(
            new ClassInfo("World", ClassInfo.PackageType.NMS), null, "paperConfig");
    private static final ReflectField<Double> VILLAGER_INFECTION_CHANCE = new ReflectField<>(
            new ClassInfo("com.destroystokyo.paper.PaperWorldConfig", ClassInfo.PackageType.UNKNOWN),
            double.class, "zombieVillagerInfectionChance");

    private LegacyPaperWorldConfig() {
    }

    public static double getVillagerInfectionChance(World world) {
        if (!VILLAGER_INFECTION_CHANCE.isValid())
            return -1.0D;

        Object worldHandle = WORLD_GET_HANDLE.invoke(world);
        if (worldHandle == null)
            return -1.0D;

        Object paperConfig = WORLD_PAPER_CONFIG.get(worldHandle);
        return paperConfig == null ? -1.0D : VILLAGER_INFECTION_CHANCE.get(paperConfig, -1.0D);
    }

}
