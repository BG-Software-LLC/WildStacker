package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.hooks.PluginHooks;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class MythicMobsListener implements Listener {

    public MythicMobsListener() {
        PluginHooks.isMythicMobsEnabled = true;
    }

    @EventHandler
    public void onMythicMobSpawn(MythicMobSpawnEvent e) {
        if (EntityUtils.isStackable(e.getEntity()))
            WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.MYTHIC_MOBS);
    }

}
