package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.songoda.arconix.api.ArconixAPI;
import com.songoda.arconix.plugin.Arconix;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public final class HologramsProvider_Arconix implements HologramsProvider, Listener {

    private WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private ArconixAPI arconixAPI = Arconix.pl().getApi();
    private List<Location> savedLocations = new ArrayList<>();

    public HologramsProvider_Arconix(){
        WildStackerPlugin.log(" - Using Arconix as HologramsProvider.");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void createHologram(StackedObject stackedObject, String line) {
        Location location = null;

        if(stackedObject instanceof StackedSpawner)
            location = ((StackedSpawner) stackedObject).getLocation();
        else if(stackedObject instanceof StackedBarrel)
            location = ((StackedBarrel) stackedObject).getLocation();

        if(location != null) {
            createHologram(location.add(0.5, 1, 0.5), line);
        }
    }

    @Override
    public void createHologram(Location location, String line) {
        arconixAPI.packetLibrary.getHologramManager().spawnHologram(location, line);
        savedLocations.add(location);
    }

    @Override
    public void deleteHologram(StackedObject stackedObject) {
        Location location = null;

        if(stackedObject instanceof StackedSpawner)
            location = ((StackedSpawner) stackedObject).getLocation();
        else if(stackedObject instanceof StackedBarrel)
            location = ((StackedBarrel) stackedObject).getLocation();

        if(location != null) {
            deleteHologram(location.add(0.5, 1, 0.5));
        }
    }

    @Override
    public void deleteHologram(Location location) {
        if(!isHologram(location))
            return;

        arconixAPI.packetLibrary.getHologramManager().despawnHologram(location);
        savedLocations.remove(location);
    }

    @Override
    public void changeLine(StackedObject stackedObject, String newLine, boolean createIfNull) {
        Location location = null;

        if(stackedObject instanceof StackedSpawner)
            location = ((StackedSpawner) stackedObject).getLocation();
        else if(stackedObject instanceof StackedBarrel)
            location = ((StackedBarrel) stackedObject).getLocation();

        if(location != null) {
            changeLine(location.add(0.5, 1, 0.5), newLine, createIfNull);
        }
    }

    @Override
    public void changeLine(Location location, String newLine, boolean createIfNull) {
        if(isHologram(location)) {
            deleteHologram(location);
        }else if(!createIfNull)
            return;

        createHologram(location, newLine);
    }

    @Override
    public void clearHolograms() {
        ArrayList<Location> hologramsLocations = arconixAPI.packetLibrary.getHologramManager().getLocations();
        for(Location location : hologramsLocations){
            if(GeneralUtils.isChunkLoaded(location)) {
                Block underBlock = location.getBlock().getRelative(BlockFace.DOWN);
                if (savedLocations.contains(location) &&
                        (!plugin.getSystemManager().isStackedSpawner(underBlock) && !plugin.getSystemManager().isStackedBarrel(underBlock))) {
                    deleteHologram(location);
                }
            }
        }
    }

    @Override
    public boolean isHologram(Location location) {
        return savedLocations.contains(location);
    }

}
