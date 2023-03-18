package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.common.reflection.ReflectField;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class EntityDamageData {

    private static final ReflectField<Map<EntityDamageEvent.DamageModifier, Double>> MODIFIERS_FIELD =
            new ReflectField<>(EntityDamageEvent.class, Map.class, "modifiers");
    private static final ReflectField<Map<EntityDamageEvent.DamageModifier, Double>> ORIGINALS_FIELD =
            new ReflectField<>(EntityDamageEvent.class, Map.class, "originals");

    private final Map<EntityDamageEvent.DamageModifier, Double> modifiers;
    private boolean cancelled;

    public EntityDamageData(EntityDamageEvent event) {
        // We copy all the modifiable fields of the event.
        this(event.isCancelled(), MODIFIERS_FIELD.get(event));

        // We clear the values of the current event
        MODIFIERS_FIELD.set(event, new EnumMap<>(ORIGINALS_FIELD.get(event)));
        event.setCancelled(false);
    }

    public EntityDamageData(boolean cancelled, double baseDamage) {
        this(cancelled, Collections.emptyMap());
        this.modifiers.put(EntityDamageEvent.DamageModifier.BASE, baseDamage);
    }

    public EntityDamageData(boolean cancelled, Map<EntityDamageEvent.DamageModifier, Double> damageModifiers) {
        this.cancelled = cancelled;
        this.modifiers = damageModifiers.isEmpty() ? new EnumMap<>(EntityDamageEvent.DamageModifier.class) : new EnumMap<>(damageModifiers);
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public Map<EntityDamageEvent.DamageModifier, Double> getModifiers() {
        return modifiers;
    }

    public void applyToEvent(EntityDamageEvent event) {
        if (modifiers.containsKey(EntityDamageEvent.DamageModifier.BASE))
            MODIFIERS_FIELD.set(event, modifiers);

        event.setCancelled(this.cancelled);
    }

}
