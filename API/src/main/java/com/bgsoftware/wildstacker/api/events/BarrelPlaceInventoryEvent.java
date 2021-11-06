package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * BarrelPlaceInventoryEvent is called when blocks are moved into the place inventory of a barrel.
 */
public class BarrelPlaceInventoryEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int increaseAmount;

    /**
     * The constructor for the event.
     *
     * @param player         The player who placed the barrel.
     * @param barrel         The barrel object.
     * @param increaseAmount The amount that the barrel is increased by.
     */
    public BarrelPlaceInventoryEvent(Player player, StackedBarrel barrel, int increaseAmount) {
        super(player, barrel);
        this.increaseAmount = increaseAmount;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the barrel object of the event.
     */
    public StackedBarrel getBarrel() {
        return (StackedBarrel) object;
    }

    /**
     * Get the amount that the barrel is increased by.
     */
    public int getIncreaseAmount() {
        return increaseAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
