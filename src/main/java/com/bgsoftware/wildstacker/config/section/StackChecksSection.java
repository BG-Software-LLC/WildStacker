package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.api.config.StackCheckType;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;

public class StackChecksSection extends SettingsContainerHolder implements SettingsManager.StackChecks {

    @Override
    public boolean isCheckEnabled(StackCheckType check) {
        return check.isEnabled();
    }
}
