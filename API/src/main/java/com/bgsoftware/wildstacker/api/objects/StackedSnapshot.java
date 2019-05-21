package com.bgsoftware.wildstacker.api.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Map;

public interface StackedSnapshot {

    Map.Entry<Integer, EntityType> getStackedSpawner(Location location);

    boolean isStackedSpawner(Location location);

    Map.Entry<Integer, Material> getStackedBarrel(Location location);

    boolean isStackedBarrel(Location location);

    Map<Location, Map.Entry<Integer, EntityType>> getAllSpawners();

    Map<Location, Map.Entry<Integer, Material>> getAllBarrels();

}
