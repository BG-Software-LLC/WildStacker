package com.bgsoftware.wildstacker.hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_PlotSquaredLegacy implements ClaimsProvider {

    private PlotAPI API = new PlotAPI();

    @Override
    public boolean hasClaimAccess(Player player, Location location) {
        Plot plot = API.getPlot(location);
        Plot playerLocationPlot = API.getPlot(player.getLocation());
        if(plot != null && playerLocationPlot != null && !plot.getId().equals(playerLocationPlot.getId()))
            return false;
        return plot == null || player.hasPermission("plots.admin.build.other") ||
                plot.isOwner(player.getUniqueId()) || plot.isAdded(player.getUniqueId());
    }
}
