package xyz.wildseries.wildstacker.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.api.objects.StackedItem;
import xyz.wildseries.wildstacker.api.objects.StackedSpawner;
import xyz.wildseries.wildstacker.utils.async.AsyncCallback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused", "WeakerAccess", "InfiniteLoopStatement"})
public final class SafeStacker extends Thread {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final SafeStacker instance = new SafeStacker();
    private static final Map<Integer, Runnable> runnables = new ConcurrentHashMap<>();
    private static int lastCheckedRunnable = 0;
    private static int runnableNumber = 0;

    private SafeStacker(){
        start();
    }

    @Override
    public void run() {
        while(true){
            while(!runnables.isEmpty()){
                runnables.get(lastCheckedRunnable).run();
                runnables.remove(lastCheckedRunnable);
                lastCheckedRunnable++;
            }
            try {
                while(runnables.isEmpty())
                    Thread.sleep(10);
            }catch(Exception ignored){}
        }
    }

    private static void addOperation(Runnable runnable){
        runnables.put(runnableNumber++, runnable);
    }

    public static void tryStack(StackedEntity stackedEntity){
        tryStack(stackedEntity, null);
    }

    public static void tryStack(StackedEntity stackedEntity, AsyncCallback<LivingEntity> asyncCallback){
        int range = plugin.getSettings().entitiesCheckRange;
        List<Entity> entities = stackedEntity.getLivingEntity().getNearbyEntities(range, range, range);
        addOperation(() -> {
            LivingEntity livingEntity = stackedEntity.tryStackAsync(entities);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(livingEntity));
        });
    }

    public static void tryStackInto(StackedEntity stackedEntity, StackedEntity targetEntity){
        tryStackInto(stackedEntity, targetEntity, null);
    }

    public static void tryStackInto(StackedEntity stackedEntity, StackedEntity targetEntity, AsyncCallback<Boolean> asyncCallback){
        addOperation(() -> {
            boolean succeed = stackedEntity.tryStackInto(targetEntity);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(succeed));
        });
    }

    public static void tryStack(StackedItem stackedItem, AsyncCallback<Item> asyncCallback){
        int range = plugin.getSettings().entitiesCheckRange;
        List<Entity> entities = stackedItem.getItem().getNearbyEntities(range, range, range);
        addOperation(() -> {
            Item item = stackedItem.tryStackAsync(entities);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(item));
        });
    }

    public static void tryStackInto(StackedItem stackedItem, StackedItem targetItem){
        tryStackInto(stackedItem, targetItem, null);
    }

    public static void tryStackInto(StackedItem stackedItem, StackedItem targetItem, AsyncCallback<Boolean> asyncCallback){
        addOperation(() -> {
            boolean succeed = stackedItem.tryStackInto(targetItem);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(succeed));
        });
    }

    public static void trySpawnerStack(StackedEntity stackedEntity, StackedSpawner stackedSpawner){
        trySpawnerStack(stackedEntity, stackedSpawner, null);
    }

    public static void trySpawnerStack(StackedEntity stackedEntity, StackedSpawner stackedSpawner, AsyncCallback<LivingEntity> asyncCallback){
        int range = plugin.getSettings().entitiesCheckRange;
        List<Entity> entities = stackedEntity.getLivingEntity().getNearbyEntities(range, range, range);
        addOperation(() -> {
            LivingEntity livingEntity = stackedEntity.trySpawnerStackAsync(stackedSpawner, entities);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(livingEntity));
        });
    }

}
