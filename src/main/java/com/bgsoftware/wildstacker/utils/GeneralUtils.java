package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.key.KeyMap;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class GeneralUtils {

    private static final NumberFormat numberFormant = DecimalFormat.getNumberInstance();

    public static boolean contains(List<String> list, StackedEntity stackedEntity){
        return list.contains(stackedEntity.getType().name()) ||  list.contains(stackedEntity.getSpawnCause().name()) ||
                list.contains(stackedEntity.getType().name() + ":" + stackedEntity.getSpawnCause().name()) ||
                list.contains("all") || list.contains("ALL");
    }

    public static boolean containsOrEmpty(List<String> list, StackedEntity stackedEntity){
        return list.isEmpty() || contains(list, stackedEntity);
    }

    public static boolean containsOrEmpty(List<String> list, String element){
        return list.isEmpty() || element.isEmpty() || list.contains(element);
    }

    public static boolean contains(List<String> list, StackedSpawner stackedSpawner){
        return list.contains(stackedSpawner.getSpawnedType().name()) || list.contains("all") || list.contains("ALL");
    }

    public static boolean containsOrEmpty(List<String> list, StackedSpawner stackedSpawner){
        return list.isEmpty() || contains(list, stackedSpawner);
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
        if(map.containsKey("ALL"))
            return map.get("ALL");
        return def;
    }

    public static boolean isSameChunk(Location location, Chunk chunk){
        return chunk.getX() == location.getBlockX() >> 4 && chunk.getZ() == location.getBlockZ() >> 4;
    }

    public static boolean isChunkLoaded(Location location){
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static <T extends StackedObject> Optional<T> getClosest(Location origin, Collection<T> objects){
        return getClosest(origin, objects.stream());
    }

    public static <T extends StackedObject> Optional<T> getClosest(Location origin, Stream<T> objects){
        Map<T, Double> distances = new HashMap<>();
        return objects.min((o1, o2) -> {
            if(!distances.containsKey(o1))
                distances.put(o1, o1.getLocation().distanceSquared(origin));
            if(!distances.containsKey(o2))
                distances.put(o2, o2.getLocation().distanceSquared(origin));

            return distances.get(o1).compareTo(distances.get(o2));
        });
    }

    public static String format(double number){
        return numberFormant.format(number);
    }

}
