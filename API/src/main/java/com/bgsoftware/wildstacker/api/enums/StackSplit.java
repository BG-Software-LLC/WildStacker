package com.bgsoftware.wildstacker.api.enums;

public enum StackSplit {

    BEE_AGRO,
    ENTITY_BREED,
    ENTER_VEHICLE,
    IRON_GOLEM_AGRO,
    MUSHROOM_SHEAR,
    NAME_TAG,
    PIGMAN_AGRO,
    SHEEP_DYE,
    SHEEP_SHEAR,
    VILLAGER_INFECTION,
    WOLF_AGRO;

    private boolean enabled;

    StackSplit() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
