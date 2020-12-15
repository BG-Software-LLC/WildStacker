package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;

public enum StackCheck {

    /**
     * Custom stack checks.
     */
    SPAWN_REASON(),
    NERFED(),
    NAME_TAG(),
    UPGRADE(),

    /**
     * Vanilla stack checks.
     */
    AGE(),
    ANIMAL_OWNER(),
    BAT_AWAKE(EntityTypes.BAT),
    CAN_BREED(),
    CAT_COLLAR_COLOR(EntityTypes.CAT),
    CAT_TYPE(EntityTypes.CAT),
    ENDERMAN_CARRIED_BLOCK(EntityTypes.ENDERMAN),
    EXACT_AGE(),
    GUARDIAN_ELDER(EntityTypes.ELDER_GUARDIAN),
    HORSE_CARRYING_CHEST(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_COLOR(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_JUMP(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_MAX_TAME_PROGRESS(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_STYLE(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_TAME_PROGRESS(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_TYPE(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    IS_TAMED(),
    LLAMA_COLOR(EntityTypes.LLAMA),
    LLAMA_STRENGTH(EntityTypes.LLAMA),
    MOOSHROOM_TYPE(EntityTypes.MOOSHROOM),
    OCELOT_TYPE(EntityTypes.OCELOT),
    PARROT_TYPE(EntityTypes.PARROT),
    PHANTOM_SIZE(EntityTypes.PHANTOM),
    PIG_SADDLE(EntityTypes.PIG),
    PUFFERFISH_STATE(EntityTypes.PUFFERFISH),
    RABBIT_TYPE(EntityTypes.RABBIT),
    SHEEP_COLOR(EntityTypes.SHEEP),
    SHEEP_SHEARED(EntityTypes.SHEEP),
    SKELETON_TYPE(EntityTypes.SKELETON, EntityTypes.WITHER_SKELETON),
    SLIME_SIZE(EntityTypes.SLIME, EntityTypes.MAGMA_CUBE),
    TROPICALFISH_BODY_COLOR(EntityTypes.TROPICAL_FISH),
    TROPICALFISH_TYPE(EntityTypes.TROPICAL_FISH),
    TROPICALFISH_TYPE_COLOR(EntityTypes.TROPICAL_FISH),
    VILLAGER_PROFESSION(EntityTypes.VILLAGER, EntityTypes.ZOMBIE_VILLAGER),
    WOLF_ANGRY(EntityTypes.WOLF),
    WOLF_COLLAR_COLOR(EntityTypes.WOLF),
    ZOMBIE_BABY(EntityTypes.ZOMBIE, EntityTypes.ZOMBIE_VILLAGER),
    ZOMBIE_PIGMAN_ANGRY(EntityTypes.ZOMBIE_PIGMAN);

    private boolean enabled;
    private final EntityTypes[] allowedTypes;

    StackCheck(EntityTypes... allowedTypes) {
        this.enabled = false;
        this.allowedTypes = allowedTypes;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isTypeAllowed(EntityTypes entityType){
        if(allowedTypes.length == 0)
            return true;

        for (EntityTypes allowedType : allowedTypes) {
            if (entityType == allowedType)
                return true;
        }

        return false;
    }

}
