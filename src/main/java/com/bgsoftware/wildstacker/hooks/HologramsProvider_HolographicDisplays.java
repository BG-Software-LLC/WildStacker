package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.function.Function;

public final class HologramsProvider_HolographicDisplays implements HologramsProvider {

    private WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private Function<Location, Location> parseLocation = location -> location.add(0.5, 1.5, 0.5);

    public HologramsProvider_HolographicDisplays(){
        WildStackerPlugin.log(" - Using HolographicDisplays as HologramsProvider.");
    }

    @Override
    public void createHologram(StackedObject stackedObject, String line) {
        createHologram(parseLocation.apply(stackedObject.getLocation()), line);
    }

    @Override
    public void createHologram(Location location, String line) {
        Hologram hologram = HologramsAPI.createHologram(plugin, location);
        hologram.appendTextLine(line);
    }

    @Override
    public void deleteHologram(StackedObject stackedObject) {
        deleteHologram(parseLocation.apply(stackedObject.getLocation()));
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
        changeLine(parseLocation.apply(stackedObject.getLocation()), newLine, createIfNull);
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
        try {
            for (Hologram hologram : HologramsAPI.getHolograms(plugin)) {
                if (GeneralUtils.isChunkLoaded(hologram.getLocation())) {
                    Block underBlock = hologram.getLocation().getBlock().getRelative(BlockFace.DOWN);
                    if (!plugin.getSystemManager().isStackedBarrel(underBlock) && !plugin.getSystemManager().isStackedSpawner(underBlock)) {
                        hologram.delete();
                        break;
                    }
                }
            }
        }catch(Exception ignored){}
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
