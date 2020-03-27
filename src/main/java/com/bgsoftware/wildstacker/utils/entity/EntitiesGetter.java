package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.BiPair;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public final class EntitiesGetter {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final int REMOVE_TIME = 40;

    private static final ConcurrentLinkedQueue<BiPair<Location, Integer, CompletableFuture<Set<Entity>>>>
            pendingRequests = new ConcurrentLinkedQueue<>();

    private static Map<ChunkPosition, Set<Entity>> cachedEntities = new ConcurrentHashMap<>();
    private static Map<Integer, Set<ChunkPosition>> toRemoveByTick = new ConcurrentHashMap<>();
    private static Map<ChunkPosition, Integer> toRemoveByChunk = new ConcurrentHashMap<>();

    private static BukkitTask currentTask = null;
    private static int currentTick = 0;

    public static CompletableFuture<Set<Entity>> getNearbyEntities(Location location, int range){
        ChunkPosition chunkPosition = new ChunkPosition(location);
        CompletableFuture<Set<Entity>> completableFuture;

        if(currentTask == null){
            completableFuture = CompletableFuture.completedFuture(new HashSet<>());
        }

        else if(cachedEntities.containsKey(chunkPosition)){
            completableFuture = CompletableFuture.completedFuture(cachedEntities.get(chunkPosition).stream()
                    .filter(entity -> entity.isValid() && !entity.isDead()).collect(Collectors.toSet()));
            cacheChunk(chunkPosition);
        }

        else {
            completableFuture = new CompletableFuture<>();
            int nearbyChunks = range % 16 == 0 ? range / 16 : (range / 16) + 1;
            pendingRequests.add(new BiPair<>(location, nearbyChunks, completableFuture));
        }

        return completableFuture;
    }

    private static void cacheChunk(ChunkPosition chunkPosition){
        int chunkTick = toRemoveByChunk.get(chunkPosition);

        Set<ChunkPosition> chunkPositions = toRemoveByTick.get(chunkTick);
        if(chunkPositions != null) {
            chunkPositions.remove(chunkPosition);

            if (chunkPositions.isEmpty())
                toRemoveByTick.remove(chunkTick);
        }

        toRemoveByTick.computeIfAbsent(currentTick + REMOVE_TIME, s -> new HashSet<>()).add(chunkPosition);
    }

    private static BukkitTask createNewTask() {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            currentTick++;

            while (!pendingRequests.isEmpty()){
                BiPair<Location, Integer, CompletableFuture<Set<Entity>>> pair = pendingRequests.poll();

                if (pair == null)
                    break;

                Location location = pair.getX();
                int nearbyChunks = pair.getY();

                World world = location.getWorld();
                int chunkX = location.getBlockX() >> 4, chunkZ = location.getBlockZ() >> 4;

                Set<Entity> nearbyEntities = Collections.newSetFromMap(new ConcurrentHashMap<>());

                for(int x = -nearbyChunks; x <= nearbyChunks; x++){
                    for(int z = -nearbyChunks; z <= nearbyChunks; z++){
                        Chunk chunk = world.getChunkAt(chunkX + x, chunkZ + z);
                        nearbyEntities.addAll(Arrays.asList(chunk.getEntities()));
                    }
                }

                ChunkPosition chunkPosition = new ChunkPosition(world, chunkX, chunkZ);
                cachedEntities.put(chunkPosition, nearbyEntities);
                toRemoveByChunk.put(chunkPosition, currentTick + REMOVE_TIME);
                toRemoveByTick.computeIfAbsent(currentTick + REMOVE_TIME, s -> new HashSet<>());

                pair.getZ().complete(nearbyEntities);
            }

            if(toRemoveByTick.containsKey(currentTick)){
                toRemoveByTick.get(currentTick).forEach(chunkPosition -> {
                    cachedEntities.remove(chunkPosition);
                    toRemoveByChunk.remove(chunkPosition);
                });
                toRemoveByTick.remove(currentTick);
            }

        }, 5L, 5L);
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

    private static final class ChunkPosition{

        private final String world;
        private final int x, z;

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

}
