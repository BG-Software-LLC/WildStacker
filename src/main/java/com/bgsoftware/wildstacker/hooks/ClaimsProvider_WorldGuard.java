package com.bgsoftware.wildstacker.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class ClaimsProvider_WorldGuard implements ClaimsProvider {

    private WorldGuardPlugin worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

    @Override
    public boolean hasClaimAccess(Player player, org.bukkit.Location bukkitLocation) {
        try {
            WorldGuardPlatform worldGuardPlatform = WorldGuard.getInstance().getPlatform();
            RegionContainer regionContainer = worldGuardPlatform.getRegionContainer();
            LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
            Location location = new Location(localPlayer.getExtent(), bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
            RegionQuery regionQuery = regionContainer.createQuery();
            return worldGuardPlatform.getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(bukkitLocation.getWorld())) || regionQuery.testBuild(location, localPlayer);
        }catch(Throwable ex){
            try {
                return (boolean) worldGuard.getClass().getMethod("canBuild", Player.class, Block.class).invoke(worldGuard, player, bukkitLocation.getBlock());
            }catch(Exception ignored){}
        }
        return false;
    }
}
