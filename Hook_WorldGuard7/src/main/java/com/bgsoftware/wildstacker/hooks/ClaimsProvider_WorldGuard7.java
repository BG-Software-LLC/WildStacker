package com.bgsoftware.wildstacker.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

public final class ClaimsProvider_WorldGuard7 implements ClaimsProvider {

    @Override
    public boolean hasClaimAccess(Player player, org.bukkit.Location bukkitLocation) {
        WorldGuardPlatform worldGuardPlatform = WorldGuard.getInstance().getPlatform();
        RegionContainer regionContainer = worldGuardPlatform.getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        Location location = new Location(localPlayer.getExtent(), bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
        return worldGuardPlatform.getSessionManager().hasBypass(localPlayer,
                BukkitAdapter.adapt(bukkitLocation.getWorld())) || regionQuery.testBuild(location, localPlayer);
    }

}
