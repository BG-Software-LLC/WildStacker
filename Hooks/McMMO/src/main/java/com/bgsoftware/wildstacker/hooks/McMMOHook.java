package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.listeners.IEntityDeathListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class McMMOHook {

    private static final String CUSTOM_NAME_KEY = "mcMMO: Custom Name";
    private static final String CUSTOM_NAME_VISIBLE_KEY = "mcMMO: Name Visibility";

    // Multiplier tags
    private static final String SPAWNER_ENTITY_KEY = "mcMMO: Spawned Entity";

    private static final Set<UUID> spawnerEntities = new HashSet<>();

    private static WildStackerPlugin plugin;
    private static Plugin mcMMO;

    public static void register(WildStackerPlugin plugin) {
        McMMOHook.plugin = plugin;
        mcMMO = com.gmail.nossr50.mcMMO.p;
        plugin.getProviders().registerEntityDeathListener(McMMOHook::handleDeath);
        plugin.getProviders().registerNameChangeListener(McMMOHook::updateCachedName);
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
        livingEntity.setMetadata(SPAWNER_ENTITY_KEY, new FixedMetadataValue(mcMMO, true));
    }

    private static boolean isSpawnerEntity(LivingEntity livingEntity) {
        return livingEntity.hasMetadata(SPAWNER_ENTITY_KEY);
    }

}
