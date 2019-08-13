package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class SpawnerAmountChangeEvent extends AmountChangeEvent<StackedSpawner> {

    private static final HandlerList HANDLERS = new HandlerList();

    public SpawnerAmountChangeEvent(StackedSpawner stackedSpawner, int stackAmount){
        super(stackedSpawner, stackAmount);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
