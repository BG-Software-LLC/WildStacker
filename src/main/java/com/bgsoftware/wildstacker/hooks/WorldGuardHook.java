package com.bgsoftware.wildstacker.hooks;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldGuardHook {

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static List<String> getRegionsName(org.bukkit.Location bukkitLocation){
        if(Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            ApplicableRegionSet applicableRegionSet;
            try {
                RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                Location location = new Location(BukkitUtil.getLocalWorld(bukkitLocation.getWorld()), bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
                applicableRegionSet = regionContainer.createQuery().getApplicableRegions(location);
            }catch(Throwable ex){
                try {
                    Class<?> regionQueryClass = Class.forName("com.sk89q.worldguard.bukkit.RegionQuery");
                    Class<?> regionContainerClass = Class.forName("com.sk89q.worldguard.bukkit.RegionContainer");
                    Object regionContainer = WorldGuardPlugin.class.getMethod("getRegionContainer").invoke(WorldGuardPlugin.inst());
                    Object regionQuery = regionContainerClass.getMethod("createQuery").invoke(regionContainer);
                    applicableRegionSet = (ApplicableRegionSet) regionQueryClass.getMethod("getApplicableRegions", bukkitLocation.getClass())
                            .invoke(regionQuery, bukkitLocation);
                }catch(Exception ex1){
                    ex1.printStackTrace();
                    return new ArrayList<>();
                }
            }
            return applicableRegionSet.getRegions().stream().map(ProtectedRegion::getId).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

}
