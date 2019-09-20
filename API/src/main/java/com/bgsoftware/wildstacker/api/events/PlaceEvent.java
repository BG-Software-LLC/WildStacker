package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * PlaceEvent is a base event for all the placement events.
 */
public abstract class PlaceEvent extends Event implements Cancellable {

    private boolean cancelled;

    protected final StackedObject object;
    protected final Player player;

    /**
     * The constructor for the event.
     * @param player The player who placed the object.
     * @param object The stacked object that was placed.
     */
    public PlaceEvent(Player player, StackedObject object){
        super(!Bukkit.isPrimaryThread());
        this.player = player;
        this.object = object;
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
