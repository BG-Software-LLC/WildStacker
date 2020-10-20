package com.bgsoftware.wildstacker.utils.threads;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Executor {

    private static final ExecutorService dataService = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("WildStacker Database Thread #%d").build());
    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static boolean shutdown = false;

    public static void sync(Runnable runnable){
        if(shutdown)
            return;

        if(!Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTask(plugin, runnable);
        else
            runnable.run();
    }

    public static void sync(Runnable runnable, long delayedTime){
        if(shutdown)
            return;

        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayedTime);
    }

    public static void async(Runnable runnable){
        if(shutdown)
            return;

        if(!Bukkit.isPrimaryThread())
            runnable.run();
        else
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static void async(Runnable runnable, long delay){
        if(shutdown)
            return;

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public static BukkitTask timer(Runnable runnable, long period){
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, 0L, period);
    }

    public static void data(Runnable runnable){
        if(shutdown)
            return;

        dataService.execute(runnable);
    }

    public static void stop(){
        try{
            shutdown = true;
            dataService.shutdown();
            dataService.awaitTermination(1, TimeUnit.MINUTES);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
