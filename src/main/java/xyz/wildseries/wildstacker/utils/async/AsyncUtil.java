package xyz.wildseries.wildstacker.utils.async;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.api.objects.StackedItem;

@SuppressWarnings("WeakerAccess")
public class AsyncUtil {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    public static void tryStack(StackedEntity stackedEntity){
        tryStack(stackedEntity, null);
    }

    public static void tryStack(StackedEntity stackedEntity, AsyncCallback<LivingEntity> asyncCallback){
        if(Bukkit.isPrimaryThread()) {
            new Thread(() -> {
                LivingEntity livingEntity = stackedEntity.tryStack();
                if (asyncCallback != null)
                    Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(livingEntity));
            }).start();
        }else{
            LivingEntity livingEntity = stackedEntity.tryStack();
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(livingEntity));
        }
    }

    public static void tryStackInto(StackedEntity stackedEntity, StackedEntity targetEntity){
        tryStackInto(stackedEntity, targetEntity, null);
    }

    public static void tryStackInto(StackedEntity stackedEntity, StackedEntity targetEntity, AsyncCallback<Boolean> asyncCallback){
        if(Bukkit.isPrimaryThread()) {
            new Thread(() -> {
                boolean succeed = stackedEntity.tryStackInto(targetEntity);
                if (asyncCallback != null)
                    Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(succeed));
            }).start();
        }else{
            boolean succeed = stackedEntity.tryStackInto(targetEntity);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(succeed));
        }
    }

    public static void tryStack(StackedItem stackedItem, AsyncCallback<Item> asyncCallback){
        if(Bukkit.isPrimaryThread()) {
            new Thread(() -> {
                Item item = stackedItem.tryStack();
                if (asyncCallback != null)
                    Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(item));
            }).start();
        }else{
            Item item = stackedItem.tryStack();
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(item));
        }
    }

    public static void tryStackInto(StackedItem stackedItem, StackedItem targetItem){
        tryStackInto(stackedItem, targetItem, null);
    }

    public static void tryStackInto(StackedItem stackedItem, StackedItem targetItem, AsyncCallback<Boolean> asyncCallback){
        if(Bukkit.isPrimaryThread()) {
            new Thread(() -> {
                boolean succeed = stackedItem.tryStackInto(targetItem);
                if (asyncCallback != null)
                    Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(succeed));
            }).start();
        }else{
            boolean succeed = stackedItem.tryStackInto(targetItem);
            if (asyncCallback != null)
                Bukkit.getScheduler().runTask(plugin, () -> asyncCallback.run(succeed));
        }
    }

}
