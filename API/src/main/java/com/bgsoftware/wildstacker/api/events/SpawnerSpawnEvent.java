package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * SpawnerSpawnEvent is called when an entity is spawned by a spawner.
 * This is a replacement for Bukkit's event.
 */
public class SpawnerSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedEntity stackedEntity;
    private final StackedSpawner stackedSpawner;

    /**
     * The constructor for the event.
     * @param stackedEntity The entity object that was spawned.
     * @param stackedSpawner The spawner object that spawned the entity.
     */
    public SpawnerSpawnEvent(StackedEntity stackedEntity, StackedSpawner stackedSpawner){
        this.stackedEntity = stackedEntity;
        this.stackedSpawner = stackedSpawner;
    }

    /**
     * Get the entity object that was spawned.
     */
    public StackedEntity getEntity() {
        return stackedEntity;
    }

    /**
     * Get the spawner object that spawned the entity.
     */
    public StackedSpawner getSpawner() {
        return stackedSpawner;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
