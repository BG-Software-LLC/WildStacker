package com.bgsoftware.wildstacker.listeners.events;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.stacker.items.WStackedItem;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public final class EventsListener {

    private static WildStackerPlugin plugin;

    @Nullable
    private static IEggLayListener eggLayListener;
    @Nullable
    private static IScuteDropListener scuteDropListener;
    private static final List<Pair<IEntityPickupListener, EventPriority>> entityPickupListeners = new LinkedList<>();

    public static void register(WildStackerPlugin plugin) {
        EventsListener.plugin = plugin;
        boolean registeredPlayerPickup = false;

        try {
            Class.forName("org.bukkit.event.player.PlayerAttemptPickupItemEvent");
            plugin.getServer().getPluginManager().registerEvents(new PlayerAttemptPickup(), plugin);
            registeredPlayerPickup = true;
        } catch (Exception ignored) {
        }

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

    public static void registerEggLayListener(IEggLayListener eggLayListener) {
        EventsListener.eggLayListener = eggLayListener;
    }

    public static void registerScuteDropListener(IScuteDropListener scuteDropListener) {
        EventsListener.scuteDropListener = scuteDropListener;
    }

    public static void addEntityPickupListener(IEntityPickupListener entityPickupListener, EventPriority eventPriority) {
        entityPickupListeners.add(new Pair<>(entityPickupListener, eventPriority));
        entityPickupListeners.sort(Comparator.comparingInt(o -> o.getValue().getSlot()));
    }

    private static boolean notifyEntityPickupListeners(Cancellable event, StackedItem stackedItem,
                                                       LivingEntity livingEntity, int remaining) {
        for (Pair<IEntityPickupListener, EventPriority> pair : entityPickupListeners) {
            if (pair.getKey().apply(event, stackedItem, livingEntity, remaining))
                return true;
        }

        return false;
    }

    private static class PlayerAttemptPickup implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onPlayerAttemptPickupItem(org.bukkit.event.player.PlayerAttemptPickupItemEvent e) {
            if (entityPickupListeners.isEmpty() || e.getRemaining() >= e.getItem().getItemStack().getAmount()
                    || !ItemUtils.isStackable(e.getItem()))
                return;

            org.bukkit.event.entity.EntityPickupItemEvent bukkitEntityPickupItemEvent =
                    new org.bukkit.event.entity.EntityPickupItemEvent(e.getPlayer(), e.getItem(), e.getRemaining());
            Bukkit.getPluginManager().callEvent(bukkitEntityPickupItemEvent);

            if (bukkitEntityPickupItemEvent.isCancelled() ||
                    notifyEntityPickupListeners(e, WStackedItem.of(e.getItem()), e.getPlayer(), 0)) {
                e.setCancelled(true);
                e.setFlyAtPlayer(false);
            }
        }

    }

    private static class EntityPickup implements Listener {

        private final boolean listenToPlayersPickup;

        private EntityPickup(boolean listenToPlayersPickup) {
            this.listenToPlayersPickup = listenToPlayersPickup;
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onEntityPickupItem(org.bukkit.event.entity.EntityPickupItemEvent e) {
            if (entityPickupListeners.isEmpty() || (!listenToPlayersPickup && e.getEntity() instanceof Player))
                return;

            if (ItemUtils.isStackable(e.getItem()) && notifyEntityPickupListeners(e, WStackedItem.of(e.getItem()),
                    e.getEntity(), e.getRemaining()))
                e.setCancelled(true);
        }

    }

    private static class PlayerPickup implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onPlayerPickupItem(org.bukkit.event.player.PlayerPickupItemEvent e) {
            if (entityPickupListeners.isEmpty() || !ItemUtils.isStackable(e.getItem()))
                return;

            if (notifyEntityPickupListeners(e, WStackedItem.of(e.getItem()), e.getPlayer(), e.getRemaining()))
                e.setCancelled(true);
        }

    }

    private static class EggLay implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onEggLay(ItemSpawnEvent e) {
            if (eggLayListener == null || e.getEntity().getItemStack().getType() != Material.EGG)
                return;

            Item egg = e.getEntity();

            EntitiesGetter.getNearbyEntities(e.getEntity().getLocation(), 2, entity ->
                    entity instanceof Chicken && plugin.getNMSEntities().getEggLayTime((Chicken) entity) <= 0
            ).findFirst().ifPresent(chicken -> eggLayListener.apply((Chicken) chicken, egg));
        }

    }

    private static class ScuteDrop implements Listener {

        @Nullable
        private static final Material SCUTE = ((Supplier<Material>) () -> {
            try {
                return Material.valueOf("SCUTE");
            } catch (IllegalArgumentException error) {
                return null;
            }
        }).get();

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onScuteDrop(ItemSpawnEvent e) {
            if (scuteDropListener == null || e.getEntity().getItemStack().getType() != SCUTE)
                return;

            Item scute = e.getEntity();

            EntitiesGetter.getNearbyEntities(e.getEntity().getLocation(), 2,
                            entity -> entity instanceof org.bukkit.entity.Turtle)
                    .findFirst().ifPresent(turtle -> scuteDropListener.apply(turtle, scute));
        }

    }

}
