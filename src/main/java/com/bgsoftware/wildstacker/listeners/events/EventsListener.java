package com.bgsoftware.wildstacker.listeners.events;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

@SuppressWarnings("unused")
public final class EventsListener {

    private static WildStackerPlugin plugin;

    public static void register(WildStackerPlugin plugin){
        EventsListener.plugin = plugin;
        try{
            Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
            plugin.getServer().getPluginManager().registerEvents(new EntityPickup(), plugin);
        }catch(Exception ex){
            try{
                Class.forName("org.bukkit.event.player.PlayerPickupItemEvent");
                plugin.getServer().getPluginManager().registerEvents(new PlayerPickup(), plugin);
            }catch(Exception ignored){}
        }
        plugin.getServer().getPluginManager().registerEvents(new EggLay(), plugin);
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

    private static class EggLay implements Listener{

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onEggLay(ItemSpawnEvent e){
            if(e.getEntity().getItemStack().getType() != Material.EGG)
                return;

            Item egg = e.getEntity();

            for(Entity entity : e.getEntity().getNearbyEntities(2, 2, 2)){
                if(entity instanceof Chicken){
                    if(plugin.getNMSAdapter().getEggLayTime((Chicken) entity) <= 0){
                        EggLayEvent eggLayEvent = new EggLayEvent(egg, (Chicken) entity);
                        Bukkit.getPluginManager().callEvent(eggLayEvent);
                        break;
                    }
                }
            }
        }

    }

}
