package com.bgsoftware.wildstacker.hooks;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

    public static List<ItemStack> getDrops(LivingEntity livingEntity, int stackAmount){
        List<ItemStack> drops = new ArrayList<>();

//        if (isMythicMob(livingEntity)) {
//            for(int i = 0; i < stackAmount; i++) {
//                ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);
//                Optional<DropTable> dropTable = MythicMobs.inst().getDropManager().getDropTable(activeMob.getType().getInternalName());
//                dropTable.ifPresent(dropTable1 -> {
//                    LootBag lootBag = dropTable1.generate(new DropMetadata(activeMob, activeMob.getEntity())).equip(activeMob.getEntity());
//                });
//            }
//        }

        return drops;
    }

}
