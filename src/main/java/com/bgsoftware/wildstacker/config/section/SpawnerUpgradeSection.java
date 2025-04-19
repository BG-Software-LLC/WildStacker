package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;

public class SpawnerUpgradeSection extends SettingsContainerHolder implements SettingsManager.SpawnerUpgrades {

    @Override
    public boolean shouldMultiplyStackAmount() {
        return getContainer().spawnerUpgradesMultiplyStackAmount;
    }
}
