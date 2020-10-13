package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public final class MythicMobsHook {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static LivingEntity tryDuplicate(LivingEntity livingEntity){
        if(WStackedEntity.of(livingEntity).getSpawnCause() == SpawnCause.MYTHIC_MOBS){
            ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);
            ActiveMob duplicate = MythicMobs.inst().getMobManager().spawnMob(activeMob.getType().getInternalName(), livingEntity.getLocation());
            return (LivingEntity) duplicate.getEntity().getBukkitEntity();
        }

        return null;
    }

    public static boolean isMythicMob(Entity entity){
        if(PluginHooks.isMythicMobsEnabled)
            return MythicMobs.inst().getMobManager().isActiveMob(entity.getUniqueId());
        return false;
    }

    public static boolean areSimilar(UUID en1, UUID en2){
        if(PluginHooks.isMythicMobsEnabled){
            ActiveMob activeMob1 = MythicMobs.inst().getMobManager().getActiveMob(en1).orElse(null);
            ActiveMob activeMob2 = MythicMobs.inst().getMobManager().getActiveMob(en2).orElse(null);

            if((activeMob1 == null) != (activeMob2 == null))
                return false;

            return activeMob1 == null || activeMob1.getType().getInternalName().equals(activeMob2.getType().getInternalName());
        }

        return true;
    }

    public static String getMythicName(LivingEntity livingEntity){
        if(PluginHooks.isMythicMobsEnabled){
            ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);
            try {
                return activeMob.getDisplayName();
            }catch(Throwable ignored){}
        }

        return plugin.getNMSAdapter().getCustomName(livingEntity);
    }

}
