package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public final class SuperiorSkyblockHook {

    public static double getDropsMultiplier(LivingEntity livingEntity){
        if(Bukkit.getPluginManager().isPluginEnabled("SuperiorSkyblock2")){
            Island island = SuperiorSkyblockAPI.getIslandAt(livingEntity.getLocation());
            if(island != null){
                return island.getMobDropsMultiplier();
            }
        }
        return 1.0;
    }

}
