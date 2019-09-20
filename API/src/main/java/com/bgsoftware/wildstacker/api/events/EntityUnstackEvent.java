package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.event.HandlerList;

/**
 * EntityUnstackEvent is called when an entity is unstacked.
 */
public class EntityUnstackEvent extends UnstackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     * @param entity The entity object.
     * @param unstackAmount The amount the entity is unstacked by.
     */
    public EntityUnstackEvent(StackedEntity entity, int unstackAmount){
        super(entity, unstackAmount);
    }

    /**
     * Get the entity that is unstacked.
     */
    public StackedEntity getEntity() {
        return (StackedEntity) object;
    }

    /**
     * Get the amount the entity is unstacked by.
     */
    public int getAmount(){
        return unstackAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
