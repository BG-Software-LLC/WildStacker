package com.bgsoftware.wildstacker.tasks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemsMerger extends BukkitRunnable {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static BukkitTask task = null;

    private ItemsMerger(){
        if(plugin.getSettings().itemsStackingEnabled && plugin.getSettings().itemsStackInterval > 0)
            task = runTaskTimerAsynchronously(plugin, plugin.getSettings().itemsStackInterval, plugin.getSettings().itemsStackInterval);
    }

    public static void start(){
        if(task != null)
            task.cancel();

        new ItemsMerger();
    }

    @Override
    public void run() {
        if(Bukkit.getOnlinePlayers().size() > 0) {
            for(World world : Bukkit.getWorlds()){
                try {
                    Set<Item> items = ConcurrentHashMap.newKeySet();
                    items.addAll(world.getEntitiesByClass(Item.class));

                    for (Item item : items) {
                        try {
                            if (!item.isValid())
                                continue;

                            StackedItem stackedItem = WStackedItem.of(item);

                            if (!stackedItem.isWhitelisted() || stackedItem.isBlacklisted() || stackedItem.isWorldDisabled())
                                continue;

                            stackedItem.runStackAsync(null);
                        } catch (Throwable ignored) { }
                    }
                }catch(Throwable ignored){}
            }
        }
    }
}
