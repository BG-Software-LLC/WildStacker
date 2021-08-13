package com.bgsoftware.wildstacker.listeners.events;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

@SuppressWarnings("unused")
public final class EventsListener {

    private static WildStackerPlugin plugin;

    public static void register(WildStackerPlugin plugin) {
        EventsListener.plugin = plugin;
        boolean registeredPlayerPickup = false;

        try {
            Class.forName("org.bukkit.event.player.PlayerAttemptPickupItemEvent");
            plugin.getServer().getPluginManager().registerEvents(new PlayerAttemptPickup(), plugin);
            registeredPlayerPickup = true;
        } catch (Exception ignored) {}

        try {
            Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
            plugin.getServer().getPluginManager().registerEvents(new EntityPickup(!registeredPlayerPickup), plugin);
        } catch (Exception ex) {
            try {
                Class.forName("org.bukkit.event.player.PlayerPickupItemEvent");
                plugin.getServer().getPluginManager().registerEvents(new PlayerPickup(), plugin);
            } catch (Exception ignored) {
            }
        }

        plugin.getServer().getPluginManager().registerEvents(new EggLay(), plugin);
        if (ServerVersion.isAtLeast(ServerVersion.v1_13))
            plugin.getServer().getPluginManager().registerEvents(new ScuteDrop(), plugin);
    }

    private static class PlayerAttemptPickup implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityPickupEvent(org.bukkit.event.player.PlayerAttemptPickupItemEvent e) {
            if (!ItemUtils.isStackable(e.getItem()))
                return;

            EntityPickupItemEvent entityPickupItemEvent = new EntityPickupItemEvent(e.getPlayer(), WStackedItem.of(e.getItem()));
            Bukkit.getPluginManager().callEvent(entityPickupItemEvent);
            if (entityPickupItemEvent.isCancelled()) {
                e.setCancelled(true);
                e.setFlyAtPlayer(false);
            }
        }

    }

    private static class EntityPickup implements Listener {

        private final boolean listenToPlayersPickup;

        private EntityPickup(boolean listenToPlayersPickup){
            this.listenToPlayersPickup = listenToPlayersPickup;
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityPickupEvent(org.bukkit.event.entity.EntityPickupItemEvent e) {
            if(!listenToPlayersPickup && e.getEntity() instanceof Player)
                return;

            if (!ItemUtils.isStackable(e.getItem()))
                return;

            EntityPickupItemEvent entityPickupItemEvent = new EntityPickupItemEvent(e.getEntity(), WStackedItem.of(e.getItem()));
            Bukkit.getPluginManager().callEvent(entityPickupItemEvent);
            if (entityPickupItemEvent.isCancelled())
                e.setCancelled(true);
        }

    }

    private static class PlayerPickup implements Listener {

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityPickupEvent(org.bukkit.event.player.PlayerPickupItemEvent e) {
            if (!ItemUtils.isStackable(e.getItem()))
                return;

            EntityPickupItemEvent entityPickupItemEvent = new EntityPickupItemEvent(e.getPlayer(), WStackedItem.of(e.getItem()));
            Bukkit.getPluginManager().callEvent(entityPickupItemEvent);
            if (entityPickupItemEvent.isCancelled())
                e.setCancelled(true);
        }

    }

    private static class EggLay implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onEggLay(ItemSpawnEvent e) {
            if (e.getEntity().getItemStack().getType() != Material.EGG)
                return;

            Item egg = e.getEntity();

            EntitiesGetter.getNearbyEntities(e.getEntity().getLocation(), 2, entity ->
                            entity instanceof Chicken && plugin.getNMSAdapter().getEggLayTime((Chicken) entity) <= 0)
                    .findFirst().ifPresent(chicken -> {
                        EggLayEvent eggLayEvent = new EggLayEvent(egg, (Chicken) chicken);
                        Bukkit.getPluginManager().callEvent(eggLayEvent);
                    });
        }

    }

    private static class ScuteDrop implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onScuteDrop(ItemSpawnEvent e) {
            if (!e.getEntity().getItemStack().getType().name().equals("SCUTE"))
                return;

            Item scute = e.getEntity();

            EntitiesGetter.getNearbyEntities(e.getEntity().getLocation(), 2,
                            entity -> entity instanceof org.bukkit.entity.Turtle)
                    .findFirst().ifPresent(turtle -> {
                        ScuteDropEvent scuteDropEvent = new ScuteDropEvent(scute, (Turtle) turtle);
                        Bukkit.getPluginManager().callEvent(scuteDropEvent);
                    });
        }

    }

}
