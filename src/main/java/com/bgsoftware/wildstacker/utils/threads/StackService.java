package com.bgsoftware.wildstacker.utils.threads;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public final class StackService {

    private static final Map<String, StackServiceWorld> stackServiceWorldMap = Maps.newConcurrentMap();
    private static final Set<StackedObject> mainThreadObjects = new HashSet<>();

    public static void execute(StackedObject stackedObject, Runnable runnable){
        if(mainThreadObjects.contains(stackedObject)){
            if(Bukkit.isPrimaryThread())
                runnable.run();
            else
                Executor.sync(runnable);

            return;
        }

        execute(stackedObject.getWorld(), runnable);
    }

    public static void execute(World world, Runnable runnable){
        if(isStackThread()) {
            runnable.run();
            return;
        }

        stackServiceWorldMap.computeIfAbsent(world.getName(), stackServiceWorld -> new StackServiceWorld(world.getName())).add(runnable);
    }

    public static boolean isStackThread(){
        long threadId =  Thread.currentThread().getId();
        return stackServiceWorldMap.values().stream().anyMatch(stackServiceWorld -> stackServiceWorld.taskId == threadId);
    }

    public static boolean canStackFromThread(){
        return isStackThread() || Bukkit.isPrimaryThread();
    }

    public synchronized static void runOnMain(StackedObject stackedObject) {
        mainThreadObjects.add(stackedObject);
    }

    public synchronized static void runAsync(StackedObject stackedObject) {
        mainThreadObjects.remove(stackedObject);
    }

    public static void stop(){
        stackServiceWorldMap.values().forEach(StackServiceWorld::stop);
        stackServiceWorldMap.clear();
    }

    private static final class StackServiceWorld {

        private final List<Runnable> asyncRunnables = Collections.synchronizedList(new ArrayList<>());
        private long taskId = -1;
        private final Timer timer = new Timer(true);

        StackServiceWorld(String world){
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (taskId == -1) {
                            Thread.currentThread().setName("WildStacker Stacking Thread (" + world + ")");
                            taskId = Thread.currentThread().getId();
                        }

                        asyncRunnables.forEach(Runnable::run);
                        asyncRunnables.clear();
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }, 250, 250);
        }

        void add(Runnable runnable){
            asyncRunnables.add(runnable);
        }

        void stop(){
            timer.cancel();
            asyncRunnables.clear();
        }

    }

}
