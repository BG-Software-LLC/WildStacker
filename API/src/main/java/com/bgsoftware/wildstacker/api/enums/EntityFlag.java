package com.bgsoftware.wildstacker.api.enums;

public enum EntityFlag {

    CORPSE(Boolean.class),
    SPAWN_CAUSE(SpawnCause.class),
    NAME_TAG(Boolean.class),
    ORIGINAL_AMOUNT(Integer.class),
    DEAD_ENTITY(Boolean.class),
    DEMO_ENTITY(Boolean.class),
    REMOVED_ENTITY(Boolean.class),
    BREEDABLE_AMOUNT(Integer.class),
    BYPASS_STACKING(Boolean.class),
    EXP_TO_DROP(Integer.class),

    // Items related flags
    DROPPED_BY_PLAYER(Boolean.class),
    RECENTLY_PICKED_UP(Boolean.class);

    private final Class<?> valueClass;

    EntityFlag(Class<?> valueClass){
        this.valueClass = valueClass;
    }

    public Class<?> getValueClass() {
        return valueClass;
    }

}
