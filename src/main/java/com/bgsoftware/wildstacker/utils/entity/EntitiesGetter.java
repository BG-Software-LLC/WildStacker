package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.cache.TTLCache;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class EntitiesGetter {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final int REMOVE_TIME = 5;

    private static final ConcurrentLinkedQueue<EntityGetInformation> pendingRequests = new ConcurrentLinkedQueue<>();

    private static final TTLCache<ChunkPosition, CompletableFuture<EntityBox>> cachedEntities = new TTLCache<>();

    private static BukkitTask currentTask = null;

    public static CompletableFuture<Set<Entity>> getNearbyEntities(Location location, int range, Predicate<Entity> filter){
        ChunkPosition chunkPosition = new ChunkPosition(location);
        CompletableFuture<EntityBox> completableFuture = null;
        boolean generateNew = true;

        Predicate<Entity> wrapperFilter = entity -> GeneralUtils.isNearby(location, entity.getLocation(), range) && filter.test(entity);

        if(currentTask == null){
            completableFuture = CompletableFuture.completedFuture(EntityBox.EMPTY_BOX);
            generateNew = false;
        }

        else if(cachedEntities.containsKey(chunkPosition)){
            CompletableFuture<EntityBox> entityBox = cachedEntities.get(chunkPosition);
            assert entityBox != null;
            if(!entityBox.isDone() || entityBox.getNow(null).anyMatch(wrapperFilter)) {
                cachedEntities.refreshTTL(chunkPosition, REMOVE_TIME);
                completableFuture = entityBox;
                generateNew = false;
            }
            else{
                cachedEntities.remove(chunkPosition);
            }
        }

        if(generateNew) {
            completableFuture = new CompletableFuture<>();
            int nearbyChunks = range % 16 == 0 ? range / 16 : (range / 16) + 1;
            pendingRequests.add(new EntityGetInformation(location, nearbyChunks, completableFuture));
            cachedEntities.put(chunkPosition, completableFuture, -1);
        }

        return completableFuture.thenApply(nearbyEntities -> nearbyEntities.filter(wrapperFilter).collect(Collectors.toSet()));
    }

    public static void start(){
        stop();
        currentTask = createNewTask();
    }

    public static void stop(){
        if(currentTask != null) {
            currentTask.cancel();
            currentTask = null;
            pendingRequests.clear();
            cachedEntities.clear();
        }
    }

    public static int size(){
        return cachedEntities.size();
    }

    public static void removeCache(Chunk chunk){
        ChunkPosition chunkPosition = new ChunkPosition(chunk);
        CompletableFuture<EntityBox> completableFuture = cachedEntities.remove(chunkPosition);
        if(completableFuture != null)
            completableFuture.whenComplete((entityBox, throwable) -> entityBox.clear());
    }

    private static BukkitTask createNewTask() {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            while (!pendingRequests.isEmpty()){
                EntityGetInformation info = pendingRequests.poll();

                if (info == null)
                    break;

                World world = info.location.getWorld();
                int chunkX = info.location.getBlockX() >> 4, chunkZ = info.location.getBlockZ() >> 4;

                EntityBox entityBox = new EntityBox();

                for(int x = -info.nearbyChunks; x <= info.nearbyChunks; x++){
                    for(int z = -info.nearbyChunks; z <= info.nearbyChunks; z++){
                        Chunk chunk = world.getChunkAt(chunkX + x, chunkZ + z);
                        entityBox.feed(Arrays.asList(chunk.getEntities()));
                    }
                }

                ChunkPosition chunkPosition = new ChunkPosition(world, chunkX, chunkZ);
                cachedEntities.put(chunkPosition, info.completableFuture, REMOVE_TIME);

                info.completableFuture.complete(entityBox);
            }
        }, 5L, 5L);
    }

    private static final class ChunkPosition{

        private final String world;
        private final int x, z;

        ChunkPosition(Chunk chunk){
            this.world = chunk.getWorld().getName();
            this.x = chunk.getX();
            this.z = chunk.getZ();
        }

        ChunkPosition(Location location){
            this.world = location.getWorld().getName();
            this.x = location.getBlockX() >> 4;
            this.z = location.getBlockZ() >> 4;
        }

        ChunkPosition(World world, int x, int z){
            this.world = world.getName();
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkPosition that = (ChunkPosition) o;
            return x == that.x &&
                    z == that.z &&
                    world.equals(that.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, x, z);
        }
    }

    private static final class EntityGetInformation {

        private final Location location;
        private final int nearbyChunks;
        private final CompletableFuture<EntityBox> completableFuture;

        EntityGetInformation(Location location, int nearbyChunks, CompletableFuture<EntityBox> completableFuture){
            this.location = location;
            this.nearbyChunks = nearbyChunks;
            this.completableFuture = completableFuture;
        }

    }

}
