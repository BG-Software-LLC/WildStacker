package com.bgsoftware.wildstacker.hooks;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.entity.Entity;

public final class CitizensHook {


    public static boolean isNPC(Entity entity){
        if(!PluginHooks.isCitizensEnabled || !CitizensAPI.hasImplementation())
            return false;

        NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();

        return npcRegistry != null && npcRegistry.isNPC(entity);
    }

}
