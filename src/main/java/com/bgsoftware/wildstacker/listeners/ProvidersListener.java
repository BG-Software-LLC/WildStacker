package com.bgsoftware.wildstacker.listeners;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

@SuppressWarnings("unused")
public final class ProvidersListener implements Listener {

    private final WildStackerPlugin plugin;

    public ProvidersListener(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginEnable(PluginEnableEvent e) {
        plugin.getProviders().loadPluginHooks(plugin, e.getPlugin(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginEnable(PluginDisableEvent e) {
        plugin.getProviders().loadPluginHooks(plugin, e.getPlugin(), false);
    }

}
