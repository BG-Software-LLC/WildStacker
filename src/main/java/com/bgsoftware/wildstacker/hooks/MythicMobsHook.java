package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public final class MythicMobsHook {

    public static LivingEntity tryDuplicate(LivingEntity livingEntity){
        if(WStackedEntity.of(livingEntity).getSpawnCause() == SpawnCause.MYTHIC_MOBS){
            ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);
            ActiveMob duplicate = MythicMobs.inst().getMobManager().spawnMob(activeMob.getType().getInternalName(), livingEntity.getLocation());
            return duplicate.getLivingEntity();
        }

        return null;
    }

    public static boolean isEnabled(){
        return Bukkit.getPluginManager().isPluginEnabled("MythicMobs");
    }

}
