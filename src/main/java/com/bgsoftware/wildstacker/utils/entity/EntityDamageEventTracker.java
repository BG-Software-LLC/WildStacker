package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.common.reflection.ReflectField;
import com.google.common.base.Function;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;

public class EntityDamageEventTracker {

    private static final ReflectField<Map<EntityDamageEvent.DamageModifier, Double>> MODIFIERS_FIELD =
            new ReflectField<>(EntityDamageEvent.class, Map.class, "modifiers");
    private static final ReflectField<Map<EntityDamageEvent.DamageModifier, ? extends Function<? super Double, Double>>> MODIFIER_FUNCTIONS_FIELD =
            new ReflectField<>(EntityDamageEvent.class, Map.class, "modifierFunctions");

    public static EntityDamageEvent createEvent(EntityDamageEvent handle) {
        if (isTracker(handle)) {
            return handle;
        } else if (handle instanceof EntityDamageByEntityEvent) {
            return new TrackDamageByEntityEvent((EntityDamageByEntityEvent) handle);
        } else {
            return new TrackDamageEvent(handle);
        }
    }

    public static boolean isTracker(EntityDamageEvent event) {
        return event instanceof TrackerEvent;
    }

    private interface TrackerEvent {

    }

    private static class TrackDamageEvent extends EntityDamageEvent implements TrackerEvent {

        TrackDamageEvent(EntityDamageEvent handle) {
            super(handle.getEntity(), handle.getCause(), MODIFIERS_FIELD.get(handle), MODIFIER_FUNCTIONS_FIELD.get(handle));
        }

    }

    private static class TrackDamageByEntityEvent extends EntityDamageByEntityEvent implements TrackerEvent {

        TrackDamageByEntityEvent(EntityDamageByEntityEvent handle) {
            super(handle.getDamager(), handle.getEntity(), handle.getCause(), MODIFIERS_FIELD.get(handle), MODIFIER_FUNCTIONS_FIELD.get(handle));
        }

    }

}
