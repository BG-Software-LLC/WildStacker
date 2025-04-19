package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;

import java.util.List;

public class BucketsSection extends SettingsContainerHolder implements SettingsManager.Buckets {

    @Override
    public boolean isEnabled() {
        return getContainer().bucketsStackerEnabled;
    }

    @Override
    public int getMaxStack() {
        return getContainer().bucketsMaxStack;
    }

    @Override
    public List<String> getBlacklistedNames() {
        return getContainer().bucketsBlacklistedNames;
    }
}
