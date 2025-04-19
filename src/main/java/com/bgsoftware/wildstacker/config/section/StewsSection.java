package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;

public class StewsSection extends SettingsContainerHolder implements SettingsManager.Stews {

    @Override
    public boolean isEnabled() {
        return getContainer().stewsStackingEnabled;
    }

    @Override
    public int getMaxStack() {
        return getContainer().stewsMaxStack;
    }
}
