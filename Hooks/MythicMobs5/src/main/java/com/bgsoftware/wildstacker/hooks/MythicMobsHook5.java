package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.stacker.entities.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class MythicMobsHook5 {

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new MythicMobsListener(), plugin);
        plugin.getProviders().registerEntityDuplicateListener(MythicMobsHook5::tryDuplicate);
    }

    private static LivingEntity tryDuplicate(LivingEntity livingEntity) {
        if (WStackedEntity.of(livingEntity).getSpawnCause() == SpawnCause.MYTHIC_MOBS) {
            ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(livingEntity);
            ActiveMob duplicate = MythicBukkit.inst().getMobManager().spawnMob(activeMob.getType().getInternalName(), livingEntity.getLocation());
            return (LivingEntity) duplicate.getEntity().getBukkitEntity();
        }

        return null;
    }

    public static final class MythicMobsListener implements Listener {

        @EventHandler
        public void onMythicMobSpawn(MythicMobSpawnEvent e) {
            if (EntityUtils.isStackable(e.getEntity()))
                WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.MYTHIC_MOBS);
        }

    }

}
