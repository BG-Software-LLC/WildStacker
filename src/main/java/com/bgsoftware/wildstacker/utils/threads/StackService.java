package com.bgsoftware.wildstacker.utils.threads;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public final class StackService {

    private static final Set<Runnable> asyncRunnables = Sets.newConcurrentHashSet();

    private static boolean mainThreadFlag = false;
    private static long taskId = -1;

    static {
        final Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(taskId == -1){
                    Thread.currentThread().setName("WildStacker Stacking Thread");
                    taskId = Thread.currentThread().getId();
                }

                asyncRunnables.forEach(Runnable::run);
                asyncRunnables.clear();
            }
        }, 250, 250);
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

        asyncRunnables.add(runnable);
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
