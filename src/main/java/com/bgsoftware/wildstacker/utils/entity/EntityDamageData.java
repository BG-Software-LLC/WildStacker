package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.common.reflection.ReflectField;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.EnumMap;
import java.util.Map;

public class EntityDamageData {

    private static final ReflectField<Map<EntityDamageEvent.DamageModifier, Double>> MODIFIERS_FIELD =
            new ReflectField<>(EntityDamageEvent.class, Map.class, "modifiers");
    private static final ReflectField<Map<EntityDamageEvent.DamageModifier, Double>> ORIGINALS_FIELD =
            new ReflectField<>(EntityDamageEvent.class, Map.class, "originals");

    private final Map<EntityDamageEvent.DamageModifier, Double> modifiers;
    private final boolean cancelled;

    public EntityDamageData(EntityDamageEvent event) {
        // We copy all the modifiable fields of the event.
        this.modifiers = MODIFIERS_FIELD.get(event);
        this.cancelled = event.isCancelled();

        // We clear the values of the current event
        MODIFIERS_FIELD.set(event, new EnumMap<>(ORIGINALS_FIELD.get(event)));
        event.setCancelled(false);
    }

    public void restoreEvent(EntityDamageEvent event) {
        MODIFIERS_FIELD.set(event, modifiers);
        event.setCancelled(this.cancelled);
    }

}
