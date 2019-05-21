package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class WStackedSnapshot implements StackedSnapshot {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final Map<Location, Map.Entry<Integer, EntityType>> stackedSpawners = new HashMap<>();
    private final Map<Location, Map.Entry<Integer, Material>> stackedBarrels = new HashMap<>();

    public WStackedSnapshot(Chunk chunk, boolean loadData){
        if(loadData)
            plugin.getDataHandler().loadChunkData(chunk, false);

        Iterator<StackedSpawner> stackedSpawnerIterator = plugin.getDataHandler().CACHED_SPAWNERS.iterator(chunk);
        while(stackedSpawnerIterator.hasNext()){
            StackedSpawner stackedSpawner = stackedSpawnerIterator.next();
            stackedSpawners.put(stackedSpawner.getLocation(), new Map.Entry<Integer, EntityType>() {
                @Override
                public Integer getKey() {
                    return stackedSpawner.getStackAmount();
                }

                @Override
                public EntityType getValue() {
                    return stackedSpawner.getSpawnedType();
                }

                @Override
                public EntityType setValue(EntityType value) {
                    throw new UnsupportedOperationException("Cannot use setValue");
                }
            });
        }

        Iterator<StackedBarrel> stackedBarrelIterator = plugin.getDataHandler().CACHED_BARRELS.iterator(chunk);
        while(stackedBarrelIterator.hasNext()){
            StackedBarrel stackedBarrel = stackedBarrelIterator.next();
            stackedBarrels.put(stackedBarrel.getLocation(), new Map.Entry<Integer, Material>() {
                @Override
                public Integer getKey() {
                    return stackedBarrel.getStackAmount();
                }

                @Override
                public Material getValue() {
                    return stackedBarrel.getType();
                }

                @Override
                public Material setValue(Material value) {
                    throw new UnsupportedOperationException("Cannot use setValue");
                }
            });
        }
    }

    @Override
    public Map.Entry<Integer, EntityType> getStackedSpawner(Location location) {
        return stackedSpawners.getOrDefault(location, new Map.Entry<Integer, EntityType>() {
            @Override
            public Integer getKey() {
                return 1;
            }

            @Override
            public EntityType getValue() {
                return EntityType.PIG;
            }

            @Override
            public EntityType setValue(EntityType value) {
                return null;
            }
        });
    }

    @Override
    public boolean isStackedSpawner(Location location) {
        return stackedSpawners.containsKey(location);
    }

    @Override
    public Map.Entry<Integer, Material> getStackedBarrel(Location location) {
        return stackedBarrels.getOrDefault(location, new Map.Entry<Integer, Material>() {
            @Override
            public Integer getKey() {
                return 1;
            }

            @Override
            public Material getValue() {
                return Material.AIR;
            }

            @Override
            public Material setValue(Material value) {
                return null;
            }
        });
    }

    @Override
    public boolean isStackedBarrel(Location location) {
        return stackedBarrels.containsKey(location);
    }

    @Override
    public Map<Location, Map.Entry<Integer, EntityType>> getAllSpawners() {
        return new HashMap<>(stackedSpawners);
    }

    @Override
    public Map<Location, Map.Entry<Integer, Material>> getAllBarrels() {
        return new HashMap<>(stackedBarrels);
    }
}
