package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.boydti.fawe.config.Settings;

public final class ConflictPluginFixer_FastAsyncWorldEdit implements ConflictPluginFixer {

    private final WildStackerPlugin plugin;

    public ConflictPluginFixer_FastAsyncWorldEdit(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void fixConflict() {
        if(plugin.getSettings().getItems().isEnabled()) {
            Settings.IMP.TICK_LIMITER.ITEMS = Integer.MAX_VALUE;
            WildStackerPlugin.log("");
            WildStackerPlugin.log("Detected FastAsyncWorldEdit - Disabling ticks limiter for items...");
        }
    }

}
