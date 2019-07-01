package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.Bukkit;

public final class Executor {

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
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

}
