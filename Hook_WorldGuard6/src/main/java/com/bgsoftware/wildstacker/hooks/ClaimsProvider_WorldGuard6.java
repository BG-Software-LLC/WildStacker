package com.bgsoftware.wildstacker.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_WorldGuard6 implements ClaimsProvider {

    @Override
    public boolean hasClaimAccess(Player player, Location location) {
        return WorldGuardPlugin.inst().canBuild(player, location);
    }

}
