package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;

import java.util.concurrent.Executors;

public final class Executor {

    private static final java.util.concurrent.Executor executor = Executors.newFixedThreadPool(8, new ThreadFactoryBuilder().setNameFormat("WildStacker Thread ").build());
    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static void sync(Runnable runnable){
        if(!Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTask(plugin, runnable);
        else
            runnable.run();
    }

    public static void sync(Runnable runnable, long delayedTime){
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayedTime);
    }

    public static void async(Runnable runnable){
        if(!Bukkit.isPrimaryThread())
            runnable.run();
        else
            executor.execute(runnable);
    }

}
