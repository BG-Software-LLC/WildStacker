package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.utils.reflection.ReflectionUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class WorldGuardHook {

    private static final WorldGuardProvider worldGuardProvider;

    static {
        if(ReflectionUtils.isPluginEnabled("com.sk89q.worldguard.protection.regions.RegionContainer")){
            worldGuardProvider = new WorldGuardProvider() {};
        }
        else{
            worldGuardProvider = newOldInstance();
        }
    }

    public static List<String> getRegionsName(org.bukkit.Location bukkitLocation){
        return PluginHooks.isWorldGuardEnabled ? worldGuardProvider.getRegionNames(bukkitLocation) : new ArrayList<>();
    }

    public static boolean hasClaimAccess(Player player, org.bukkit.Location bukkitLocation){
        return !PluginHooks.isWorldGuardEnabled || worldGuardProvider.hasClaimAccess(player, bukkitLocation);
    }

    public interface WorldGuardProvider{

        default List<String> getRegionNames(org.bukkit.Location bukkitLocation){
            com.sk89q.worldguard.protection.regions.RegionContainer regionContainer = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            com.sk89q.worldguard.protection.regions.RegionQuery regionQuery = regionContainer.createQuery();
            com.sk89q.worldedit.util.Location location = new com.sk89q.worldedit.util.Location(new com.sk89q.worldedit.bukkit.BukkitWorld(bukkitLocation.getWorld()), bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
            com.sk89q.worldguard.protection.ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(location);
            return applicableRegionSet.getRegions().stream().map(com.sk89q.worldguard.protection.regions.ProtectedRegion::getId)
                    .collect(Collectors.toList());
        }

        default boolean hasClaimAccess(Player player, org.bukkit.Location bukkitLocation){
            com.sk89q.worldguard.internal.platform.WorldGuardPlatform worldGuardPlatform = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform();
            com.sk89q.worldguard.protection.regions.RegionContainer regionContainer = worldGuardPlatform.getRegionContainer();
            com.sk89q.worldguard.protection.regions.RegionQuery regionQuery = regionContainer.createQuery();
            com.sk89q.worldguard.LocalPlayer localPlayer = com.sk89q.worldguard.bukkit.WorldGuardPlugin.inst().wrapPlayer(player);
            com.sk89q.worldedit.util.Location location = new com.sk89q.worldedit.util.Location(localPlayer.getExtent(), bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
            return worldGuardPlatform.getSessionManager().hasBypass(localPlayer, com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(bukkitLocation.getWorld())) || regionQuery.testBuild(location, localPlayer);
        }

    }

    private static WorldGuardProvider newOldInstance(){
        try{
            return (WorldGuardProvider) Class.forName("com.bgsoftware.wildstacker.hooks.WorldGuardHook_Old").newInstance();
        }catch(Throwable ex){
            return new WorldGuardProvider() {};
        }
    }

}
