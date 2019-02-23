package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public final class HologramsProvider_HolographicDisplays implements HologramsProvider {

    private WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public HologramsProvider_HolographicDisplays(){
        WildStackerPlugin.log(" - Using HolographicDisplays as HologramsProvider.");
    }

    @Override
    public void createHologram(StackedObject stackedObject, String line) {
        Location location = null;

        if(stackedObject instanceof StackedSpawner)
            location = ((StackedSpawner) stackedObject).getLocation();
        else if(stackedObject instanceof StackedBarrel)
            location = ((StackedBarrel) stackedObject).getLocation();

        if(location != null) {
            createHologram(location.add(0.5, 1.5, 0.5), line);
        }
    }

    @Override
    public void createHologram(Location location, String line) {
        Hologram hologram = HologramsAPI.createHologram(plugin, location);
        hologram.appendTextLine(line);
    }

    @Override
    public void deleteHologram(StackedObject stackedObject) {
        Location location = null;

        if(stackedObject instanceof StackedSpawner)
            location = ((StackedSpawner) stackedObject).getLocation();
        else if(stackedObject instanceof StackedBarrel)
            location = ((StackedBarrel) stackedObject).getLocation();

        if(location != null) {
            deleteHologram(location.add(0.5, 1.5, 0.5));
        }
    }

    @Override
    public void deleteHologram(Location location) {
        Hologram hologram = getHologram(location);

        if(hologram == null)
            return;

        hologram.delete();
    }

    @Override
    public void changeLine(StackedObject stackedObject, String newLine, boolean createIfNull) {
        Location location = null;

        if(stackedObject instanceof StackedSpawner)
            location = ((StackedSpawner) stackedObject).getLocation();
        else if(stackedObject instanceof StackedBarrel)
            location = ((StackedBarrel) stackedObject).getLocation();

        if(location != null) {
            changeLine(location.add(0.5, 1.5, 0.5), newLine, createIfNull);
        }
    }

    @Override
    public void changeLine(Location location, String newLine, boolean createIfNull) {
        Hologram hologram = getHologram(location);

        if(hologram == null) {
            if(!createIfNull)
                return;
            createHologram(location, newLine);
            return;
        }

        hologram.clearLines();
        hologram.appendTextLine(newLine);
    }

    @Override
    public void clearHolograms() {
        for(Hologram hologram : HologramsAPI.getHolograms(plugin)){
            Block underBlock = hologram.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if(!plugin.getSystemManager().isStackedBarrel(underBlock) && !plugin.getSystemManager().isStackedSpawner(underBlock)) {
                hologram.delete();
                break;
            }
        }
    }

    @Override
    public boolean isHologram(Location location) {
        return getHologram(location) != null;
    }

    private Hologram getHologram(Location location){
        for(Hologram hologram : HologramsAPI.getHolograms(plugin)){
            if(hologram.getLocation().equals(location)) {
                return hologram;
            }
        }

        return null;
    }

}
