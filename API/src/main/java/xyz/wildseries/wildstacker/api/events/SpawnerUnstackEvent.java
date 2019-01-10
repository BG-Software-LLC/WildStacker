package xyz.wildseries.wildstacker.api.events;

import org.bukkit.event.HandlerList;
import xyz.wildseries.wildstacker.api.objects.StackedSpawner;

@SuppressWarnings("unused")
public class SpawnerUnstackEvent extends UnstackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public SpawnerUnstackEvent(StackedSpawner spawner, int unstackAmount){
        super(spawner, unstackAmount);
    }

    public StackedSpawner getSpawner() {
        return (StackedSpawner) object;
    }

    public int getAmount(){
        return unstackAmount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
