package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class EntityStackEvent extends StackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public EntityStackEvent(StackedEntity entity, StackedEntity target){
        super(entity, target);
    }

    public StackedEntity getEntity() {
        return (StackedEntity) object;
    }

    public StackedEntity getTarget() {
        return (StackedEntity) target;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
