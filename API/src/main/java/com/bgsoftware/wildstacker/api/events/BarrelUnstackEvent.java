package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.event.HandlerList;

/**
 * BarrelUnstackEvent is called when a barrel is unstacked.
 */
public class BarrelUnstackEvent extends UnstackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     * @param barrel The barrel object.
     * @param unstackAmount The amount the barrel is unstacked by.
     */
    public BarrelUnstackEvent(StackedBarrel barrel, int unstackAmount){
        super(barrel, unstackAmount);
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
    public int getAmount(){
        return unstackAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
