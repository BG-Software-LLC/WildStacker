package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;
import com.bgsoftware.wildstacker.api.enums.StackSplit;

public class StackSplitsSection extends SettingsContainerHolder implements SettingsManager.StackSplits {

    @Override
    public boolean isSplitEnabled(StackSplit split) {
        return split.isEnabled();
    }
}
