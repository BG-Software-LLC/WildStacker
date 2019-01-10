package xyz.wildseries.wildstacker.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.api.objects.StackedSpawner;

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
