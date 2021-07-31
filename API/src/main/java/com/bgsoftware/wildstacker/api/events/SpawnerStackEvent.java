package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.event.HandlerList;

/**
 * SpawnerStackEvent is called when a spawner is stacked into another spawner.
 */
public class SpawnerStackEvent extends StackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     *
     * @param spawner The original spawner object.
     * @param target  The spawner object that is stacked.
     */
    public SpawnerStackEvent(StackedSpawner spawner, StackedSpawner target) {
        super(spawner, target);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the original spawner.
     */
    public StackedSpawner getSpawner() {
        return (StackedSpawner) object;
    }

    /**
     * Get the spawner that is stacked.
     */
    public StackedSpawner getTarget() {
        return (StackedSpawner) target;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
