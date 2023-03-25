package com.bgsoftware.wildstacker.hooks;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

import java.util.List;
import java.util.stream.Collectors;

public final class RegionsProvider_WorldGuard6 implements RegionsProvider {

    @Override
    public List<String> getRegionNames(Location bukkitLocation) {
        RegionContainer regionContainer = WorldGuardPlugin.inst().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(bukkitLocation);
        return applicableRegionSet.getRegions().stream().map(ProtectedRegion::getId).collect(Collectors.toList());
    }

}
