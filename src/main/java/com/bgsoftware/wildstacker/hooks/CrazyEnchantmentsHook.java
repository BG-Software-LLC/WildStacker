package com.bgsoftware.wildstacker.hooks;

import me.badbones69.crazyenchantments.Main;
import me.badbones69.crazyenchantments.enchantments.Axes;
import me.badbones69.crazyenchantments.enchantments.Bows;
import me.badbones69.crazyenchantments.enchantments.Swords;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrazyEnchantmentsHook {

    private static Swords swordsListener;
    private static Bows bowsListener;
    private static Axes axesListener;

    public static void register() {
        Main crazyMain = JavaPlugin.getPlugin(Main.class);
        for (RegisteredListener registeredListener : EntityDamageByEntityEvent.getHandlerList().getRegisteredListeners()) {
            if (registeredListener.getPlugin().equals(crazyMain)) {
                EntityDamageByEntityEvent.getHandlerList().unregister(registeredListener);
                if (registeredListener.getListener() instanceof Swords)
                    swordsListener = (Swords) registeredListener.getListener();
                if (registeredListener.getListener() instanceof Bows)
                    bowsListener = (Bows) registeredListener.getListener();
                if (registeredListener.getListener() instanceof Axes)
                    axesListener = (Axes) registeredListener.getListener();
            }
        }

        crazyMain.getServer().getPluginManager().registerEvents(new MySwordsListener(), crazyMain);
        crazyMain.getServer().getPluginManager().registerEvents(new MyBowsListener(), crazyMain);
        crazyMain.getServer().getPluginManager().registerEvents(new MyAxesListener(), crazyMain);
    }

    private static class MySwordsListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerDamage(EntityDamageByEntityEvent e) {
            swordsListener.onPlayerDamage(e);
        }

    }

    private static class MyBowsListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerDamage(EntityDamageByEntityEvent e) {
            bowsListener.onArrowDamage(e);
        }

    }

    private static class MyAxesListener implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerDamage(EntityDamageByEntityEvent e) {
            axesListener.onPlayerDamage(e);
        }

    }


}
