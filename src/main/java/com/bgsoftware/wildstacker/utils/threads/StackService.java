package com.bgsoftware.wildstacker.utils.threads;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public final class StackService {

    private static final ExecutorService stackService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("WildStacker Stack Thread").build());
    private static long threadId = -1;

    static {
        stackService.execute(() -> threadId = Thread.currentThread().getId());
    }

    public static void execute(Runnable runnable){
        if(isStackThread())
            runnable.run();
        else
            stackService.execute(runnable);
    }

    public static boolean isStackThread(){
        return Thread.currentThread().getId() == threadId;
    }

    public static boolean canStackFromThread(){
        return isStackThread() || Bukkit.isPrimaryThread();
    }

    public static void stop(){
        try{
            stackService.shutdown();
            stackService.awaitTermination(1, TimeUnit.MINUTES);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
