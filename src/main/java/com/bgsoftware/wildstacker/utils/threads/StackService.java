package com.bgsoftware.wildstacker.utils.threads;

import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.Bukkit;

@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public final class StackService {

    private static int threadId = 1;
    private static boolean mainThreadFlag;

    public static void execute(Runnable runnable){
        if(mainThreadFlag){
            if(Bukkit.isPrimaryThread())
                runnable.run();
            else
                Executor.sync(runnable);

            mainThreadFlag = false;
        }

        if(isStackThread()) {
            runnable.run();
            return;
        }

        Executor.async(() -> {
            String threadName = Thread.currentThread().getName();
            Thread.currentThread().setName("WildStacker Stack Thread #" + threadId++);
            runnable.run();
            Thread.currentThread().setName(threadName);
        });
    }

    public static boolean isStackThread(){
        return Thread.currentThread().getName().startsWith("WildStacker Stack Thread");
    }

    public static boolean canStackFromThread(){
        return isStackThread() || Bukkit.isPrimaryThread();
    }

    public static void runOnMain() {
        mainThreadFlag = true;
    }

}
