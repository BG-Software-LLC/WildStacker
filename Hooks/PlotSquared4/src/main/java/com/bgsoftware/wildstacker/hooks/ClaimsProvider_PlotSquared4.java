package com.bgsoftware.wildstacker.hooks;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.sk89q.worldedit.math.BlockVector2;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_PlotSquared4 implements ClaimsProvider {

    private final PlotAPI API = new PlotAPI();

    @Override
    public boolean hasClaimAccess(Player player, Location location) {
        BlockVector2 chunkPosition = BlockVector2.at(location.getBlockX() >> 4, location.getBlockZ() >> 4);

        Location playerLocation = player.getLocation();
        BlockVector2 playerChunkPosition = BlockVector2.at(playerLocation.getBlockX() >> 4,
                playerLocation.getBlockZ() >> 4);

        Plot plot = API.getChunkManager().hasPlot(location.getWorld().getName(), chunkPosition);
        Plot playerLocationPlot = API.getChunkManager().hasPlot(player.getWorld().getName(), playerChunkPosition);

        if (plot != null && playerLocationPlot != null && !plot.getId().equals(playerLocationPlot.getId()))
            return false;

        return plot == null || player.hasPermission("plots.admin.build.other") ||
                plot.isOwner(player.getUniqueId()) || plot.isAdded(player.getUniqueId());
    }

}
