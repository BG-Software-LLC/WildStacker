package com.bgsoftware.wildstacker.listeners.events;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public final class EventsListener {

    private static WildStackerPlugin plugin;

    @Nullable
    private static IEggLayListener eggLayListener;
    @Nullable
    private static IScuteDropListener scuteDropListener;

    public static void register(WildStackerPlugin plugin) {
        EventsListener.plugin = plugin;

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

    private static class EggLay implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onEggLay(ItemSpawnEvent e) {
            if (eggLayListener == null || !Materials.isChickenEgg(e.getEntity().getItemStack()))
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
