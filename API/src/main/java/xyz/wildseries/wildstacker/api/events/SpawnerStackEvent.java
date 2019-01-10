package xyz.wildseries.wildstacker.api.events;

import org.bukkit.event.HandlerList;
import xyz.wildseries.wildstacker.api.objects.StackedSpawner;

@SuppressWarnings("unused")
public class SpawnerStackEvent extends StackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public SpawnerStackEvent(StackedSpawner spawner, StackedSpawner target){
        super(spawner, target);
    }

    public StackedSpawner getSpawner() {
        return (StackedSpawner) object;
    }

    public StackedSpawner getTarget() {
        return (StackedSpawner) target;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
