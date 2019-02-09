package xyz.wildseries.wildstacker.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class SafeStacker {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final Executor executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("WildStacker Thread").build());

    private static void execute(Runnable runnable){
        executor.execute(runnable);
    }

    public static void tryStack(StackedEntity stackedEntity){
        tryStack(stackedEntity, null);
    }

    public static void tryStack(StackedEntity stackedEntity, AsyncCallback<LivingEntity> asyncCallback){
        int range = plugin.getSettings().entitiesCheckRange;
        List<Entity> entities = stackedEntity.getLivingEntity().getNearbyEntities(range, range, range);
        execute(() -> {
            LivingEntity livingEntity = stackedEntity.tryStackAsync(entities);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(livingEntity));
        });
    }

    public static void tryStackInto(StackedEntity stackedEntity, StackedEntity targetEntity){
        tryStackInto(stackedEntity, targetEntity, null);
    }

    public static void tryStackInto(StackedEntity stackedEntity, StackedEntity targetEntity, AsyncCallback<Boolean> asyncCallback){
        execute(() -> {
            boolean succeed = stackedEntity.tryStackInto(targetEntity);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(succeed));
        });
    }

    public static void tryStack(StackedItem stackedItem, AsyncCallback<Item> asyncCallback){
        int range = plugin.getSettings().entitiesCheckRange;
        List<Entity> entities = stackedItem.getItem().getNearbyEntities(range, range, range);
        execute(() -> {
            Item item = stackedItem.tryStackAsync(entities);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(item));
        });
    }

    public static void tryStackInto(StackedItem stackedItem, StackedItem targetItem){
        tryStackInto(stackedItem, targetItem, null);
    }

    public static void tryStackInto(StackedItem stackedItem, StackedItem targetItem, AsyncCallback<Boolean> asyncCallback){
        execute(() -> {
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
        execute(() -> {
            LivingEntity livingEntity = stackedEntity.trySpawnerStackAsync(stackedSpawner, entities);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(livingEntity));
        });
    }

}
