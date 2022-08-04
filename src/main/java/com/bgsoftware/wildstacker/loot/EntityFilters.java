package com.bgsoftware.wildstacker.loot;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class EntityFilters {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final JSONParser JSON_PARSER = new JSONParser();

    private EntityFilters() {

    }

    public static Predicate<Entity> checkPermissionFilter(String permission) {
        return entity -> !(entity instanceof Player) || entity.hasPermission(permission);
    }

    public static Predicate<Entity> checkUpgradeFilter(String upgrade) {
        return entity -> upgrade.equalsIgnoreCase(WStackedEntity.of(entity).getUpgrade().getName());
    }

    public static Predicate<Entity> spawnCauseFilter(String spawnCauseName) {
        boolean negate = spawnCauseName.startsWith("!");
        SpawnCause spawnCause = SpawnCause.valueOf((negate ? spawnCauseName.substring(1) : spawnCauseName)
                .toUpperCase(Locale.ENGLISH));
        return entity -> (WStackedEntity.of(entity).getSpawnCause() == spawnCause) != negate;
    }

    public static Predicate<Entity> spawnCausesFilter(JSONArray spawnCausesArray) {
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

        return entity -> filteredSpawnCauses.contains(WStackedEntity.of(entity).getSpawnCause());
    }

    public static Predicate<Entity> deathCauseFilter(String deathCauseName) {
        boolean negate = deathCauseName.startsWith("!");
        EntityDamageEvent.DamageCause filteredDeathCause = EntityDamageEvent.DamageCause.valueOf(
                (negate ? deathCauseName.substring(1) : deathCauseName).toUpperCase(Locale.ENGLISH));
        return entity -> (LootTable.getDeathCause(entity) == filteredDeathCause) != negate;
    }

    public static Predicate<Entity> deathCausesFilter(JSONArray deathCausesArray) {
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

        return entity -> {
            EntityDamageEvent.DamageCause deathCause = LootTable.getDeathCause(entity);
            return deathCause != null && filteredDeathCauses.contains(deathCause);
        };
    }

    public static Predicate<Entity> typeFilter(String entityTypeName) {
        boolean negate = entityTypeName.startsWith("!");
        EntityType entityType = EntityType.valueOf((negate ? entityTypeName.substring(1) : entityTypeName).toUpperCase(Locale.ENGLISH));
        return entity -> (entity.getType() == entityType) != negate;
    }

    public static Predicate<Entity> advancedFilter(Map<String, Object> advancedFilter) {
        Object typeName = advancedFilter.get("type");

        if (!(typeName instanceof String))
            throw new IllegalArgumentException("Missing type field");

        // Only type filter.
        if (advancedFilter.size() == 1)
            return typeFilter((String) typeName);

        EntityType entityType = EntityType.valueOf(((String) typeName).toUpperCase(Locale.ENGLISH));

        if (!entityType.isAlive())
            return typeFilter((String) typeName);

        return entity -> {
            if (entity.getType() != entityType)
                return false;

            advancedFilter.remove("type");

            return plugin.getNMSEntities().checkEntityAttributes((LivingEntity) entity, advancedFilter);
        };
    }

}
