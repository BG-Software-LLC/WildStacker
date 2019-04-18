package com.bgsoftware.wildstacker.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.boss.api.BossAPI;
import org.mineacademy.boss.api.BossDrop;
import org.mineacademy.boss.lib.fo.api.AutoUpdateMap;

import java.util.List;
import java.util.stream.Collectors;

public final class BossHook {

    public static List<ItemStack> getDrops(LivingEntity livingEntity){
        AutoUpdateMap<Integer, BossDrop> dropsMap = BossAPI.getBoss(livingEntity).getDrops();
        return dropsMap.values().stream().map(BossDrop::getItem).collect(Collectors.toList());
    }

    public static boolean isBoss(LivingEntity livingEntity){
        return Bukkit.getPluginManager().isPluginEnabled("Boss") && BossAPI.isBoss(livingEntity);
    }

}
