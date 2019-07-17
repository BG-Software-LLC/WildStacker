package com.bgsoftware.wildstacker.listeners.events;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class EggLayEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Chicken chicken;
    private final Item egg;

    public EggLayEvent(Item egg, Chicken chicken){
        this.chicken = chicken;
        this.egg = egg;
    }

    public Item getEgg() {
        return egg;
    }

    public Chicken getChicken() {
        return chicken;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
