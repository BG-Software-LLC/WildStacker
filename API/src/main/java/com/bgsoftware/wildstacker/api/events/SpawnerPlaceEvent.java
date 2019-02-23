package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class SpawnerPlaceEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public SpawnerPlaceEvent(Player player, StackedSpawner spawner){
        super(player, spawner);
    }

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
