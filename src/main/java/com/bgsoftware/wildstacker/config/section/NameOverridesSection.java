package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;

import java.util.Map;

public class NameOverridesSection extends SettingsContainerHolder implements SettingsManager.NameOverrides {

    @Override
    public Map<String, String> getOverrides() {
        return getContainer().customNames;
    }
}
