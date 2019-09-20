package com.bgsoftware.wildstacker.api;

import com.bgsoftware.wildstacker.api.handlers.SystemManager;

public interface WildStacker {

    /**
     * Get the system manager of the plugin.
     * The manager contains many useful methods.
     * @return The system manager.
     */
    SystemManager getSystemManager();

}
