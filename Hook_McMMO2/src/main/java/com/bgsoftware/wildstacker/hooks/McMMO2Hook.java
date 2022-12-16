package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.listeners.IEntityDeathListener;
import com.gmail.nossr50.datatypes.meta.RuptureTaskMeta;
import com.gmail.nossr50.util.compat.CompatibilityManager;
import com.gmail.nossr50.util.compat.layers.persistentdata.MobMetaFlagType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class McMMO2Hook {

    private static final String CUSTOM_NAME_KEY = "mcMMO: Custom Name";
    private static final String CUSTOM_NAME_VISIBLE_KEY = "mcMMO: Name Visibility";

    private static final String RUPTURE_TASK_KEY = "mcMMO: RuptureTask";

    private static final Set<UUID> spawnerEntities = new HashSet<>();

    private static WildStackerPlugin plugin;
    private static com.gmail.nossr50.mcMMO mcMMO;

    public static void register(WildStackerPlugin plugin) {
        McMMO2Hook.plugin = plugin;
        mcMMO = com.gmail.nossr50.mcMMO.p;
        plugin.getProviders().registerEntityDeathListener(McMMO2Hook::handleDeath);
        plugin.getProviders().registerNameChangeListener(McMMO2Hook::updateCachedName);
    }

    private static void handleDeath(StackedEntity stackedEntity, IEntityDeathListener.Type type) {
        LivingEntity livingEntity = stackedEntity.getLivingEntity();

        switch (type) {
            case BEFORE_DEATH_EVENT:
                updateCachedName(livingEntity);
                if (isSpawnerEntity(livingEntity))
                    spawnerEntities.add(livingEntity.getUniqueId());
                break;
            case AFTER_DEATH_EVENT:
                if (spawnerEntities.remove(livingEntity.getUniqueId()))
                    updateSpawnedEntity(livingEntity);

                cancelRuptureTask(livingEntity);
                break;
        }
    }

    private static void updateCachedName(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return;

        if (entity.hasMetadata(CUSTOM_NAME_KEY)) {
            entity.removeMetadata(CUSTOM_NAME_KEY, mcMMO);
            entity.setMetadata(CUSTOM_NAME_KEY, new FixedMetadataValue(mcMMO, plugin.getNMSEntities().getCustomName(entity)));
        }
        if (entity.hasMetadata(CUSTOM_NAME_VISIBLE_KEY)) {
            entity.removeMetadata(CUSTOM_NAME_VISIBLE_KEY, mcMMO);
            entity.setMetadata(CUSTOM_NAME_VISIBLE_KEY, new FixedMetadataValue(mcMMO, plugin.getNMSEntities().isCustomNameVisible(entity)));
        }
    }

    private static void updateSpawnedEntity(LivingEntity livingEntity) {
        CompatibilityManager compatibilityManager = com.gmail.nossr50.mcMMO.getCompatibilityManager();
        if (compatibilityManager != null)
            compatibilityManager.getPersistentDataLayer().flagMetadata(MobMetaFlagType.MOB_SPAWNER_MOB, livingEntity);
    }

    private static void cancelRuptureTask(LivingEntity livingEntity) {
        for (MetadataValue metadataValue : livingEntity.getMetadata(RUPTURE_TASK_KEY)) {
            if (metadataValue instanceof RuptureTaskMeta) {
                ((RuptureTaskMeta) metadataValue).getRuptureTimerTask().cancel();
            }
        }
    }

    private static boolean isSpawnerEntity(LivingEntity livingEntity) {
        return hasTag(livingEntity, MobMetaFlagType.MOB_SPAWNER_MOB);
    }

    private static boolean hasTag(LivingEntity livingEntity, MobMetaFlagType mobMetaFlagType) {
        CompatibilityManager compatibilityManager = com.gmail.nossr50.mcMMO.getCompatibilityManager();
        return compatibilityManager != null && compatibilityManager.getPersistentDataLayer()
                .hasMobFlag(mobMetaFlagType, livingEntity);
    }

}
