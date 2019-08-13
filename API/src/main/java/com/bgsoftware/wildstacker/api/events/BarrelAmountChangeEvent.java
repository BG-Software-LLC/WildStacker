package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class BarrelAmountChangeEvent extends AmountChangeEvent<StackedBarrel> {

    private static final HandlerList HANDLERS = new HandlerList();

    public BarrelAmountChangeEvent(StackedBarrel stackedBarrel, int stackAmount){
        super(stackedBarrel, stackAmount);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
