package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import net.aminecraftdev.custombosses.events.BossSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class CustomBossesHook {

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onBossSpawn(BossSpawnEvent e) {
                if (EntityUtils.isStackable(e.getBoss()))
                    WStackedEntity.of(e.getBoss()).setSpawnCause(SpawnCause.CUSTOM_BOSSES);
            }
        }, plugin);
    }

}
