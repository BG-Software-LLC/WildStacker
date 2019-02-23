package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class BarrelPlaceEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public BarrelPlaceEvent(Player player, StackedBarrel barrel){
        super(player, barrel);
    }

    public StackedBarrel getBarrel() {
        return (StackedBarrel) object;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
