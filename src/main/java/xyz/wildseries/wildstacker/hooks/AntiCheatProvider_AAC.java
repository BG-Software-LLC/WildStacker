package xyz.wildseries.wildstacker.hooks;

import me.konsolas.aac.api.PlayerViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.wildseries.wildstacker.WildStackerPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class AntiCheatProvider_AAC implements AntiCheatProvider, Listener {

    private Set<UUID> bypassPlayers = new HashSet<>();

    public AntiCheatProvider_AAC(){
        WildStackerPlugin.log(" - Using AAC as AntiCheatProvider.");
        Bukkit.getPluginManager().registerEvents(this, WildStackerPlugin.getPlugin());
    }

    @Override
    public void enableBypass(Player player) {
        bypassPlayers.add(player.getUniqueId());
    }

    @Override
    public void disableBypass(Player player) {
        bypassPlayers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerViolation(PlayerViolationEvent e) {
        if(bypassPlayers.contains(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

}
