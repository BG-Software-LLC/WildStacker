package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class EntityAmountChangeEvent extends AmountChangeEvent<StackedEntity> {

    private static final HandlerList HANDLERS = new HandlerList();

    public EntityAmountChangeEvent(StackedEntity stackedEntity, int stackAmount){
        super(stackedEntity, stackAmount);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
