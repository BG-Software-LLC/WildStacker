package com.bgsoftware.wildstacker.utils.threads;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.utils.Executor;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public final class StackService {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final Map<Location, ServiceElement> services = new ConcurrentHashMap<>();
    private static int threadId = 1;
    private static boolean mainThreadFlag = false;

    public static void execute(StackedObject stackedObject, Runnable runnable){
        if(isStackThread() || mainThreadFlag) {
            mainThreadFlag = false;
            runnable.run();
        }
        else {
            findExecutor(stackedObject, executorService -> executorService.execute(runnable));
        }
    }

    public static Future<Boolean> submit(StackedObject stackedObject, Callable<Boolean> callable){
        return findExecutor(stackedObject).submit(callable);
    }

    private static void findExecutor(StackedObject stackedObject, Consumer<ExecutorService> executorServiceConsumer){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> findExecutor(stackedObject, executorServiceConsumer));
            return;
        }

        executorServiceConsumer.accept(findExecutor(stackedObject));
    }

    private static ExecutorService findExecutor(StackedObject stackedObject){
        Location location = stackedObject.getLocation();
        int stackCheck = 0;

        if(stackedObject instanceof StackedItem)
            stackCheck = plugin.getSettings().itemsCheckRange;
        else if(stackedObject instanceof StackedEntity)
            stackCheck = plugin.getSettings().entitiesCheckRange;
        else if(stackedObject instanceof StackedSpawner)
            stackCheck = plugin.getSettings().spawnersCheckRange;
        else if(stackedObject instanceof StackedBarrel)
            stackCheck = plugin.getSettings().barrelsCheckRange;

        return getClosest(location, stackCheck);
    }

    public static ExecutorService getClosest(Location location, int stackCheck){
        Optional<Location> optionalLocation = services.keySet().stream().min((o1, o2) -> {
            double distance1 = distance(o1, location), distance2 = distance(o2, location);
            return Double.compare(distance1, distance2);
        });

        ServiceElement serviceElement;

        if(!optionalLocation.isPresent() || !isInsideRange(optionalLocation.get(), location, stackCheck)) {
            serviceElement = new ServiceElement();
            services.put(location, serviceElement);
        }
        else{
            serviceElement = services.get(optionalLocation.get());
        }

        serviceElement.lastUse = System.currentTimeMillis() / 1000;

        return serviceElement.executorService;
    }

    public static boolean isStackThread(){
        return Thread.currentThread().getName().startsWith("WildStacker Stack Thread");
    }

    public static boolean canStackFromThread(){
        return isStackThread() || Bukkit.isPrimaryThread();
    }

    public static void clearCache(){
        for(Map.Entry<Location, ServiceElement> entry : services.entrySet()){
            if((System.currentTimeMillis() / 1000) - entry.getValue().lastUse >= 30)
                services.remove(entry.getKey());
        }
    }

    public static void stop(){
        try{
            for(ServiceElement serviceElement : services.values())
                serviceElement.executorService.shutdownNow();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void runOnMain(){
        mainThreadFlag = true;
    }

    private static boolean isInsideRange(Location location, Location target, int range){
        if(!location.getWorld().getName().equals(target.getWorld().getName()))
            return false;

        Location max = location.clone().add(range, range, range), min = location.clone().subtract(range, range, range);
        return target.getX() >= min.getX() && target.getX() <= max.getX() &&
                target.getY() >= min.getY() && target.getY() <= max.getY() &&
                target.getZ() >= min.getZ() && target.getZ() <= max.getZ();
    }

    private static double distance(Location loc1, Location loc2){
        return loc1.getWorld().getName().equals(loc2.getWorld().getName()) ? loc1.distanceSquared(loc2) : Double.MAX_VALUE;
    }

    private static ExecutorService createService(){
        return Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setPriority(1).setNameFormat("WildStacker Stack Thread #" + threadId++).build());
    }

    private static class ServiceElement{

        private final ExecutorService executorService;
        private long lastUse;

        ServiceElement(){
            executorService = createService();
            lastUse = System.currentTimeMillis() / 1000;
        }

    }

}
