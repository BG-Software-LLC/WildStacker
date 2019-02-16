package xyz.wildseries.wildstacker.hooks;

import org.bukkit.entity.Player;
import xyz.wildseries.wildstacker.WildStackerPlugin;

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
