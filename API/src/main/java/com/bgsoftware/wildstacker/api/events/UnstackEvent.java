package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * UnstackEvent is a base event for all the unstack events.
 */
public abstract class UnstackEvent extends Event implements Cancellable {

    private boolean cancelled;

    protected final StackedObject object;
    protected final int unstackAmount;

    /**
     * The constructor for the event.
     * @param object The original stacked object.
     * @param unstackAmount The amount the object is unstacked by.
     */
    public UnstackEvent(StackedObject object, int unstackAmount){
        super(!Bukkit.isPrimaryThread());
        this.object = object;
        this.unstackAmount = unstackAmount;
        cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
