package xyz.wildseries.wildstacker.hooks;

import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.entity.Player;
import xyz.wildseries.wildstacker.WildStackerPlugin;

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
