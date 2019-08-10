package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.utils.Executor;
import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.plotsquared.bukkit.events.PlotClearEvent;
import com.plotsquared.bukkit.events.PlotDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class ClaimsProvider_PlotSquaredLegacy implements ClaimsProvider {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private PlotAPI API = new PlotAPI();

    public ClaimsProvider_PlotSquaredLegacy(){
        Bukkit.getPluginManager().registerEvents(new ClaimsProvider_PlotSquaredLegacy.PlotsListener(), plugin);
    }

    @Override
    public boolean hasClaimAccess(Player player, Location location) {
        Plot plot = API.getPlot(location);
        Plot playerLocationPlot = API.getPlot(player.getLocation());
        if(plot != null && playerLocationPlot != null && !plot.getId().equals(playerLocationPlot.getId()))
            return false;
        return plot == null || player.hasPermission("plots.admin.build.other") ||
                plot.isOwner(player.getUniqueId()) || plot.isAdded(player.getUniqueId());
    }

    @SuppressWarnings("unused")
    private class PlotsListener implements Listener {

        @EventHandler
        public void onPlotDelete(PlotDeleteEvent e) {
            PlotId plotId = e.getPlotId();

            Executor.async(() -> {
                for (StackedSpawner stackedSpawner : plugin.getSystemManager().getStackedSpawners()){
                    if(insidePlot(plotId, stackedSpawner.getLocation()))
                        stackedSpawner.remove();
                }
                for(StackedBarrel stackedBarrel : plugin.getSystemManager().getStackedBarrels()){
                    if(insidePlot(plotId, stackedBarrel.getLocation()))
                        stackedBarrel.remove();
                }
            });
        }

        @EventHandler
        public void onPlotClear(PlotClearEvent e){
            PlotId plotId = e.getPlotId();

            Executor.async(() -> {
                for (StackedSpawner stackedSpawner : plugin.getSystemManager().getStackedSpawners()){
                    if(insidePlot(plotId, stackedSpawner.getLocation()))
                        stackedSpawner.remove();
                }
                for(StackedBarrel stackedBarrel : plugin.getSystemManager().getStackedBarrels()){
                    if(insidePlot(plotId, stackedBarrel.getLocation()))
                        stackedBarrel.remove();
                }
            });
        }

        private boolean insidePlot(PlotId plotId, Location location){
            Plot plot = API.getChunkManager().hasPlot(location.getWorld().getName(),
                    new ChunkLoc(location.getChunk().getX(), location.getChunk().getZ()));
            return plot != null && plot.getId().equals(plotId);
        }

    }

}
