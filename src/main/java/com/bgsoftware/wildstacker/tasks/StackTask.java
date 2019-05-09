package com.bgsoftware.wildstacker.tasks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
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
        List<StackedEntity> stackedEntities = new ArrayList<>();

        if(Bukkit.getOnlinePlayers().size() > 0) {
            for(World world : Bukkit.getWorlds()){
                try {
                    Set<LivingEntity> livingEntities = ConcurrentHashMap.newKeySet();
                    livingEntities.addAll(world.getLivingEntities());

                    for (LivingEntity livingEntity : livingEntities) {
                        try {
                            if (!livingEntity.isValid() || livingEntity instanceof ArmorStand || livingEntity instanceof Player)
                                continue;

                            StackedEntity stackedEntity = WStackedEntity.of(livingEntity);

                            if (!stackedEntity.isWhitelisted() || stackedEntity.isBlacklisted() || stackedEntity.isWorldDisabled())
                                continue;

                            stackedEntities.add(stackedEntity);
                        } catch (Throwable ignored) { }
                    }
                }catch(Throwable ignored){}
            }
        }

        Executor.sync(() -> stackedEntities.forEach(StackedEntity::tryStack));
    }
}
