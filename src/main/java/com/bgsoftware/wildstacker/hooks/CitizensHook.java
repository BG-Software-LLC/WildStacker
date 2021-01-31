package com.bgsoftware.wildstacker.hooks;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public final class CitizensHook {


    public static boolean isNPC(Entity entity){
        if(!PluginHooks.isCitizensEnabled)
            return false;

        if(!CitizensAPI.hasImplementation())
            CitizensAPI.setImplementation(JavaPlugin.getPlugin(Citizens.class));

        return CitizensAPI.getNPCRegistry().isNPC(entity);
    }

}
