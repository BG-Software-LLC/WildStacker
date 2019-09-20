package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * SpawnerPlaceEvent is called when a new spawner is placed in the world.
 */
public class SpawnerPlaceEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     * @param player The player who placed the spawner.
     * @param spawner The spawner object of the placed block.
     */
    public SpawnerPlaceEvent(Player player, StackedSpawner spawner){
        super(player, spawner);
    }

    /**
     * Get the spawner object of the event.
     */
    public StackedSpawner getSpawner() {
        return (StackedSpawner) object;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
