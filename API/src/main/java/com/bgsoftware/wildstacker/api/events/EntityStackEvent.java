package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.event.HandlerList;

/**
 * EntityStackEvent is called when an entity is stacked into another entity.
 */
public class EntityStackEvent extends StackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     *
     * @param entity The original entity object.
     * @param target The entity object that is stacked.
     */
    public EntityStackEvent(StackedEntity entity, StackedEntity target) {
        super(entity, target);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the original entity.
     */
    public StackedEntity getEntity() {
        return (StackedEntity) object;
    }

    /**
     * Get the entity that is stacked.
     */
    public StackedEntity getTarget() {
        return (StackedEntity) target;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
