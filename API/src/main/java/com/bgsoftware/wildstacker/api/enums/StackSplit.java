package com.bgsoftware.wildstacker.api.enums;

public enum StackSplit {

    FEED,
    NAME_TAG,
    MUSHROOM_SHEAR,
    SHEEP_SHEAR,
    SHEEP_DYE,
    ENTITY_BREED;

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
