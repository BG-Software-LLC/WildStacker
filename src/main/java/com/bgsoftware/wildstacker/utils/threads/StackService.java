package com.bgsoftware.wildstacker.utils.threads;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public final class StackService {

    private static final List<Runnable> asyncRunnables = new ArrayList<>();

    private static boolean mainThreadFlag = false;
    private static long taskId = -1;

    static {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("WildStacker Stacking Thread").build());

        executorService.submit(() -> taskId = Thread.currentThread().getId());

        executorService.scheduleAtFixedRate(() -> {
            synchronized (asyncRunnables){
                asyncRunnables.forEach(Runnable::run);
                asyncRunnables.clear();
            }
        }, 250, 250, TimeUnit.MILLISECONDS);
    }

    public static void execute(Runnable runnable){
        if(mainThreadFlag){
            if(Bukkit.isPrimaryThread())
                runnable.run();
            else
                Executor.sync(runnable);

            return;
        }

        if(isStackThread()) {
            runnable.run();
            return;
        }

        synchronized (asyncRunnables){
            asyncRunnables.add(runnable);
        }
    }

    public static boolean isStackThread(){
        return taskId == Thread.currentThread().getId();
    }

    public static boolean canStackFromThread(){
        return isStackThread() || Bukkit.isPrimaryThread();
    }

    public synchronized static void runOnMain() {
        mainThreadFlag = true;
    }

    public synchronized static void runAsync() {
        mainThreadFlag = false;
    }

}
