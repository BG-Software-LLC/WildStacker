package com.bgsoftware.wildstacker.api.enums;

public enum StackSplit {

    ENTITY_BREED,
    FEED,
    MUSHROOM_SHEAR,
    NAME_TAG,
    SHEEP_DYE,
    SHEEP_SHEAR,
    VILLAGER_INFECTION;

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
