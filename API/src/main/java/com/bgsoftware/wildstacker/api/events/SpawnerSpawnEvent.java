package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class SpawnerSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedEntity stackedEntity;
    private final StackedSpawner stackedSpawner;

    public SpawnerSpawnEvent(StackedEntity stackedEntity, StackedSpawner stackedSpawner){
        this.stackedEntity = stackedEntity;
        this.stackedSpawner = stackedSpawner;
    }

    public StackedEntity getEntity() {
        return stackedEntity;
    }

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
