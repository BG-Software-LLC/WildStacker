package xyz.wildseries.wildstacker.api.events;

import org.bukkit.event.HandlerList;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;

@SuppressWarnings("unused")
public class EntityUnstackEvent extends UnstackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public EntityUnstackEvent(StackedEntity entity, int unstackAmount){
        super(entity, unstackAmount);
    }

    public StackedEntity getEntity() {
        return (StackedEntity) object;
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
