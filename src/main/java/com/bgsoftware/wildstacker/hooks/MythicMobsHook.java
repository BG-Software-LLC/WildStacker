package com.bgsoftware.wildstacker.hooks;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public final class MythicMobsHook {

    public static boolean isMythicMob(LivingEntity livingEntity){
        if(Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            return MythicMobs.inst().getMobManager().isActiveMob(livingEntity.getUniqueId());
        }

        return false;
    }

}
