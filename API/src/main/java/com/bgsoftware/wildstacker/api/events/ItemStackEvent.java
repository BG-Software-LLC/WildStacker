package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedItem;
import org.bukkit.event.HandlerList;

/**
 * ItemStackEvent is called when an item is stacked into another item.
 */
public class ItemStackEvent extends StackEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The constructor for the event.
     * @param item The original item object.
     * @param target The item object that is stacked.
     */
    public ItemStackEvent(StackedItem item, StackedItem target){
        super(item, target);
    }

    /**
     * Get the original item.
     */
    public StackedItem getItem() {
        return (StackedItem) object;
    }

    /**
     * Get the item that is stacked.
     */
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
