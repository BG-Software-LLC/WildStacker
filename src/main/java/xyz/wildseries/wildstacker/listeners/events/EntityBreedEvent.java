package xyz.wildseries.wildstacker.listeners.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public final class EntityBreedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private LivingEntity father, mother;

    public EntityBreedEvent(LivingEntity father, LivingEntity mother){
        this.father = father;
        this.mother = mother;
    }

    public LivingEntity getFather() {
        return father;
    }

    public LivingEntity getMother() {
        return mother;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
