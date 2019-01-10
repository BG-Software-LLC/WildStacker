package xyz.wildseries.wildstacker.api.events;

import org.bukkit.event.HandlerList;
import xyz.wildseries.wildstacker.api.objects.StackedItem;

@SuppressWarnings("unused")
public class ItemStackEvent extends StackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public ItemStackEvent(StackedItem item, StackedItem target){
        super(item, target);
    }

    public StackedItem getItem() {
        return (StackedItem) object;
    }

    public StackedItem getTarget() {
        return (StackedItem) target;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
