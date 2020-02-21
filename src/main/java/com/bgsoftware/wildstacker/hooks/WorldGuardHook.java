package com.bgsoftware.wildstacker.hooks;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class WorldGuardHook {

    public static List<String> getRegionsName(org.bukkit.Location bukkitLocation){
        if(PluginHooks.isWorldGuardEnabled) {
            try {
                ApplicableRegionSet applicableRegionSet;
                try {
                    RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionQuery regionQuery = regionContainer.createQuery();
                    Location location = new Location(new BukkitWorld(bukkitLocation.getWorld()), bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
                    applicableRegionSet = regionQuery.getApplicableRegions(location);
                } catch (Throwable ex) {
                    com.sk89q.worldguard.bukkit.RegionContainer regionContainer = WorldGuardPlugin.inst().getRegionContainer();
                    com.sk89q.worldguard.bukkit.RegionQuery regionQuery = regionContainer.createQuery();
                    applicableRegionSet = regionQuery.getApplicableRegions(bukkitLocation);
                }
                return applicableRegionSet.getRegions().stream().map(ProtectedRegion::getId).collect(Collectors.toList());
            }catch(Throwable ignored){}
        }
        return new ArrayList<>();
    }

}
