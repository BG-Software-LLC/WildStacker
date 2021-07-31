package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.event.HandlerList;

/**
 * BarrelStackEvent is called when a barrel is stacked into another barrel.
 */
public class BarrelStackEvent extends StackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     *
     * @param barrel The original barrel object.
     * @param target The barrel object that is stacked.
     */
    public BarrelStackEvent(StackedBarrel barrel, StackedBarrel target) {
        super(barrel, target);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the original barrel.
     */
    public StackedBarrel getBarrel() {
        return (StackedBarrel) object;
    }

    /**
     * Get the barrel that is stacked.
     */
    public StackedBarrel getTarget() {
        return (StackedBarrel) target;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
