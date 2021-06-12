package com.bgsoftware.wildstacker.hooks;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Entity;

public final class CitizensHook {


    public static boolean isNPC(Entity entity){
        if(!PluginHooks.isCitizensEnabled || !CitizensAPI.hasImplementation())
            return false;

        return CitizensAPI.getNPCRegistry().isNPC(entity);
    }

}
