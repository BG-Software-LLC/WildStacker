package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class SilkSpawnersListener implements Listener {

    private final WildStackerPlugin plugin;

    public SilkSpawnersListener(WildStackerPlugin plugin){
        this.plugin = plugin;
        PluginHooks.isSilkSpawnersEnabled = true;
    }

    //This one will run only if SilkSpawners is enabled
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnerBreak(SilkSpawnersSpawnerBreakEvent e){
        if(plugin.getSettings().spawnersStackingEnabled) {
            e.setDrop(WStackedSpawner.of(e.getSpawner()).getDropItem(0));
        }
    }

    //This one will run only if SilkSpawners is enabled
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerChange(SilkSpawnersSpawnerChangeEvent e){
        if(plugin.getSettings().spawnersStackingEnabled)
            Executor.sync(() -> WStackedSpawner.of(e.getSpawner()).updateName(), 2L);
    }

}
