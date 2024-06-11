package com.bgsoftware.wildstacker.tasks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.scheduler.ScheduledTask;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Item;

public final class ItemsMerger {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static ScheduledTask task = null;

    private ItemsMerger() {
        if (plugin.getSettings().itemsStackingEnabled && plugin.getSettings().itemsStackInterval > 0)
            task = Scheduler.runRepeatingTask(this::run, plugin.getSettings().itemsStackInterval);
        else
            task = null;
    }

    public static void start() {
        if (task != null)
            task.cancel();

        new ItemsMerger();
    }

    public void run() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (World world : Bukkit.getWorlds()) {
                try {
                    for (Item item : world.getEntitiesByClass(Item.class)) {
                        try {
                            if (!ItemUtils.isStackable(item))
                                continue;

                            StackedItem stackedItem = WStackedItem.of(item);

                            if (!stackedItem.isCached())
                                continue;

                            stackedItem.runStackAsync(null);
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
