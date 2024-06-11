package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public final class SilkSpawnersHook {

    private static WildStackerPlugin plugin;

    public static void register(WildStackerPlugin plugin) {
        SilkSpawnersHook.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new SilkSpawnersListener(), plugin);
        if(plugin.getSettings().spawnersStackingEnabled)
            BlockBreakEvent.getHandlerList().unregister(JavaPlugin.getPlugin(SilkSpawners.class));
    }

    private static final class SilkSpawnersListener implements Listener {

        //This one will run only if SilkSpawners is enabled
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onSpawnerBreak(SilkSpawnersSpawnerBreakEvent e) {
            if (plugin.getSettings().spawnersStackingEnabled) {
                e.setDrop(WStackedSpawner.of(e.getSpawner()).getDropItem(0));
            }
        }

        //This one will run only if SilkSpawners is enabled
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawnerChange(SilkSpawnersSpawnerChangeEvent e) {
            if (plugin.getSettings().spawnersStackingEnabled)
                Scheduler.runTask(e.getSpawner().getLocation(), () ->
                        WStackedSpawner.of(e.getSpawner()).updateName(), 2L);
        }

    }

}
