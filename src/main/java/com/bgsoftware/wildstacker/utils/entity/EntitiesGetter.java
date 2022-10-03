package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public final class EntitiesGetter {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final LoadingCache<ChunkPosition, Collection<Entity>> entitiesCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(new CacheLoader<ChunkPosition, Collection<Entity>>() {
                @Override
                public Collection<Entity> load(@NotNull ChunkPosition chunkPosition) {
                    return plugin.getNMSWorld().getEntitiesAtChunk(chunkPosition);
                }
            });

    public static void handleEntitySpawn(Entity entity) {
        ChunkPosition chunkPosition = new ChunkPosition(entity.getLocation());
        entitiesCache.getUnchecked(chunkPosition).add(entity);
    }

    public static Stream<Entity> getNearbyEntities(Location location, int range, Predicate<Entity> filter) {
        Collection<Entity> entities = plugin.getNMSWorld().getNearbyEntities(location, range, filter);

        if (entities != null) {
            return entities.stream();
        }

        int minX = location.getBlockX() - range;
        int minY = location.getBlockY() - range;
        int minZ = location.getBlockZ() - range;
        int maxX = location.getBlockX() + range;
        int maxY = location.getBlockY() + range;
        int maxZ = location.getBlockZ() + range;

        int minChunkX = minX >> 4, maxChunkX = maxX >> 4, minChunkZ = minZ >> 4, maxChunkZ = maxZ >> 4;

        String worldName = location.getWorld().getName();

        entities = new ArrayList<>();

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                entities.addAll(entitiesCache.getUnchecked(new ChunkPosition(worldName, x, z)));
            }
        }

        Location entityLocation = new Location(null, 0, 0, 0);

        return entities.stream().filter(entity ->
                isInRange(entity.getLocation(entityLocation), minX, minY, minZ, maxX, maxY, maxZ) &&
                        (filter == null || filter.test(entity))
        );
    }

    private static boolean isInRange(Location location, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

}
