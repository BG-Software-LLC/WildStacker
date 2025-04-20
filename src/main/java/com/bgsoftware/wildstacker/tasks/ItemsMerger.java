package com.bgsoftware.wildstacker.tasks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class ItemsMerger extends BukkitRunnable {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static BukkitTask task = null;

    private ItemsMerger() {
        if (plugin.getSettings().getItems().isEnabled() && plugin.getSettings().getItems().getStackInterval() > 0)
            task = runTaskTimer(plugin, plugin.getSettings().getItems().getStackInterval(), plugin.getSettings().getItems().getStackInterval());
    }

    public static void start() {
        if (task != null)
            task.cancel();

        new ItemsMerger();
    }

    @Override
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
