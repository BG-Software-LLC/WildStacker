package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class DuplicateSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedEntity stackedEntity;
    private final StackedEntity duplicate;

    public DuplicateSpawnEvent(StackedEntity stackedEntity, StackedEntity duplicate){
        this.stackedEntity = stackedEntity;
        this.duplicate = duplicate;
    }

    public StackedEntity getEntity() {
        return stackedEntity;
    }

    public StackedEntity getDuplicate() {
        return duplicate;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
