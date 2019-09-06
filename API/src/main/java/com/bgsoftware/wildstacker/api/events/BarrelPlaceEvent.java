package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * BarrelPlaceEvent is called when a new barrel is placed in the world.
 */
public class BarrelPlaceEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     * @param player The player who placed the barrel.
     * @param barrel The barrel object of the placed block.
     */
    public BarrelPlaceEvent(Player player, StackedBarrel barrel){
        super(player, barrel);
    }

    /**
     * Get the barrel object of the event.
     */
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
