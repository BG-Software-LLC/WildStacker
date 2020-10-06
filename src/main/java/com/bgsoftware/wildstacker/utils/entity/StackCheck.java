package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;

public enum StackCheck {

    /**
     * Custom stack checks.
     */
    SPAWN_REASON(),
    NERFED(),
    NAME_TAG(),

    /**
     * Vanilla stack checks.
     */
    AGE(),
    EXACT_AGE(),
    CAN_BREED(),
    IS_TAMED(),
    ANIMAL_OWNER(),
    SKELETON_TYPE(EntityTypes.SKELETON, EntityTypes.WITHER_SKELETON),
    ZOMBIE_BABY(EntityTypes.ZOMBIE, EntityTypes.ZOMBIE_VILLAGER),
    SLIME_SIZE(EntityTypes.SLIME, EntityTypes.MAGMA_CUBE),
    ZOMBIE_PIGMAN_ANGRY(EntityTypes.ZOMBIE_PIGMAN),
    ENDERMAN_CARRIED_BLOCK(EntityTypes.ENDERMAN),
    BAT_AWAKE(EntityTypes.BAT),
    GUARDIAN_ELDER(EntityTypes.ELDER_GUARDIAN),
    PIG_SADDLE(EntityTypes.PIG),
    SHEEP_SHEARED(EntityTypes.SHEEP),
    SHEEP_COLOR(EntityTypes.SHEEP),
    WOLF_ANGRY(EntityTypes.WOLF),
    WOLF_COLLAR_COLOR(EntityTypes.WOLF),
    OCELOT_TYPE(EntityTypes.OCELOT),
    CAT_TYPE(EntityTypes.CAT),
    CAT_COLLAR_COLOR(EntityTypes.CAT),
    HORSE_TYPE(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_COLOR(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_STYLE(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_CARRYING_CHEST(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_TAME_PROGRESS(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_MAX_TAME_PROGRESS(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    HORSE_JUMP(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE),
    RABBIT_TYPE(EntityTypes.RABBIT),
    VILLAGER_PROFESSION(EntityTypes.VILLAGER, EntityTypes.ZOMBIE_VILLAGER),
    LLAMA_COLOR(EntityTypes.LLAMA),
    LLAMA_STRENGTH(EntityTypes.LLAMA),
    PARROT_TYPE(EntityTypes.PARROT),
    PUFFERFISH_STATE(EntityTypes.PUFFERFISH),
    TROPICALFISH_TYPE(EntityTypes.TROPICAL_FISH),
    TROPICALFISH_BODY_COLOR(EntityTypes.TROPICAL_FISH),
    TROPICALFISH_TYPE_COLOR(EntityTypes.TROPICAL_FISH),
    PHANTOM_SIZE(EntityTypes.PHANTOM);

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
