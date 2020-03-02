package com.bgsoftware.wildstacker.api.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface StackedSnapshot {

    /**
     * Get spawner information for a location.
     * @param location The location to get info about.
     * @return Entry that contains the stack size and the type of the spawner.
     */
    Map.Entry<Integer, EntityType> getStackedSpawner(Location location);

    /**
     * Checks if a location has spawner data.
     * @param location The location to check.
     * @return True if location is a valid spawner, otherwise false.
     */
    boolean isStackedSpawner(Location location);

    /**
     * Get barrel information for a location.
     * @param location The location to get info about.
     * @return Entry that contains the stack size and the type of the barrel.
     *
     * @deprecated See getStackedBarrelItem(Location)
     */
    @Deprecated
    Map.Entry<Integer, Material> getStackedBarrel(Location location);

    /**
     * Get barrel information for a location.
     * @param location The location to get info about.
     * @return Entry that contains the stack size and the item of the barrel.
     */
    Map.Entry<Integer, ItemStack> getStackedBarrelItem(Location location);

    /**
     * Checks if a location has barrel data.
     * @param location The location to check.
     * @return True if location is a valid barrel, otherwise false.
     */
    boolean isStackedBarrel(Location location);

    /**
     * Get all spawners inside the chunk.
     * @return A map with locations and entries about all spawners.
     */
    Map<Location, Map.Entry<Integer, EntityType>> getAllSpawners();

    /**
     * Get all barrels inside the chunk.
     * @return A map with locations and entries about all barrels.
     */
    Map<Location, Map.Entry<Integer, Material>> getAllBarrels();

    /**
     * Get all barrels inside the chunk.
     * @return A map with locations and entries about all barrels.
     */
    Map<Location, Map.Entry<Integer, ItemStack>> getAllBarrelsItems();

}
