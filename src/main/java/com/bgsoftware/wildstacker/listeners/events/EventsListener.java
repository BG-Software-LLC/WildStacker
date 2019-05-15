package com.bgsoftware.wildstacker.listeners.events;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class EventsListener {

    public static void register(WildStackerPlugin plugin){
        try{
            Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
            plugin.getServer().getPluginManager().registerEvents(new EntityPickup(), plugin);
        }catch(Exception ex){
            try{
                Class.forName("org.bukkit.event.player.PlayerPickupItemEvent");
                plugin.getServer().getPluginManager().registerEvents(new PlayerPickup(), plugin);
            }catch(Exception ignored){}
        }
    }

    private static class EntityPickup implements Listener{

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityPickupEvent(org.bukkit.event.entity.EntityPickupItemEvent e){
            EntityPickupItemEvent entityPickupItemEvent = new EntityPickupItemEvent(e.getEntity(), e.getItem());
            Bukkit.getPluginManager().callEvent(entityPickupItemEvent);
            if(entityPickupItemEvent.isCancelled())
                e.setCancelled(true);
        }

    }

    private static class PlayerPickup implements Listener{

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityPickupEvent(org.bukkit.event.player.PlayerPickupItemEvent e){
            EntityPickupItemEvent entityPickupItemEvent = new EntityPickupItemEvent(e.getPlayer(), e.getItem());
            Bukkit.getPluginManager().callEvent(entityPickupItemEvent);
            if(entityPickupItemEvent.isCancelled())
                e.setCancelled(true);
        }

    }

}
