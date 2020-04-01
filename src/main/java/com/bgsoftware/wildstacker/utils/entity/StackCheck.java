package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

public enum StackCheck {

    /**
     * Custom stack checks.
     */
    SPAWN_REASON(""),
    NERFED(""),
    NAME_TAG(""),

    /**
     * Vanilla stack checks.
     */
    AGE(""),
    EXACT_AGE("Age"),
    CAN_BREED(""),
    IS_TAMED(""),
    ANIMAL_OWNER("OwnerUUID"),
    SKELETON_TYPE(Sets.newHashSet(EntityTypes.SKELETON, EntityTypes.WITHER_SKELETON),"SkeletonType"),
    ZOMBIE_BABY(Sets.newHashSet(EntityTypes.ZOMBIE, EntityTypes.ZOMBIE_VILLAGER), "IsBaby"),
    SLIME_SIZE(Sets.newHashSet(EntityTypes.SLIME, EntityTypes.MAGMA_CUBE), "Size"),
    ZOMBIE_PIGMAN_ANGRY(Sets.newHashSet(EntityTypes.ZOMBIE_PIGMAN), ""),
    ENDERMAN_CARRIED_BLOCK(Sets.newHashSet(EntityTypes.ENDERMAN),""),
    BAT_AWAKE(Sets.newHashSet(EntityTypes.BAT), "BatFlags"),
    GUARDIAN_ELDER(Sets.newHashSet(EntityTypes.ELDER_GUARDIAN), "Elder"),
    PIG_SADDLE(Sets.newHashSet(EntityTypes.PIG), "Saddle"),
    SHEEP_SHEARED(Sets.newHashSet(EntityTypes.SHEEP), "Sheared"),
    SHEEP_COLOR(Sets.newHashSet(EntityTypes.SHEEP), "Color"),
    WOLF_ANGRY(Sets.newHashSet(EntityTypes.WOLF), "Angry"),
    WOLF_COLLAR_COLOR(Sets.newHashSet(EntityTypes.WOLF), "CollarColor"),
    OCELOT_TYPE(Sets.newHashSet(EntityTypes.OCELOT), "CatType"),
    CAT_TYPE(Sets.newHashSet(EntityTypes.CAT), "CatType"),
    CAT_COLLAR_COLOR(Sets.newHashSet(EntityTypes.CAT), "CollarColor"),
    HORSE_TYPE(Sets.newHashSet(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE), "Type"),
    HORSE_COLOR(Sets.newHashSet(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE), ""),
    HORSE_STYLE(Sets.newHashSet(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE), ""),
    HORSE_CARRYING_CHEST(Sets.newHashSet(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE), "ChestedHorse"),
    HORSE_TAME_PROGRESS(Sets.newHashSet(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE), "Temper"),
    HORSE_MAX_TAME_PROGRESS(Sets.newHashSet(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE), "Bukkit.MaxDomestication"),
    HORSE_JUMP(Sets.newHashSet(EntityTypes.HORSE, EntityTypes.SKELETON_HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.DONKEY, EntityTypes.MULE), ""),
    RABBIT_TYPE(Sets.newHashSet(EntityTypes.RABBIT), "RabbitType"),
    VILLAGER_PROFESSION(Sets.newHashSet(EntityTypes.VILLAGER, EntityTypes.ZOMBIE_VILLAGER), "IsVillager", "Profession"),
    LLAMA_COLOR(Sets.newHashSet(EntityTypes.LLAMA), "Variant"),
    LLAMA_STRENGTH(Sets.newHashSet(EntityTypes.LLAMA), "Strength"),
    PARROT_TYPE(Sets.newHashSet(EntityTypes.PARROT), "Variant"),
    PUFFERFISH_STATE(Sets.newHashSet(EntityTypes.PUFFERFISH), "PuffState"),
    TROPICALFISH_TYPE(Sets.newHashSet(EntityTypes.TROPICAL_FISH), "Variant"),
    TROPICALFISH_BODY_COLOR(Sets.newHashSet(EntityTypes.TROPICAL_FISH), ""),
    TROPICALFISH_TYPE_COLOR(Sets.newHashSet(EntityTypes.TROPICAL_FISH), ""),
    PHANTOM_SIZE(Sets.newHashSet(EntityTypes.PHANTOM), "Size");

    private boolean enabled;
    private final String[] compoundKeys;
    private final Set<EntityTypes> allowedTypes;

    StackCheck(String... compoundKeys){
        this(null, compoundKeys);
    }

    StackCheck(Set<EntityTypes> allowedTypes, String... compoundKeys) {
        this.enabled = false;
        this.compoundKeys = compoundKeys;
        this.allowedTypes = allowedTypes == null ? new HashSet<>() : allowedTypes;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String[] getCompoundKeys() {
        return compoundKeys;
    }

    public boolean isTypeAllowed(EntityTypes entityType){
        return allowedTypes.isEmpty() || allowedTypes.contains(entityType);
    }

}
