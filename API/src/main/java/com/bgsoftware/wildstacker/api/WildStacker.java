package com.bgsoftware.wildstacker.api;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.api.handlers.UpgradesManager;

public interface WildStacker {

    /**
     * Get the system manager of the plugin.
     * The manager contains many useful methods.
     */
    SystemManager getSystemManager();

    /**
     * Get the upgrades manager of the plugin.
     */
    UpgradesManager getUpgradesManager();

    /**
     * Get the settings of the plugin.
     */
    SettingsManager getSettings();

}
