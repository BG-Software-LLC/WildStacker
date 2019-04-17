package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@SuppressWarnings("unused")
public final class NoClaimConflictListener implements Listener {

    private final WildStackerPlugin plugin;
    private final Map<Location, UUID> placers = new WeakHashMap<>();

    public NoClaimConflictListener(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e){
        if(plugin.getSettings().spawnersStackingEnabled && e.getBlockPlaced().getType() == Materials.SPAWNER.toBukkitType()) {
            placers.put(e.getBlockPlaced().getLocation(), e.getPlayer().getUniqueId());
        }
        else if(plugin.getSettings().barrelsStackingEnabled && plugin.getSettings().whitelistedBarrels.contains(e.getItemInHand())){
            placers.put(e.getBlockPlaced().getLocation(), e.getPlayer().getUniqueId());
        }
        Executor.sync(() -> placers.remove(e.getBlockPlaced().getLocation()), 2L);
    }

    @EventHandler
    public void onSpawnerStack(SpawnerStackEvent e){
        if(placers.containsKey(e.getTarget().getLocation())){
            Player placer = Bukkit.getPlayer(placers.get(e.getTarget().getLocation()));
            if(placer == null)
                return;
            if(!plugin.getProviders().hasClaimAccess(placer, e.getSpawner().getLocation()))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBarrelStack(BarrelStackEvent e){
        if(placers.containsKey(e.getTarget().getLocation())){
            Player placer = Bukkit.getPlayer(placers.get(e.getTarget().getLocation()));
            if(placer == null)
                return;
            if(!plugin.getProviders().hasClaimAccess(placer, e.getBarrel().getLocation()))
                e.setCancelled(true);
        }
    }

}
