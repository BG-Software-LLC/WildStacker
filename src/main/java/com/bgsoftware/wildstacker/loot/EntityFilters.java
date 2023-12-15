package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.loot.entity.LivingLootEntityAttributes;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.json.simple.JSONArray;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class EntityFilters {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private EntityFilters() {

    }

    public static Predicate<LootEntityAttributes> checkPermissionFilter(String permission) {
        return entityData -> entityData.getEntityType() != EntityType.PLAYER ||
                !(entityData instanceof LivingLootEntityAttributes) ||
                ((LivingLootEntityAttributes) entityData).getLivingEntity().hasPermission(permission);
    }

    public static Predicate<LootEntityAttributes> checkUpgradeFilter(String upgrade) {
        return entityData -> {
            if (entityData.isIgnoreUpgrade())
                return true;
            SpawnerUpgrade spawnerUpgrade = entityData.getUpgrade();
            return spawnerUpgrade != null && spawnerUpgrade.getName().equalsIgnoreCase(upgrade);
        };
    }

    public static Predicate<LootEntityAttributes> spawnCauseFilter(String spawnCauseName) {
        boolean negate = spawnCauseName.startsWith("!");
        SpawnCause spawnCause = SpawnCause.valueOf((negate ? spawnCauseName.substring(1) : spawnCauseName)
                .toUpperCase(Locale.ENGLISH));
        return entityData -> {
            if (entityData.isIgnoreSpawnCause())
                return true;
            SpawnCause entityDataSpawnCause = entityData.getSpawnCause();
            return entityDataSpawnCause != null && (entityDataSpawnCause == spawnCause) != negate;
        };
    }

    public static Predicate<LootEntityAttributes> spawnCausesFilter(JSONArray spawnCausesArray) {
        FastEnumArray<SpawnCause> filteredSpawnCauses = new FastEnumArray<>(SpawnCause.class);
        // noinspection unchecked
        spawnCausesArray.forEach(spawnCauseName -> {
            try {
                filteredSpawnCauses.add(SpawnCause.valueOf(((String) spawnCauseName).toUpperCase(Locale.ENGLISH)));
            } catch (Throwable ignored) {
            }
        });

        if (filteredSpawnCauses.size() == 0)
            throw new IllegalArgumentException("No filters");

        return entityData -> {
            if (entityData.isIgnoreSpawnCause())
                return true;
            SpawnCause entityDataSpawnCause = entityData.getSpawnCause();
            return entityDataSpawnCause != null && filteredSpawnCauses.contains(entityDataSpawnCause);
        };
    }

    public static Predicate<LootEntityAttributes> deathCauseFilter(String deathCauseName) {
        boolean negate = deathCauseName.startsWith("!");
        EntityDamageEvent.DamageCause filteredDeathCause = EntityDamageEvent.DamageCause.valueOf(
                (negate ? deathCauseName.substring(1) : deathCauseName).toUpperCase(Locale.ENGLISH));

        return entityData -> {
            if (entityData.isIgnoreDeathCause())
                return true;
            EntityDamageEvent.DamageCause deathCause = entityData.getDeathCause();
            return deathCause != null && (deathCause == filteredDeathCause) != negate;
        };
    }

    public static Predicate<LootEntityAttributes> deathCausesFilter(JSONArray deathCausesArray) {
        FastEnumArray<EntityDamageEvent.DamageCause> filteredDeathCauses = new FastEnumArray<>(EntityDamageEvent.DamageCause.class);
        // noinspection unchecked
        deathCausesArray.forEach(deathCauseName -> {
            try {
                filteredDeathCauses.add(EntityDamageEvent.DamageCause.valueOf(((String) deathCauseName).toUpperCase(Locale.ENGLISH)));
            } catch (Throwable ignored) {
            }
        });

        if (filteredDeathCauses.size() == 0)
            throw new IllegalArgumentException("No filters");

        return entityData -> {
            if (entityData.isIgnoreDeathCause())
                return true;
            EntityDamageEvent.DamageCause deathCause = entityData.getDeathCause();
            return deathCause != null && filteredDeathCauses.contains(deathCause);
        };
    }

    public static Predicate<LootEntityAttributes> typeFilter(String entityTypeName) {
        boolean negate = entityTypeName.startsWith("!");
        EntityType entityType = EntityType.valueOf((negate ? entityTypeName.substring(1) : entityTypeName).toUpperCase(Locale.ENGLISH));
        return entityData -> (entityData.getEntityType() == entityType) != negate;
    }

    public static Predicate<LootEntityAttributes> advancedFilter(Map<String, Object> advancedFilter) {
        Object typeName = advancedFilter.get("type");

        if (!(typeName instanceof String))
            throw new IllegalArgumentException("Missing type field");

        // Only type filter.
        if (advancedFilter.size() == 1)
            return typeFilter((String) typeName);

        EntityType entityType = EntityType.valueOf(((String) typeName).toUpperCase(Locale.ENGLISH));

        if (!entityType.isAlive())
            return typeFilter((String) typeName);

        advancedFilter.remove("type");

        return entityData -> {
            if (!(entityData instanceof LivingLootEntityAttributes))
                return true;

            if (entityData.getEntityType() != entityType)
                return false;

            LivingEntity livingEntity = ((LivingLootEntityAttributes) entityData).getLivingEntity();
            return plugin.getNMSEntities().checkEntityAttributes(livingEntity, advancedFilter);
        };
    }

}
