package com.bgsoftware.wildstacker.hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_PlotSquared implements ClaimsProvider {

    private PlotAPI API = new PlotAPI();

    @Override
    public boolean hasClaimAccess(Player player, Location location) {
        Plot plot = API.getPlot(location);
        PlotPlayer plotPlayer = PlotPlayer.wrap(player);
        return plot == null || player.hasPermission("plots.admin.build.other") || plotPlayer.getPlots().contains(plot);
    }
}
