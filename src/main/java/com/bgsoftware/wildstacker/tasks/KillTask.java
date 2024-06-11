package com.bgsoftware.wildstacker.tasks;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.scheduler.ScheduledTask;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class KillTask {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static ScheduledTask task = null;
    private static long timeLeft;

    private KillTask() {
        timeLeft = plugin.getSettings().killTaskInterval;
        task = Scheduler.runRepeatingTask(this::run, 20L);
    }

    public static void start() {
        if (task != null)
            task.cancel();

        new KillTask();
    }

    public static long getTimeLeft() {
        return timeLeft;
    }

    public void run() {
        if (plugin.getSettings().killTaskInterval > 0) {
            if (timeLeft == 0) {
                if (Bukkit.getOnlinePlayers().size() > 0) {
                    plugin.getSystemManager().performKillAll(true);
                    for (Player player : Bukkit.getOnlinePlayers())
                        Locale.KILL_ALL_ANNOUNCEMENT.send(player);
                }

                timeLeft = plugin.getSettings().killTaskInterval;
                return;
            }

            if (timeLeft == 10 || timeLeft == 30 || timeLeft == 60) {
                for (Player player : Bukkit.getOnlinePlayers())
                    Locale.KILL_ALL_REMAINING_TIME.send(player, timeLeft);
            }

            timeLeft -= 1;
        }
    }

}
