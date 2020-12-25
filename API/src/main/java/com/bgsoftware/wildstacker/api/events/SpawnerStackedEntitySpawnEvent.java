package com.bgsoftware.wildstacker.api.events;

import org.bukkit.Bukkit;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * SpawnerStackedEntitySpawnEvent is called when a spawner attempts to spawn a stacked entity.
 */
public class SpawnerStackedEntitySpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CreatureSpawner creatureSpawner;
    private boolean shouldBeStacked = true;

    /**
     * The constructor for the event.
     * @param creatureSpawner The spawner that will spawn the entity.
     */
    public SpawnerStackedEntitySpawnEvent(CreatureSpawner creatureSpawner){
        super(!Bukkit.isPrimaryThread());
        this.creatureSpawner = creatureSpawner;
    }

    /**
     * Get the spawner that will spawn the entity.
     */
    public CreatureSpawner getSpawner() {
        return creatureSpawner;
    }

    /**
     * Check whether or not the entity should be spawned as a stacked entity.
     */
    public boolean shouldBeStacked(){
        return shouldBeStacked;
    }

    /**
     * Set whether or not the entity should be spawned as a stacked entity.
     */
    public void setShouldBeStacked(boolean shouldBeStacked){
        this.shouldBeStacked = shouldBeStacked;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
