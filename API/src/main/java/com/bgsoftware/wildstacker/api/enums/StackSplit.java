package com.bgsoftware.wildstacker.api.enums;

public enum StackSplit {

    BEE_AGRO,
    ENTITY_BREED,
    IRON_GOLEM_AGRO,
    MUSHROOM_SHEAR,
    NAME_TAG,
    PIGMAN_AGRO,
    SHEEP_DYE,
    SHEEP_SHEAR,
    VILLAGER_INFECTION,
    WOLF_AGRO;

    private boolean enabled;

    StackSplit(){
        this.enabled = false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
