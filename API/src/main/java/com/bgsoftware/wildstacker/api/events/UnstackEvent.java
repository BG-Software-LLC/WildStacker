package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * UnstackEvent is a base event for all the unstack events.
 */
public abstract class UnstackEvent extends Event implements Cancellable {

    private boolean cancelled;

    protected final StackedObject object;
    protected final Entity unstackSource;
    protected final int unstackAmount;

    /**
     * The constructor for the event.
     * @param object The original stacked object.
     * @param unstackSource Get the source for the unstack.
     * @param unstackAmount The amount the object is unstacked by.
     */
    public UnstackEvent(StackedObject object, Entity unstackSource, int unstackAmount){
        super(!Bukkit.isPrimaryThread());
        this.object = object;
        this.unstackSource = unstackSource;
        this.unstackAmount = unstackAmount;
        cancelled = false;
    }

    /**
     * Get the source for the unstack.
     * Can be null if was killed by an unknown source (fall damage, fire, etc)
     */
    public Entity getUnstackSource() {
        return unstackSource;
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
