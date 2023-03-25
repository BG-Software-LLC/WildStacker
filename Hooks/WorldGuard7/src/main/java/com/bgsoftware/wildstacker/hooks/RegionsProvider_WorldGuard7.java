package com.bgsoftware.wildstacker.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import java.util.List;
import java.util.stream.Collectors;

public final class RegionsProvider_WorldGuard7 implements RegionsProvider {

    @Override
    public List<String> getRegionNames(org.bukkit.Location bukkitLocation) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();

        Location location = new Location(BukkitAdapter.adapt(bukkitLocation.getWorld()),
                bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());

        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(location);

        return applicableRegionSet.getRegions().stream()
                .map(ProtectedRegion::getId)
                .collect(Collectors.toList());
    }

}
