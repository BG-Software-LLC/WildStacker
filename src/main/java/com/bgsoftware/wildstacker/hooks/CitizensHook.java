package com.bgsoftware.wildstacker.hooks;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Entity;

public final class CitizensHook {

    public static boolean isNPC(Entity entity){
        return PluginHooks.isCitizensEnabled && CitizensAPI.getNPCRegistry().isNPC(entity);
    }

}
