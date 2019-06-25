package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Executor {

    private static final java.util.concurrent.Executor executor = Executors.newFixedThreadPool(8, new ThreadFactoryBuilder().setNameFormat("WildStacker Thread %d").build());
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

    public static void stop(){
        try{
            ((ExecutorService) executor).shutdown();
            ((ExecutorService) executor).awaitTermination(10, TimeUnit.MINUTES);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
