package com.bgsoftware.wildstacker.hooks;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMILocation;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import com.Zrips.CMI.Modules.Holograms.HologramManager;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Collections;
import java.util.function.Function;

public final class HologramsProvider_CMI implements HologramsProvider {

    private WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private HologramManager hologramManager = CMI.getInstance().getHologramManager();

    private Function<Location, Location> parseLocation = location -> location.add(0.5, 1.2, 0.5);

    public HologramsProvider_CMI(){
        WildStackerPlugin.log(" - Using CMI as HologramsProvider.");
    }

    @Override
    public void createHologram(StackedObject stackedObject, String line) {
        createHologram(parseLocation.apply(stackedObject.getLocation()), line);
    }

    @Override
    public void createHologram(Location location, String line) {
        CMIHologram hologram = new CMIHologram("WS-" + getLocation(location), new CMILocation(location));
        hologram.setLines(Collections.singletonList(line));
        hologram.setSaveToFile(false);
        hologramManager.addHologram(hologram);
    }

    @Override
    public void deleteHologram(StackedObject stackedObject) {
        deleteHologram(parseLocation.apply(stackedObject.getLocation()));
    }

    @Override
    public void deleteHologram(Location location) {
        if(!isHologram(location))
            return;

        CMIHologram hologram = getHologram(location);
        hologramManager.removeHolo(hologram);
    }

    @Override
    public void changeLine(StackedObject stackedObject, String newLine, boolean createIfNull) {
        changeLine(parseLocation.apply(stackedObject.getLocation()), newLine, createIfNull);
    }

    @Override
    public void changeLine(Location location, String newLine, boolean createIfNull) {
        CMIHologram hologram = getHologram(location);

        if(hologram == null) {
            if(!createIfNull)
                return;
            createHologram(location, newLine);
            return;
        }

        hologram.setLines(Collections.singletonList(newLine));
        hologram.update();
    }

    @Override
    public void clearHolograms() {
        for(CMIHologram hologram : hologramManager.getHolograms().values()){
            if(hologram.getName().startsWith("WS") && GeneralUtils.isChunkLoaded(hologram.getLocation())) {
                Block underBlock = hologram.getLocation().getBlock().getRelative(BlockFace.DOWN);
                if (!plugin.getSystemManager().isStackedSpawner(underBlock) && !plugin.getSystemManager().isStackedBarrel(underBlock)) {
                    hologramManager.removeHolo(hologram);
                    break;
                }
            }
        }
    }

    @Override
    public boolean isHologram(Location location) {
        return getHologram(location) != null;
    }

    private CMIHologram getHologram(Location location){
        return hologramManager.getByName("WS-" + getLocation(location));
    }

    private String getLocation(Location location){
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }

}
