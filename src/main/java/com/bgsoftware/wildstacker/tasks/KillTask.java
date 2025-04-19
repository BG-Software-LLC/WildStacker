package com.bgsoftware.wildstacker.tasks;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class KillTask extends BukkitRunnable {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static BukkitTask task = null;
    private static long timeLeft;

    private KillTask() {
        timeLeft = plugin.getSettings().getKillTask().getInterval();
        task = runTaskTimer(plugin, 20L, 20L);
    }

    public static void start() {
        if (task != null)
            task.cancel();

        new KillTask();
    }

    public static long getTimeLeft() {
        return timeLeft;
    }

    @Override
    public void run() {
        if (plugin.getSettings().getKillTask().getInterval() > 0) {
            if (timeLeft == 0) {
                if (Bukkit.getOnlinePlayers().size() > 0) {
                    plugin.getSystemManager().performKillAll(true);
                    for (Player player : Bukkit.getOnlinePlayers())
                        Locale.KILL_ALL_ANNOUNCEMENT.send(player);
                }

                timeLeft = plugin.getSettings().getKillTask().getInterval();
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
