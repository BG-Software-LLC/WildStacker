package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.sainttx.holograms.HologramPlugin;
import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.line.HologramLine;
import com.sainttx.holograms.api.line.TextLine;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class HologramsProvider_Holograms implements HologramsProvider {

    private WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private HologramManager hologramManager;

    public HologramsProvider_Holograms(){
        WildStackerPlugin.log(" - Using Holograms as HologramsProvider.");
        hologramManager =  JavaPlugin.getPlugin(HologramPlugin.class).getHologramManager();
    }

    @Override
    public void createHologram(StackedObject stackedObject, String line) {
        createHologram(stackedObject.getLocation().add(0.5, 1, 0.5), line);
    }

    @Override
    public void createHologram(Location location, String line) {
        Hologram hologram = new Hologram("WS-" + location.toString(), location);
        hologramManager.addActiveHologram(hologram);
        HologramLine hologramLine = new TextLine(hologram, line);
        hologram.addLine(hologramLine);
    }

    @Override
    public void deleteHologram(StackedObject stackedObject) {
        deleteHologram(stackedObject.getLocation().add(0.5, 1, 0.5));
    }

    @Override
    public void deleteHologram(Location location) {
        Hologram hologram = getHologram(location);

        if(hologram == null)
            return;

        hologramManager.deleteHologram(hologram);
    }

    @Override
    public void changeLine(StackedObject stackedObject, String newLine, boolean createIfNull) {
        changeLine(stackedObject.getLocation().add(0.5, 1, 0.5), newLine, createIfNull);
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

        new ArrayList<>(hologram.getLines()).forEach(hologram::removeLine);
        HologramLine hologramLine = new TextLine(hologram, newLine);
        hologram.addLine(hologramLine);
    }

    @Override
    public void clearHolograms() {
        for(Hologram hologram : hologramManager.getActiveHolograms().values()){
            if(hologram.getId().startsWith("WS") && GeneralUtils.isChunkLoaded(hologram.getLocation())) {
                Block underBlock = hologram.getLocation().getBlock().getRelative(BlockFace.DOWN);
                if (!plugin.getSystemManager().isStackedSpawner(underBlock) && !plugin.getSystemManager().isStackedBarrel(underBlock)) {
                    hologramManager.deleteHologram(hologram);
                    break;
                }
            }
        }
    }

    @Override
    public boolean isHologram(Location location) {
        return getHologram(location) != null;
    }

    private Hologram getHologram(Location location){
        for(Hologram hologram : hologramManager.getActiveHolograms().values()){
            if(hologram.getId().startsWith("WS-") && hologram.getLocation().equals(location)) {
                return hologram;
            }
        }

        return null;
    }
}
