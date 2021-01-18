package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

/**
 * EntityUnstackEvent is called when an entity is unstacked.
 */
public class EntityUnstackEvent extends UnstackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     * @param entity The entity object.
     * @param unstackSource Get the source for the unstack.
     * @param unstackAmount The amount the entity is unstacked by.
     */
    public EntityUnstackEvent(StackedEntity entity, Entity unstackSource, int unstackAmount){
        super(entity, unstackSource, unstackAmount);
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

    /**
     * Set the amount the entity will be unstacked by.
     * @param unstackAmount The amount to set.
     */
    public void setAmount(int unstackAmount){
        this.unstackAmount = unstackAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
