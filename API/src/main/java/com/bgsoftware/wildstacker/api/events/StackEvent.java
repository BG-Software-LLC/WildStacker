package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * StackEvent is a base event for all the stack events.
 */
public abstract class StackEvent extends Event implements Cancellable {

    private boolean cancelled;

    protected final StackedObject object, target;

    /**
     * The constructor for the event.
     * @param object The original stacked object.
     * @param target The stacked object that is stacked.
     */
    public StackEvent(StackedObject object, StackedObject target){
        super(!Bukkit.isPrimaryThread());
        this.object = object;
        this.target = target;
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
