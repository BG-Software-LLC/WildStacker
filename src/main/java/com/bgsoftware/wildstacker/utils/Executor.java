package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;

import java.util.concurrent.Executors;

public final class Executor {

    private static final java.util.concurrent.Executor executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("WildStacker Thread").build());
    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static Thread thread;

    static {
        executor.execute(() -> thread = Thread.currentThread());
    }

    public static void sync(Runnable runnable){
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void sync(Runnable runnable, long delayedTime){
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayedTime);
    }

    public static void async(Runnable runnable){
        if(Thread.currentThread().getId() == thread.getId())
            runnable.run();
        else
            executor.execute(runnable);
    }

}
