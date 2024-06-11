package com.bgsoftware.wildstacker.tasks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.scheduler.ScheduledTask;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class StackTask {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static ScheduledTask task;

    private StackTask() {
        if (plugin.getSettings().entitiesStackingEnabled && plugin.getSettings().entitiesStackInterval > 0)
            task = Scheduler.runRepeatingTask(this::run, plugin.getSettings().entitiesStackInterval);
        else
            task = null;
    }

    public static void start() {
        if (task != null)
            task.cancel();

        new StackTask();
    }

    public void run() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (World world : Bukkit.getWorlds()) {
                try {
                    Set<LivingEntity> livingEntities = ConcurrentHashMap.newKeySet();
                    livingEntities.addAll(world.getLivingEntities());

                    for (LivingEntity livingEntity : livingEntities) {
                        try {
                            if (!EntityUtils.isStackable(livingEntity))
                                continue;

                            StackedEntity stackedEntity = WStackedEntity.of(livingEntity);

                            if (!stackedEntity.isCached())
                                continue;

                            stackedEntity.runStackAsync(null);
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
