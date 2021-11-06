package com.bgsoftware.wildstacker.hooks;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import org.bukkit.entity.Player;

public final class ClaimsProvider_PlotSquaredV6 implements ClaimsProvider {

    private final PlotSquared instance = PlotSquared.get();

    @Override
    public boolean hasClaimAccess(Player player, org.bukkit.Location location) {
        Plot plot = getPlot(location);
        Plot playerLocationPlot = getPlot(player.getLocation());
        if (plot != null && playerLocationPlot != null && !plot.getId().equals(playerLocationPlot.getId()))
            return false;
        return plot == null || player.hasPermission("plots.admin.build.other") ||
                plot.isOwner(player.getUniqueId()) || plot.isAdded(player.getUniqueId());
    }

    private Plot getPlot(org.bukkit.Location location) {
        Location plotLocation = BukkitUtil.adaptComplete(location);
        PlotArea plotArea = instance.getPlotAreaManager().getPlotArea(plotLocation);
        return plotArea == null ? null : plotArea.getPlot(plotLocation);
    }

}
