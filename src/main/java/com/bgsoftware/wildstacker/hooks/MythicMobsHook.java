package com.bgsoftware.wildstacker.hooks;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public final class MythicMobsHook {

    public static boolean isMythicMob(LivingEntity livingEntity){
        if(Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            return MythicMobs.inst().getMobManager().isActiveMob(livingEntity.getUniqueId());
        }

        return false;
    }

    public static LivingEntity tryDuplicate(LivingEntity livingEntity){
        if(isMythicMob(livingEntity)){
            ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);
            ActiveMob duplicate = MythicMobs.inst().getMobManager().spawnMob(activeMob.getType().getInternalName(), livingEntity.getLocation());
            return duplicate.getLivingEntity();
        }

        return null;
    }

}
