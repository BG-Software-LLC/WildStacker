package xyz.wildseries.wildstacker.hooks;

import me.vagdedes.spartan.api.API;
import me.vagdedes.spartan.system.Enums;
import org.bukkit.entity.Player;
import xyz.wildseries.wildstacker.WildStackerPlugin;

public final class AntiCheatProvider_Spartan implements AntiCheatProvider {

    public AntiCheatProvider_Spartan(){
        WildStackerPlugin.log(" - Using Spartan as AntiCheatProvider.");
    }

    @Override
    public void enableBypass(Player player) {
        for(Enums.HackType type : Enums.HackType.values())
            API.stopCheck(player, type);
    }

    @Override
    public void disableBypass(Player player) {
        for(Enums.HackType type : Enums.HackType.values())
            API.startCheck(player, type);
    }
}
