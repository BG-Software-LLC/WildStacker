package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.tasks.KillTask;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

@SuppressWarnings("unused")
public final class PlayersListener implements Listener {

    private WildStackerPlugin plugin;

    public PlayersListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    /*
    Just notifies me if the server is using WildBuster
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (e.getPlayer().getUniqueId().toString().equals("45713654-41bf-45a1-aa6f-00fe6598703b")) {
            Scheduler.runTask(e.getPlayer(), () -> e.getPlayer().sendMessage(
                    ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + "WildSeries" + ChatColor.DARK_GRAY + "] " +
                    ChatColor.GRAY + "This server is using WildStacker v" + plugin.getDescription().getVersion()), 5L);
        }

        if (e.getPlayer().isOp() && plugin.getUpdater().isOutdated()) {
            Scheduler.runTask(e.getPlayer(), () -> e.getPlayer().sendMessage(
                    ChatColor.GOLD + "" + ChatColor.BOLD + "WildStacker" +
                    ChatColor.GRAY + " A new version is available (v" + plugin.getUpdater().getLatestVersion() + ")!"), 20L);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if (plugin.getSettings().killTaskInterval <= 0 || plugin.getSettings().killTaskTimeCommand.isEmpty())
            return;

        for (String commandSyntax : plugin.getSettings().killTaskTimeCommand.split(",")) {
            commandSyntax = "/" + commandSyntax;

            if (!e.getMessage().equalsIgnoreCase(commandSyntax) && !e.getMessage().startsWith(commandSyntax + " "))
                continue;

            e.setCancelled(true);

            Locale.KILL_ALL_REMAINING_TIME.send(e.getPlayer(), KillTask.getTimeLeft());

            return;
        }
    }

}
