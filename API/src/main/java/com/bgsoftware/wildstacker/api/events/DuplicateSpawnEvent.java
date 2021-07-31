package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * DuplicateSpawnEvent is called when a duplicate of an entity is spawned.
 */
public class DuplicateSpawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedEntity stackedEntity;
    private final StackedEntity duplicate;

    /**
     * The constructor for the event.
     *
     * @param stackedEntity The original entity.
     * @param duplicate     The duplicated entity.
     */
    public DuplicateSpawnEvent(StackedEntity stackedEntity, StackedEntity duplicate) {
        this.stackedEntity = stackedEntity;
        this.duplicate = duplicate;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the original entity.
     */
    public StackedEntity getEntity() {
        return stackedEntity;
    }

    /**
     * Get the duplicated entity.
     */
    public StackedEntity getDuplicate() {
        return duplicate;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
