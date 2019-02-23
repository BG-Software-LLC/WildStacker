package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.entity.Player;

public final class AntiCheatProvider_Default implements AntiCheatProvider {

    public AntiCheatProvider_Default(){
        WildStackerPlugin.log(" - Couldn't find any anti-cheat providers, using default one.");
    }

    @Override
    public void enableBypass(Player player) {
        //Nothing to do :>
    }

    @Override
    public void disableBypass(Player player) {
        //Nothing to do :>
    }
}
