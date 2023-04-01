package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.stacker.entities.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class ClearLaggHook {

    private static long lastTime = 0;

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClearLaggTask(EntityRemoveEvent e) {
                e.getEntityList().forEach(entity -> {
                    if (EntityUtils.isStackable(entity))
                        WStackedEntity.of(entity).remove();
                });
                if (plugin.getSettings().killTaskSyncClearLagg && System.currentTimeMillis() - lastTime > 1000) {
                    lastTime = System.currentTimeMillis();
                    plugin.getSystemManager().performKillAll(true);
                }
            }
        }, plugin);
    }

}
