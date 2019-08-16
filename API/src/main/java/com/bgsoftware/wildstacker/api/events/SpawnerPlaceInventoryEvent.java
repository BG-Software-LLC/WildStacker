package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class SpawnerPlaceInventoryEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int increaseAmount;

    public SpawnerPlaceInventoryEvent(Player player, StackedSpawner spawner, int increaseAmount){
        super(player, spawner);
        this.increaseAmount = increaseAmount;
    }

    public StackedSpawner getSpawner() {
        return (StackedSpawner) object;
    }

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
