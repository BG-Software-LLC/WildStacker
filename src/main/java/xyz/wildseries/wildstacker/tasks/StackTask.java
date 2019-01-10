package xyz.wildseries.wildstacker.tasks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.objects.WStackedEntity;
import xyz.wildseries.wildstacker.utils.async.AsyncUtil;

import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class StackTask extends BukkitRunnable {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static int taskID = -1;

    private StackTask(){
        if(plugin.getSettings().entitiesStackingEnabled && plugin.getSettings().entitiesStackInterval > 0)
            taskID = runTaskTimerAsynchronously(plugin, plugin.getSettings().entitiesStackInterval, plugin.getSettings().entitiesStackInterval).getTaskId();
    }

    public static void start(){
        if(Bukkit.getScheduler().isCurrentlyRunning(taskID))
            Bukkit.getScheduler().cancelTask(taskID);
        new StackTask();
    }

    @Override
    public void run() {
        if(Bukkit.getOnlinePlayers().size() > 0) {
            for(World world : Bukkit.getWorlds()){
                try {
                    Set<LivingEntity> livingEntities = ConcurrentHashMap.newKeySet();
                    livingEntities.addAll(world.getLivingEntities());

                    for (LivingEntity livingEntity : livingEntities) {
                        if (!livingEntity.isValid() || livingEntity instanceof ArmorStand || livingEntity instanceof Player)
                            continue;

                        if (plugin.getSettings().blacklistedEntities.contains(livingEntity.getType().name()))
                            continue;

                        AsyncUtil.tryStack(WStackedEntity.of(livingEntity));
                    }
                }catch(ConcurrentModificationException ignored){}
            }
        }
    }
}
