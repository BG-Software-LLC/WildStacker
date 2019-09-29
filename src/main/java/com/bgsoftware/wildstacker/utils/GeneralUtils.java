package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.key.KeyMap;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.List;

public final class GeneralUtils {

    public static boolean contains(List<String> list, StackedEntity stackedEntity){
        return list.contains(stackedEntity.getType().name()) ||  list.contains(stackedEntity.getSpawnCause().name()) ||
                list.contains(stackedEntity.getType().name() + ":" + stackedEntity.getSpawnCause().name()) ||
                list.contains("all") || list.contains("ALL");
    }

    public static boolean containsOrEmpty(List<String> list, StackedEntity stackedEntity){
        return list.isEmpty() || contains(list, stackedEntity);
    }

    public static boolean contains(List<String> list, StackedSpawner stackedSpawner){
        return list.contains(stackedSpawner.getSpawnedType().name()) || list.contains("all") || list.contains("ALL");
    }

    public static int get(KeyMap<Integer> map, StackedEntity stackedEntity, int def){
        if(map.containsKey(stackedEntity.getType().name()))
            return map.get(stackedEntity.getType().name());
        if(map.containsKey(stackedEntity.getSpawnCause().name()))
            return map.get(stackedEntity.getSpawnCause().name());
        if(map.containsKey(stackedEntity.getType().name() + ":" + stackedEntity.getSpawnCause().name()))
            return map.get(stackedEntity.getType().name() + ":" + stackedEntity.getSpawnCause().name());
        if(map.containsKey("all"))
            return map.get("all");
        return def;
    }

    public static boolean isSameChunk(Location location, Chunk chunk){
        return chunk.getX() == location.getBlockX() >> 4 && chunk.getZ() == location.getBlockZ() >> 4;
    }

    public static boolean isChunkLoaded(Location location){
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static Location floor(Location location){
        return new Location(location.getWorld(), roundToBlock(location.getX()), location.getBlockY(), roundToBlock(location.getZ()));
    }

    private static int roundToBlock(double d){
        int fullNumber = (int) d;
        return d >= fullNumber + 0.7D ? fullNumber + 1 : d <= fullNumber + 0.3D ? fullNumber - 1 : fullNumber;
    }

}
