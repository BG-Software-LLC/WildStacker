package com.bgsoftware.wildstacker.hooks;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.entity.Entity;

public final class CitizensHook {

    public static boolean isNPC(Entity entity){
        if(PluginHooks.isCitizensEnabled) {
            for (NPCRegistry registry : CitizensAPI.getNPCRegistries())
                if (registry.isNPC(entity))
                    return true;
        }

        return false;
    }

}
