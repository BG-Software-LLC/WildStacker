package com.bgsoftware.wildstacker.utils.threads;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class DatabaseThread {

    private static final ExecutorService dataService = Executors.newFixedThreadPool(3,
            new ThreadFactoryBuilder().setNameFormat("WildStacker Database Thread #%d").build());

    private static boolean dataShutdown = false;

    public static boolean isDataThread() {
        return Thread.currentThread().getName().contains("WildStacker Database Thread");
    }

    public static void data(Runnable runnable) {
        if (dataShutdown)
            return;

        dataService.execute(runnable);
    }

    public static void stopData() {
        try {
            dataShutdown = true;
            WildStackerPlugin.log("Shutting down database executor");
            shutdownAndAwaitTermination();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void shutdownAndAwaitTermination() {
        dataService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!dataService.awaitTermination(60, TimeUnit.SECONDS)) {
                dataService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!dataService.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            dataService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
