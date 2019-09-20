package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * SpawnerPlaceInventoryEvent is called when blocks are moved into the place inventory of a spawner.
 */
public class SpawnerPlaceInventoryEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int increaseAmount;

    /**
     * The constructor for the event.
     * @param player The player who placed the spawner.
     * @param spawner The spawner object.
     * @param increaseAmount The amount that the spawner is increased by.
     */
    public SpawnerPlaceInventoryEvent(Player player, StackedSpawner spawner, int increaseAmount){
        super(player, spawner);
        this.increaseAmount = increaseAmount;
    }

    /**
     * Get the spawner object of the event.
     */
    public StackedSpawner getSpawner() {
        return (StackedSpawner) object;
    }

    /**
     * Get the amount that the spawner is increased by.
     */
    public int getIncreaseAmount() {
        return increaseAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
