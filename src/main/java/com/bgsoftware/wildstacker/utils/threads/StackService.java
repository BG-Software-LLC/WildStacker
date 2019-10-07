package com.bgsoftware.wildstacker.utils.threads;

import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
public final class StackService {

    private static final Map<Location, ServiceElement> services = new ConcurrentHashMap<>();
    private static int threadId = 1;

    public static void execute(StackedObject stackedObject, Runnable runnable){
        if(isStackThread()) {
            runnable.run();
        }
        else {
            getOrCreateService(stackedObject).execute(runnable);
        }
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
                serviceElement.executorService.shutdownNow();
        }catch(Exception ex){
            ex.printStackTrace();
        }
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
