package com.bgsoftware.wildstacker.hooks;

import org.bukkit.entity.Player;

public interface AntiCheatProvider {

    void enableBypass(Player player);

    void disableBypass(Player player);

}
