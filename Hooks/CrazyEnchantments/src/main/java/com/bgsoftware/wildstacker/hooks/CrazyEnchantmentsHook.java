package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import me.badbones69.crazyenchantments.api.managers.AllyManager;
import me.badbones69.crazyenchantments.api.objects.AllyMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;

public final class CrazyEnchantmentsHook {

    private static boolean registered = false;

    public static void register(WildStackerPlugin plugin) {
        if (registered)
            return;

        plugin.getServer().getPluginManager().registerEvents(new AllyListener(plugin), plugin);

        registered = true;
    }

    @Nullable
    private static AllyMob.AllyType getAllyType(EntityType entityType) {
        for (AllyMob.AllyType allyType : AllyMob.AllyType.values()) {
            if (allyType.getEntityType() == entityType) {
                return allyType;
            }
        }

        return null;
    }

    private static class AllyListener implements Listener {

        private final WildStackerPlugin plugin;

        AllyListener(WildStackerPlugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void onAllySpawn(CreatureSpawnEvent e) {
            if (!EntityUtils.isStackable(e.getEntity()))
                return;

            AllyMob.AllyType allyType = getAllyType(e.getEntityType());
            if (allyType == null)
                return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (AllyManager.getInstance().isAllyMob(e.getEntity())) {
                    WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.CRAZY_ENCHANTMENTS);
                }
            }, 1L);
        }

    }


}
