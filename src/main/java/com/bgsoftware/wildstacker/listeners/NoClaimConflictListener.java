package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        else if(plugin.getSettings().barrelsStackingEnabled &&
                plugin.getSystemManager().isBarrelBlock(e.getItemInHand().getType(), e.getPlayer().getWorld())){
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

    @EventHandler
    @SuppressWarnings("all")
    public void onInventoryOpen(InventoryOpenEvent e){
        if(e.getInventory().getHolder() != null &&
                e.getInventory().getHolder().getClass().toString().contains("ultimatestacker")){
            try{
                Class holderClass = e.getInventory().getHolder().getClass();
                Method guiMethod = holderClass.getMethod("getGUI");
                Object gui = guiMethod.invoke(e.getInventory().getHolder());
                Class guiClass = gui.getClass().getSuperclass();
                Field clickableFields = guiClass.getDeclaredField("clickables");
                clickableFields.setAccessible(true);
                ((Map) clickableFields.get(gui)).clear();
                clickableFields.setAccessible(false);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

}
