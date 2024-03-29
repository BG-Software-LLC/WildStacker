package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class WStackedSnapshot implements StackedSnapshot {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final Map<Location, Pair<Integer, EntityType>> stackedSpawners = new HashMap<>();
    private final Map<Location, Pair<Integer, ItemStack>> stackedBarrels = new HashMap<>();

    public WStackedSnapshot(Chunk chunk, SnapshotOptions... snapshotOptions) {
        for (SnapshotOptions loadingOption : snapshotOptions) {
            switch (loadingOption) {
                case LOAD_SPAWNERS:
                    for (StackedSpawner stackedSpawner : plugin.getSystemManager().getStackedSpawners(chunk)) {
                        stackedSpawners.put(stackedSpawner.getLocation(), new Pair<>(stackedSpawner.getStackAmount(), stackedSpawner.getSpawnedType()));
                    }
                    break;
                case LOAD_BARRELS:
                    for (StackedBarrel stackedBarrel : plugin.getSystemManager().getStackedBarrels(chunk)) {
                        stackedBarrels.put(stackedBarrel.getLocation(), new Pair<>(stackedBarrel.getStackAmount(), stackedBarrel.getBarrelItem(1)));
                    }
                    break;
            }
        }
    }

    @Override
    public Map.Entry<Integer, EntityType> getStackedSpawner(Location location) {
        return stackedSpawners.getOrDefault(location, new Pair<>(1, null));
    }

    @Override
    public boolean isStackedSpawner(Location location) {
        return stackedSpawners.containsKey(location);
    }

    @Override
    public Map.Entry<Integer, Material> getStackedBarrel(Location location) {
        Map.Entry<Integer, ItemStack> pair = getStackedBarrelItem(location);
        return new Pair<>(pair.getKey(), pair.getValue().getType());
    }

    @Override
    public Map.Entry<Integer, ItemStack> getStackedBarrelItem(Location location) {
        return stackedBarrels.getOrDefault(location, new Pair<>(1, new ItemStack(Material.AIR)));
    }

    @Override
    public boolean isStackedBarrel(Location location) {
        return stackedBarrels.containsKey(location);
    }

    @Override
    public Map<Location, Map.Entry<Integer, EntityType>> getAllSpawners() {
        return Collections.unmodifiableMap(stackedSpawners);
    }

    @Override
    public Map<Location, Map.Entry<Integer, Material>> getAllBarrels() {
        return Collections.unmodifiableMap(getAllBarrelsItems().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new Pair<>(entry.getValue().getKey(), entry.getValue().getValue().getType())
        )));
    }

    @Override
    public Map<Location, Map.Entry<Integer, ItemStack>> getAllBarrelsItems() {
        return Collections.unmodifiableMap(stackedBarrels);
    }
}
