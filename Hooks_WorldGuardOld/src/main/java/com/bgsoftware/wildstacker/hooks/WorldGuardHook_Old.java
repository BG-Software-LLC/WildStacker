package com.bgsoftware.wildstacker.hooks;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class WorldGuardHook_Old implements WorldGuardHook.WorldGuardProvider {

    @Override
    public List<String> getRegionNames(Location bukkitLocation) {
        RegionContainer regionContainer = WorldGuardPlugin.inst().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(bukkitLocation);
        return applicableRegionSet.getRegions().stream().map(ProtectedRegion::getId).collect(Collectors.toList());
    }

    @Override
    public boolean hasClaimAccess(Player player, Location bukkitLocation) {
        return WorldGuardPlugin.inst().canBuild(player, bukkitLocation);
    }
}
