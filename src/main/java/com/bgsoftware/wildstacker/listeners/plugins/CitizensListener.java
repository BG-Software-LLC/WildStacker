package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class CitizensListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCCrate(NPCSpawnEvent e){
        Entity entity = e.getNPC().getEntity();

        if(!EntityUtils.isStackable(entity))
            return;

        WStackedEntity.of(entity).setSpawnCause(SpawnCause.CITIZENS);
    }

}
