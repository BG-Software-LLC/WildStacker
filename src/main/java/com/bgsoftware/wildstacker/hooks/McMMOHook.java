package com.bgsoftware.wildstacker.hooks;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public final class McMMOHook {

    private static final String CUSTOM_NAME_KEY = "mcMMO: Custom Name";
    private static final String CUSTOM_NAME_VISIBLE_KEY = "mcMMO: Name Visibility";
    private static final String SPAWNED_ENTITY_KEY = "mcMMO: Spawned Entity";

    private static Plugin mcMMO;

    public static void updateCachedName(LivingEntity livingEntity){
        if(mcMMO != null){
            if(livingEntity.hasMetadata(CUSTOM_NAME_KEY)) {
                livingEntity.removeMetadata(CUSTOM_NAME_KEY, mcMMO);
                livingEntity.setMetadata(CUSTOM_NAME_KEY, new FixedMetadataValue(mcMMO, livingEntity.getCustomName()));
            }
            if(livingEntity.hasMetadata(CUSTOM_NAME_VISIBLE_KEY)) {
                livingEntity.removeMetadata(CUSTOM_NAME_VISIBLE_KEY, mcMMO);
                livingEntity.setMetadata(CUSTOM_NAME_VISIBLE_KEY, new FixedMetadataValue(mcMMO, livingEntity.isCustomNameVisible()));
            }
        }
    }

    public static void updateSpawnedEntity(LivingEntity livingEntity){
        if(mcMMO != null){
            livingEntity.setMetadata(SPAWNED_ENTITY_KEY, new FixedMetadataValue(mcMMO, true));
        }
    }

    public static void handleCombat(Player attacker, LivingEntity target, double finalDamage, EntityDamageEvent.DamageCause damageCause){

    }

    public static boolean isSpawnedEntity(LivingEntity livingEntity){
        return mcMMO != null && livingEntity.hasMetadata(SPAWNED_ENTITY_KEY);
    }

    public static void setEnabled(boolean enabled){
        mcMMO = enabled ? com.gmail.nossr50.mcMMO.p : null;
    }

}
