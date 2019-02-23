package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class EntityUnstackEvent extends UnstackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public EntityUnstackEvent(StackedEntity entity, int unstackAmount){
        super(entity, unstackAmount);
    }

    public StackedEntity getEntity() {
        return (StackedEntity) object;
    }

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
