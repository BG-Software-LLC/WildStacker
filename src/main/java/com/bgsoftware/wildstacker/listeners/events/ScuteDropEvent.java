package com.bgsoftware.wildstacker.listeners.events;

import org.bukkit.entity.Item;
import org.bukkit.entity.Turtle;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class ScuteDropEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Turtle turtle;
    private final Item scute;

    public ScuteDropEvent(Item scute, Turtle turtle){
        this.turtle = turtle;
        this.scute = scute;
    }

    public Item getScute() {
        return scute;
    }

    public Turtle getTurtle() {
        return turtle;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
