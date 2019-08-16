package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class BarrelPlaceInventoryEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int increaseAmount;

    public BarrelPlaceInventoryEvent(Player player, StackedBarrel barrel, int increaseAmount){
        super(player, barrel);
        this.increaseAmount = increaseAmount;
    }

    public StackedBarrel getBarrel() {
        return (StackedBarrel) object;
    }

    public int getIncreaseAmount() {
        return increaseAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
