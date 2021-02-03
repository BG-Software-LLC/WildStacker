package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.data.structures.Fast2EnumsArray;
import com.bgsoftware.wildstacker.utils.data.structures.Fast2EnumsMap;
import com.bgsoftware.wildstacker.utils.data.structures.Fast3EnumsArray;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class GeneralUtils {

    private static final NumberFormat numberFormatter = DecimalFormat.getNumberInstance();

    static {
        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumFractionDigits(2);
        numberFormatter.setMaximumFractionDigits(2);
        numberFormatter.setRoundingMode(RoundingMode.FLOOR);
    }

    public static boolean contains(Fast2EnumsArray<EntityType, SpawnCause> fast2EnumsArray, StackedEntity stackedEntity){
        return fast2EnumsArray.contains(stackedEntity.getType(), stackedEntity.getSpawnCause());
    }

    public static boolean contains(Fast3EnumsArray<EntityType, SpawnCause, EntityDamageEvent.DamageCause> fast3EnumsArray,
                                   StackedEntity stackedEntity, EntityDamageEvent.DamageCause damageCause){
        return fast3EnumsArray.containsFirst(stackedEntity.getType(), stackedEntity.getSpawnCause()) ||
                fast3EnumsArray.containsSecond(stackedEntity.getType(), damageCause);
    }

    public static boolean containsOrEmpty(Fast2EnumsArray<EntityType, SpawnCause> fast2EnumsArray, StackedEntity stackedEntity){
        return fast2EnumsArray.size() == 0 || contains(fast2EnumsArray, stackedEntity);
    }

    public static boolean containsOrEmpty(FastEnumArray<Material> fastEnumArray, Material itemType){
        return fastEnumArray.size() == 0 || fastEnumArray.contains(itemType);
    }

    public static boolean containsOrEmpty(List<String> list, String element){
        return list.isEmpty() || element.isEmpty() || list.contains(element);
    }

    public static int get(Fast2EnumsMap<EntityType, SpawnCause, Integer> fast2EnumsMap, StackedEntity stackedEntity, int def){
        return fast2EnumsMap.getOrDefault(stackedEntity.getType(), stackedEntity.getSpawnCause(), def);
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
                distances.put(o1, distance(o1.getLocation(), origin));
            if(!distances.containsKey(o2))
                distances.put(o2, distance(o2.getLocation(), origin));

            return distances.get(o1).compareTo(distances.get(o2));
        });
    }

    public static <T extends Entity> Optional<T> getClosestBukkit(Location origin, Stream<T> objects){
        Map<T, Double> distances = new HashMap<>();
        return objects.min((o1, o2) -> {
            if(!distances.containsKey(o1))
                distances.put(o1, distance(o1.getLocation(), origin));
            if(!distances.containsKey(o2))
                distances.put(o2, distance(o2.getLocation(), origin));

            return distances.get(o1).compareTo(distances.get(o2));
        });
    }

    public static String format(double number){
        return numberFormatter.format(number);
    }

    public static Location getMiddleBlock(Location location){
        return new Location(
                location.getWorld(),
                location.getBlockX() + 0.5D,
                location.getBlockY() + 0.5D,
                location.getBlockZ() + 0.5D
        );
    }

    private static double distance(Location loc1, Location loc2){
        return loc1.getWorld() != loc2.getWorld() ? Double.POSITIVE_INFINITY : loc1.distanceSquared(loc2);
    }

}
