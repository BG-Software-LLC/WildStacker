package xyz.wildseries.wildstacker.listeners.plugins;

import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.objects.WStackedSpawner;

@SuppressWarnings("unused")
public final class SilkSpawnersListener implements Listener {

    private WildStackerPlugin instance;

    public SilkSpawnersListener(WildStackerPlugin instance){
        this.instance = instance;
    }

    //This one will run only if SilkSpawners is enabled
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawnerBreak(SilkSpawnersSpawnerBreakEvent e){
        if(instance.getSettings().spawnersStackingEnabled)
            e.setDrop(instance.getProviders().getSpawnerItem(e.getSpawner(), 0));
    }

    //This one will run only if SilkSpawners is enabled
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerChange(SilkSpawnersSpawnerChangeEvent e){
        if(instance.getSettings().spawnersStackingEnabled)
            Bukkit.getScheduler().runTaskLater(instance, () -> WStackedSpawner.of(e.getSpawner()).updateName(), 2L);
    }

}
