package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.pair.MutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class EntitiesGetter {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final int REMOVE_TIME = 40;

    private static final ConcurrentLinkedQueue<EntityGetInformation> pendingRequests = new ConcurrentLinkedQueue<>();

    public static final Map<ChunkPosition, MutablePair<EntityBox, Integer>> cachedEntities = new ConcurrentHashMap<>();

    private static BukkitTask currentTask = null;
    private static int currentTick = 0;

    public static CompletableFuture<EntityBox> getNearbyEntities(Location location, int range, Predicate<Entity> filter){
        ChunkPosition chunkPosition = new ChunkPosition(location);
        CompletableFuture<EntityBox> completableFuture;

        if(currentTask == null){
            completableFuture = CompletableFuture.completedFuture(EntityBox.EMPTY_BOX);
        }

        else if(cachedEntities.containsKey(chunkPosition)){
            MutablePair<EntityBox, Integer> pair = cachedEntities.get(chunkPosition);
            pair.setValue(currentTick);
            completableFuture = CompletableFuture.completedFuture(pair.getKey());
        }

        else {
            completableFuture = new CompletableFuture<>();
            pendingRequests.add(new EntityGetInformation(location, range, filter, completableFuture));
        }

        return completableFuture;
    }

    private static BukkitTask createNewTask() {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            currentTick++;

            while (!pendingRequests.isEmpty()){
                EntityGetInformation info = pendingRequests.poll();

                if (info == null)
                    break;

                World world = info.location.getWorld();
                int chunkX = info.location.getBlockX() >> 4, chunkZ = info.location.getBlockZ() >> 4;

                EntityBox entityBox = new EntityBox(info.filter);

                int nearbyChunks = info.range % 16 == 0 ? info.range / 16 : (info.range / 16) + 1;

                for(int x = -nearbyChunks; x <= nearbyChunks; x++){
                    for(int z = -nearbyChunks; z <= nearbyChunks; z++){
                        Chunk chunk = world.getChunkAt(chunkX + x, chunkZ + z);
                        entityBox.feed(Arrays.stream(chunk.getEntities()).filter(entity ->
                                GeneralUtils.isNearby(info.location, entity.getLocation(), info.range)).collect(Collectors.toList()));
                    }
                }

                ChunkPosition chunkPosition = new ChunkPosition(world, chunkX, chunkZ);
                cachedEntities.put(chunkPosition, new MutablePair<>(entityBox, currentTick));

                info.completableFuture.complete(entityBox);
            }

            if(currentTick % REMOVE_TIME == 0){
                cachedEntities.entrySet().removeIf(entry -> currentTick - entry.getValue().getValue() <= REMOVE_TIME);
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

    private static final class EntityGetInformation {

        private final Location location;
        private final int range;
        private final Predicate<Entity> filter;
        private final CompletableFuture<EntityBox> completableFuture;

        EntityGetInformation(Location location, int range, Predicate<Entity> filter, CompletableFuture<EntityBox> completableFuture){
            this.location = location;
            this.range = range;
            this.filter = filter;
            this.completableFuture = completableFuture;
        }

    }

}
