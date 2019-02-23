package com.bgsoftware.wildstacker.api.enums;

public enum StackCheck {

    AGE(""),
    EXACT_AGE("Age"),
    CAN_BREED(""),
    IS_TAMED(""),
    ANIMAL_OWNER("OwnerUUID"),
    SKELETON_TYPE("SkeletonType"),
    ZOMBIE_BABY("IsBaby"),
    SLIME_SIZE("Size"),
    ZOMBIE_PIGMAN_ANGRY(""),
    ENDERMAN_CARRIED_BLOCK(""),
    BAT_AWAKE("BatFlags"),
    GUARDIAN_ELDER("Elder"),
    PIG_SADDLE("Saddle"),
    SHEEP_SHEARED("Sheared"),
    SHEEP_COLOR("Color"),
    WOLF_ANGRY("Angry"),
    WOLF_COLLAR_COLOR("CollarColor"),
    OCELOT_TYPE("CatType"),
    HORSE_TYPE("Type"),
    HORSE_COLOR(""),
    HORSE_STYLE(""),
    HORSE_CARRYING_CHEST("ChestedHorse"),
    HORSE_TAME_PROGRESS("Temper"),
    HORSE_MAX_TAME_PROGRESS("Bukkit.MaxDomestication"),
    HORSE_JUMP(""),
    RABBIT_TYPE("RabbitType"),
    VILLAGER_PROFESSION("IsVillager", "Profession"),
    LLAMA_COLOR("Variant"),
    LLAMA_STRENGTH("Strength"),
    PARROT_TYPE("Variant"),
    PUFFERFISH_STATE("PuffState"),
    TROPICALFISH_TYPE("Variant"),
    TROPICALFISH_BODY_COLOR(""),
    TROPICALFISH_TYPE_COLOR(""),
    PHANTOM_SIZE("Size");

    private boolean enabled;
    private String[] compoundKeys;

    StackCheck(String... compoundKeys) {
        this.enabled = false;
        this.compoundKeys = compoundKeys;
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
}
