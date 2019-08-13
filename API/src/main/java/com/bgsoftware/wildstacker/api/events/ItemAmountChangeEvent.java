package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedItem;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class ItemAmountChangeEvent extends AmountChangeEvent<StackedItem> {

    private static final HandlerList HANDLERS = new HandlerList();

    public ItemAmountChangeEvent(StackedItem stackedItem, int stackAmount){
        super(stackedItem, stackAmount);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
