package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.scheduler.Scheduler;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.dsh105.echopet.compat.api.event.PetPreSpawnEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class EchoPetHook {

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onEchoPetSpawn(PetPreSpawnEvent e) {
                Entity entity = e.getPet().getEntityPet().getBukkitEntity();
                Scheduler.runTask(entity, () -> {
                    if (EntityUtils.isStackable(entity))
                        WStackedEntity.of(entity).setSpawnCause(SpawnCause.ECHO_PET);
                }, 1L);
            }
        }, plugin);
    }

}
