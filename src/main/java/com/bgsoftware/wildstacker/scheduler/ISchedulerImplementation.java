package com.bgsoftware.wildstacker.scheduler;

import org.bukkit.World;
import org.bukkit.entity.Entity;

public interface ISchedulerImplementation {

    boolean isRegionScheduler();

    void cancelTasks();

    ScheduledTask scheduleTask(World world, int chunkX, int chunkZ, Runnable task, long delay);

    ScheduledTask scheduleRepeatingTask(World world, int chunkX, int chunkZ, Runnable task, long delay);

    ScheduledTask scheduleTask(Entity entity, Runnable task, long delay);

    ScheduledTask scheduleTask(Runnable task, long delay);

    ScheduledTask scheduleRepeatingTask(Runnable task, long delay);

    ScheduledTask scheduleAsyncTask(Runnable task, long delay);

    ScheduledTask scheduleRepeatingAsyncTask(Runnable task, long delay);
}
