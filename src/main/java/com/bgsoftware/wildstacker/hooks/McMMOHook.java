package com.bgsoftware.wildstacker.hooks;

import com.gmail.nossr50.mcMMO;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

public final class McMMOHook {

    private static final String CUSTOM_NAME_KEY = "mcMMO: Custom Name";
    private static final String CUSTOM_NAME_VISIBLE_KEY = "mcMMO: Name Visibility";

    private static boolean enabled = false;

    public static void updateCachedName(LivingEntity livingEntity){
        if(enabled){
            mcMMO mcMMO = (mcMMO) Bukkit.getPluginManager().getPlugin("mcMMO");
            if(livingEntity.hasMetadata(CUSTOM_NAME_KEY))
                livingEntity.setMetadata(CUSTOM_NAME_KEY, new FixedMetadataValue(mcMMO, livingEntity.getCustomName()));
            if(livingEntity.hasMetadata(CUSTOM_NAME_VISIBLE_KEY))
                livingEntity.setMetadata(CUSTOM_NAME_VISIBLE_KEY, new FixedMetadataValue(mcMMO, livingEntity.isCustomNameVisible()));
        }
    }

    public static boolean isEnabled(){
        return enabled;
    }

    public static void setEnabled(){
        enabled = true;
    }

}
