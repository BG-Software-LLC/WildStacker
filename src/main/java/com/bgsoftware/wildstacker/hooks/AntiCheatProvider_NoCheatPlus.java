package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.entity.Player;

public final class AntiCheatProvider_NoCheatPlus implements AntiCheatProvider {

    public AntiCheatProvider_NoCheatPlus(){
        WildStackerPlugin.log(" - Using NoCheatPlus as AntiCheatProvider.");
    }

    @Override
    public void enableBypass(Player player) {
        NCPExemptionManager.exemptPermanently(player);
    }

    @Override
    public void disableBypass(Player player) {
        NCPExemptionManager.unexempt(player);
    }
}
