package xyz.wildseries.wildstacker.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.wildseries.wildstacker.Updater;
import xyz.wildseries.wildstacker.WildStackerPlugin;

@SuppressWarnings("unused")
public final class PlayersListener implements Listener {

    private WildStackerPlugin instance;

    public PlayersListener(WildStackerPlugin instance){
        this.instance = instance;
    }

    /*
    Just notifies me if the server is using WildBuster
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if(e.getPlayer().getUniqueId().toString().equals("45713654-41bf-45a1-aa6f-00fe6598703b")){
            Bukkit.getScheduler().runTaskLater(instance, () ->
                e.getPlayer().sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + "WildSeries" + ChatColor.DARK_GRAY + "] " +
                        ChatColor.GRAY + "This server is using WildStacker v" + instance.getDescription().getVersion()), 5L);
        }

        if(e.getPlayer().isOp() && Updater.isOutdated()){
            Bukkit.getScheduler().runTaskLater(instance, () ->
                e.getPlayer().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "WildStacker" +
                        ChatColor.GRAY + " A new version is available (v" + Updater.getLatestVersion() + ")!"), 20L);
        }
    }

}
