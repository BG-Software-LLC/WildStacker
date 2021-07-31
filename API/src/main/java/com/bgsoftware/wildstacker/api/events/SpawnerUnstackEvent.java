package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

/**
 * SpawnerUnstackEvent is called when a spawner is unstacked.
 */
public class SpawnerUnstackEvent extends UnstackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     *
     * @param spawner       The spawner object.
     * @param unstackSource Get the source for the unstack.
     * @param unstackAmount The amount the spawner is unstacked by.
     */
    public SpawnerUnstackEvent(StackedSpawner spawner, Entity unstackSource, int unstackAmount) {
        super(spawner, unstackSource, unstackAmount);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the spawner that is unstacked.
     */
    public StackedSpawner getSpawner() {
        return (StackedSpawner) object;
    }

    /**
     * Get the amount the spawner is unstacked by.
     */
    public int getAmount() {
        return unstackAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
