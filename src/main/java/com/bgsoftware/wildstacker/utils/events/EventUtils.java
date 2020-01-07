package com.bgsoftware.wildstacker.utils.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public final class EventUtils {

    public static boolean callEntityDamageEvent(EntityDamageEvent original){
        Event event = original instanceof EntityDamageByEntityEvent ?
                new FakeEntityDamageByEntityEvent((EntityDamageByEntityEvent) original) : new FakeEntityDamageEvent(original);
        Bukkit.getPluginManager().callEvent(event);
        return !((Cancellable) event).isCancelled();
    }

    public static boolean isFakeEvent(EntityDamageEvent event){
        return event instanceof FakeEntityDamageEvent || event instanceof FakeEntityDamageByEntityEvent;
    }

    private static final class FakeEntityDamageByEntityEvent extends EntityDamageByEntityEvent{

        FakeEntityDamageByEntityEvent(EntityDamageByEntityEvent entityDamageByEntityEvent){
            //noinspection deprecation
            super(entityDamageByEntityEvent.getDamager(), entityDamageByEntityEvent.getEntity(),
                    entityDamageByEntityEvent.getCause(), entityDamageByEntityEvent.getFinalDamage());
        }

    }

    private static final class FakeEntityDamageEvent extends EntityDamageEvent{

        FakeEntityDamageEvent(EntityDamageEvent entityDamageEvent){
            //noinspection deprecation
            super(entityDamageEvent.getEntity(), entityDamageEvent.getCause(), entityDamageEvent.getFinalDamage());
        }

    }

}
