package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

/**
 * BarrelUnstackEvent is called when a barrel is unstacked.
 */
public class BarrelUnstackEvent extends UnstackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     *
     * @param barrel        The barrel object.
     * @param unstackSource Get the source for the unstack.
     * @param unstackAmount The amount the barrel is unstacked by.
     */
    public BarrelUnstackEvent(StackedBarrel barrel, Entity unstackSource, int unstackAmount) {
        super(barrel, unstackSource, unstackAmount);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the barrel that is unstacked.
     */
    public StackedBarrel getBarrel() {
        return (StackedBarrel) object;
    }

    /**
     * Get the amount the barrel is unstacked by.
     */
    public int getAmount() {
        return unstackAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
