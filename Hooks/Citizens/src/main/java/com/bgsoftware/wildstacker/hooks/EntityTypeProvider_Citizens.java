package com.bgsoftware.wildstacker.hooks;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.entity.Entity;

public final class EntityTypeProvider_Citizens implements EntityTypeProvider {

    @Override
    public String checkStackEntity(Entity entity) {
        if(!CitizensAPI.hasImplementation())
            return null;

        NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
        return npcRegistry == null || !npcRegistry.isNPC(entity) ? null :
                "Cannot get a stacked entity from an NPC of Citizens.";
    }

}
