package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class PaperListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityAdd(EntityAddToWorldEvent e) {
        EntityStorage.cancelMetadataRemoval(e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRemove(EntityRemoveFromWorldEvent e) {
        EntitiesListener.IMP.handleEntityRemove(e.getEntity());
    }

}
