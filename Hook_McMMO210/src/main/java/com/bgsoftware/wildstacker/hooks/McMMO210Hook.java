package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.listeners.IEntityDeathListener;
import com.gmail.nossr50.datatypes.meta.OldName;
import com.gmail.nossr50.datatypes.meta.RuptureTaskMeta;
import com.gmail.nossr50.metadata.MobMetaFlagType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class McMMO210Hook {

    private static final String CUSTOM_NAME_KEY = "mcMMO: Custom Name";
    private static final String CUSTOM_NAME_NEW_KEY = "mcmmo_custom_name";
    private static final String OLD_NAME_KEY = "mcmmo_old_name";
    private static final String CUSTOM_NAME_VISIBLE_KEY = "mcMMO: Name Visibility";

    private static final String RUPTURE_TASK_KEY = "mcMMO: RuptureTask";

    private static final Set<UUID> spawnerEntities = new HashSet<>();

    private static WildStackerPlugin plugin;
    private static com.gmail.nossr50.mcMMO mcMMO;

    public static void register(WildStackerPlugin plugin) {
        McMMO210Hook.plugin = plugin;
        mcMMO = com.gmail.nossr50.mcMMO.p;
        plugin.getProviders().registerEntityDeathListener(McMMO210Hook::handleDeath);
        plugin.getProviders().registerNameChangeListener(McMMO210Hook::updateCachedName);
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

        String name = plugin.getNMSEntities().getCustomName(entity);

        if (entity.hasMetadata(CUSTOM_NAME_KEY)) {
            entity.removeMetadata(CUSTOM_NAME_KEY, mcMMO);
            entity.setMetadata(CUSTOM_NAME_KEY, new FixedMetadataValue(mcMMO, name));
        }

        if (entity.hasMetadata(CUSTOM_NAME_NEW_KEY)) {
            entity.removeMetadata(CUSTOM_NAME_NEW_KEY, mcMMO);
            entity.setMetadata(CUSTOM_NAME_NEW_KEY, new FixedMetadataValue(mcMMO, name));
        }

        if (entity.hasMetadata(OLD_NAME_KEY)) {
            entity.removeMetadata(OLD_NAME_KEY, mcMMO);
            entity.setMetadata(OLD_NAME_KEY, new OldName(name, mcMMO));
        }

        if (entity.hasMetadata(CUSTOM_NAME_VISIBLE_KEY)) {
            entity.removeMetadata(CUSTOM_NAME_VISIBLE_KEY, mcMMO);
            entity.setMetadata(CUSTOM_NAME_VISIBLE_KEY, new FixedMetadataValue(mcMMO, plugin.getNMSEntities().isCustomNameVisible(entity)));
        }
    }

    private static void updateSpawnedEntity(LivingEntity livingEntity) {
        com.gmail.nossr50.mcMMO.getMetadataService().getMobMetadataService()
                .flagMetadata(MobMetaFlagType.MOB_SPAWNER_MOB, livingEntity);
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
        return com.gmail.nossr50.mcMMO.getMetadataService().getMobMetadataService()
                .hasMobFlag(mobMetaFlagType, livingEntity);
    }

}
