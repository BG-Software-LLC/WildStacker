package com.bgsoftware.wildstacker.nms.v1_16_R3;

import com.bgsoftware.wildstacker.nms.NMSWorldConfig;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

public class NMSWorldConfigImpl implements NMSWorldConfig {

    @Override
    public double getVillagerInfectionChance(World world) {
        try {
            return ((CraftWorld) world).getHandle().paperConfig.zombieVillagerInfectionChance;
        } catch (LinkageError ignored) {
            return -1.0D;
        }
    }

}
