package com.bgsoftware.wildstacker.nms.v1_7_R4;

import com.bgsoftware.wildstacker.nms.NMSWorldConfig;
import org.bukkit.World;

public class NMSWorldConfigImpl implements NMSWorldConfig {

    @Override
    public double getVillagerInfectionChance(World world) {
        return -1.0D;
    }

}
