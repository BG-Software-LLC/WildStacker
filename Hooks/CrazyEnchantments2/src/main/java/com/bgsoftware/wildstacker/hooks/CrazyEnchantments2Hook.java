package com.bgsoftware.wildstacker.hooks;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.managers.AllyManager;
import com.badbones69.crazyenchantments.paper.api.objects.AllyMob;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public final class CrazyEnchantments2Hook {

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
        private final AllyManager allyManager;

        AllyListener(WildStackerPlugin plugin) {
            this.plugin = plugin;

            CrazyEnchantments crazyEnchantments = JavaPlugin.getPlugin(CrazyEnchantments.class);
            this.allyManager = crazyEnchantments.getStarter().getAllyManager();
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void onAllySpawn(CreatureSpawnEvent e) {
            if (!EntityUtils.isStackable(e.getEntity()))
                return;

            AllyMob.AllyType allyType = getAllyType(e.getEntityType());
            if (allyType == null)
                return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (allyManager.isAllyMob(e.getEntity())) {
                    WStackedEntity.of(e.getEntity()).setSpawnCause(SpawnCause.CRAZY_ENCHANTMENTS);
                }
            }, 1L);
        }

    }


}
