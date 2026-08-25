package com.bgsoftware.wildstacker.listeners.events;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Material;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

import javax.annotation.Nullable;

public final class EventsListener {

    private static WildStackerPlugin plugin;

    @Nullable
    private static IArmadilloScuteDropListener armadilloScuteDropListener;
    @Nullable
    private static IChickenEggLayListener chickenEggLayListener;
    @Nullable
    private static ITurtleScuteDropListener turtleScuteDropListener;

    public static void register(WildStackerPlugin plugin) {
        EventsListener.plugin = plugin;

        if (ServerVersion.isAtLeast(ServerVersion.v1_20)) {
            plugin.getServer().getPluginManager().registerEvents(new ArmadilloScuteDrop(), plugin);
        }

        plugin.getServer().getPluginManager().registerEvents(new ChickenEggLay(), plugin);

        if (ServerVersion.isAtLeast(ServerVersion.v1_13)) {
            plugin.getServer().getPluginManager().registerEvents(new TurtleScuteDrop(), plugin);
        }
    }

    public static void registerArmadilloScuteDropListener(IArmadilloScuteDropListener armadilloScuteDropListener) {
        EventsListener.armadilloScuteDropListener = armadilloScuteDropListener;
    }

    public static void registerChickenEggLayListener(IChickenEggLayListener eggLayListener) {
        EventsListener.chickenEggLayListener = eggLayListener;
    }

    public static void registerTurtleScuteDropListener(ITurtleScuteDropListener turtleScuteDropListener) {
        EventsListener.turtleScuteDropListener = turtleScuteDropListener;
    }

    private static class ArmadilloScuteDrop implements Listener {

        @Nullable
        private static final EntityType ARMADILLO = EntityUtils.getEntityTypeSafe("ARMADILLO");
        @Nullable
        private static final Material ARMADILLO_SCUTE = Materials.getMaterialOrNull("ARMADILLO_SCUTE");

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onArmadilloScuteDrop(ItemSpawnEvent e) {
            if (armadilloScuteDropListener == null || e.getEntity().getItemStack().getType() != ARMADILLO_SCUTE) {
                return;
            }

            Item armadilloScute = e.getEntity();

            EntitiesGetter.getNearbyEntities(e.getEntity().getLocation(), 2,
                            entity -> entity.getType() == ARMADILLO)
                    .findFirst().ifPresent(armadillo -> armadilloScuteDropListener.apply(armadillo, armadilloScute));
        }

    }

    private static class ChickenEggLay implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onChickenEggLay(ItemSpawnEvent e) {
            if (chickenEggLayListener == null || !Materials.isChickenEgg(e.getEntity().getItemStack())) {
                return;
            }

            Item egg = e.getEntity();

            EntitiesGetter.getNearbyEntities(e.getEntity().getLocation(), 2, entity ->
                            entity instanceof Chicken && plugin.getNMSEntities().getEggLayTime((Chicken) entity) <= 0)
                    .findFirst().ifPresent(chicken -> chickenEggLayListener.apply((Chicken) chicken, egg));
        }

    }

    private static class TurtleScuteDrop implements Listener {

        @Nullable
        private static final Material TURTLE_SCUTE = Materials.getMaterialOrNull("TURTLE_SCUTE", "SCUTE");

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onTurtleScuteDrop(ItemSpawnEvent e) {
            if (turtleScuteDropListener == null || e.getEntity().getItemStack().getType() != TURTLE_SCUTE) {
                return;
            }

            Item turtleScute = e.getEntity();

            EntitiesGetter.getNearbyEntities(e.getEntity().getLocation(), 2,
                            entity -> entity instanceof org.bukkit.entity.Turtle)
                    .findFirst().ifPresent(turtle -> turtleScuteDropListener.apply(turtle, turtleScute));
        }

    }

}
