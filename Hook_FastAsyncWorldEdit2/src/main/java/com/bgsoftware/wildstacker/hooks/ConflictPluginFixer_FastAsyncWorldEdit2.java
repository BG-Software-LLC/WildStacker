package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.fastasyncworldedit.core.configuration.Settings;

@SuppressWarnings("unused")
public record ConflictPluginFixer_FastAsyncWorldEdit2(WildStackerPlugin plugin) implements ConflictPluginFixer {

    @Override
    public void fixConflict() {
        if (plugin.getSettings().itemsStackingEnabled) {
            Settings.settings().TICK_LIMITER.ITEMS = Integer.MAX_VALUE;
            WildStackerPlugin.log("");
            WildStackerPlugin.log("Detected FastAsyncWorldEdit - Disabling ticks limiter for items...");
        }
    }

}
