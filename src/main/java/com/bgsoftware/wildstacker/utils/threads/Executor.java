package com.bgsoftware.wildstacker.utils.threads;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Executor {

    private static final ExecutorService dataService = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("WildStacker Database Thread #%d").build());
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

    public static void async(Runnable runnable, long delay){
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public static void data(Runnable runnable){
        dataService.execute(runnable);
    }

    public static void stop(){
        try{
            dataService.shutdown();
            dataService.awaitTermination(1, TimeUnit.MINUTES);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


}
