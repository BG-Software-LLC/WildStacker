package com.bgsoftware.wildstacker.utils.threads;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public final class StackService {

    private static final Map<Location, ServiceElement> services = new ConcurrentHashMap<>();
    private static int threadId = 1;

    public static void execute(StackedObject stackedObject, Runnable runnable){
        getOrCreateService(stackedObject).execute(() -> {
            synchronized (getOrCreateMutex(stackedObject)){
                runnable.run();
            }
        });
    }

    public static ExecutorService getOrCreateService(StackedObject stackedObject){
        Location location = stackedObject.getLocation();
        Location serviceLocation = new Location(location.getWorld(), location.getBlockX() >> 10, 0, location.getBlockZ() >> 10);

        if(!services.containsKey(serviceLocation))
            services.put(serviceLocation, new ServiceElement());

        ServiceElement serviceElement = services.get(serviceLocation);

        serviceElement.lastUse = System.currentTimeMillis();

        return serviceElement.executorService;
    }

    public static Object getOrCreateMutex(StackedObject stackedObject){
        Location location = stackedObject.getLocation();
        Location serviceLocation = new Location(location.getWorld(), location.getBlockX() >> 10, 0, location.getBlockZ() >> 10);

        if(!services.containsKey(serviceLocation))
            services.put(serviceLocation, new ServiceElement());

        ServiceElement serviceElement = services.get(serviceLocation);

        serviceElement.lastUse = System.currentTimeMillis();

        if(stackedObject instanceof StackedEntity)
            return serviceElement.mutex[0];
        else if(stackedObject instanceof StackedItem)
            return serviceElement.mutex[1];
        else if(stackedObject instanceof StackedSpawner)
            return serviceElement.mutex[2];
        else if(stackedObject instanceof StackedBarrel)
            return serviceElement.mutex[3];

        throw new IllegalArgumentException("Couldn't verify stacked object " + stackedObject + ".");
    }

    public static boolean isStackThread(){
        return Thread.currentThread().getName().startsWith("WildStacker Stack Thread");
    }

    public static boolean canStackFromThread(){
        return isStackThread() || Bukkit.isPrimaryThread();
    }

    public static void clearCache(){
        for(Map.Entry<Location, ServiceElement> entry : services.entrySet()){
            if((System.currentTimeMillis() / 1000) - entry.getValue().lastUse >= 300)
                services.remove(entry.getKey());
        }
    }

    public static void stop(){
        try{
            for(ServiceElement serviceElement : services.values())
                serviceElement.executorService.shutdown();
            for(ServiceElement serviceElement : services.values())
                serviceElement.executorService.awaitTermination(1, TimeUnit.MINUTES);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static ExecutorService createService(){
        return Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setPriority(1).setNameFormat("WildStacker Stack Thread #" + threadId++).build());
    }

    private static Object[] createMutex(){
        return new Object[] {new Object(), new Object(), new Object(), new Object()};
    }

    private static class ServiceElement{

        private final ExecutorService executorService;
        private final Object[] mutex;
        private long lastUse;

        ServiceElement(){
            executorService = createService();
            mutex = createMutex();
            lastUse = System.currentTimeMillis() / 1000;
        }

    }

}
