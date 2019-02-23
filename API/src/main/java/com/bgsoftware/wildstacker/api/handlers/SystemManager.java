package com.bgsoftware.wildstacker.api.handlers;

import com.bgsoftware.wildstacker.api.loot.LootTable;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface SystemManager {

    /**
     * Remove a stacked object from cache.
     * @param stackedObject the stacked object
     */
    void removeStackObject(StackedObject stackedObject);

    /**
     * Get the stacked entity object of a living-entity.
     * @param livingEntity The living-entity
     * @return stacked entity object
     */
    StackedEntity getStackedEntity(LivingEntity livingEntity);

    /**
     * Get the stacked item object of an item.
     * @param item The item
     * @return stacked item object
     */
    StackedItem getStackedItem(Item item);

    /**
     * Get the stacked spawner object of a creature-spawner.
     * @param spawner The creature-spawner.
     * @return stacked spawner object
     */
    StackedSpawner getStackedSpawner(CreatureSpawner spawner);

    /**
     * Get the stacked barrel object of a block.
     * @param block The block
     * @return stacked barrel object
     */
    StackedBarrel getStackedBarrel(Block block);

    /**
     * Get all the stacked entities from cache.
     * @return A list of stacked entities
     */
    List<StackedEntity> getStackedEntities();

    /**
     * Get all the stacked items from cache.
     * @return A list of stacked items
     */
    List<StackedItem> getStackedItems();

    /**
     * Get all the stacked spawners from cache.
     * @return A list of stacked spawners
     */
    List<StackedSpawner> getStackedSpawners();

    /**
     * Get all the stacked barrels from cache.
     * @return A list of stacked barrels
     */
    List<StackedBarrel> getStackedBarrels();

    /**
     * Checks if a block is a stacked spawner.
     * @return True if block is a stacked spawner, otherwise false
     */
    boolean isStackedSpawner(Block block);

    /**
     * Checks if a block is a stacked barrel.
     * @return True if block is a stacked barrel, otherwise false
     */
    boolean isStackedBarrel(Block block);

    /**
     * Performs a cache clear.
     */
    void performCacheClear();

    /**
     * Updates all spawners that the entity is linked into.
     * @param livingEntity the entity that is already linked
     * @param newLivingEntity a new entity to be replaced with the old one
     */
    void updateLinkedEntity(LivingEntity livingEntity, LivingEntity newLivingEntity);

    /**
     * Spawns a new entity without stacking it.
     * The entity will have a default spawn reason of SPAWNER.
     * @param location the location to spawn the entity at
     * @param type the entity class type
     * @return The new entity object
     */
    <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type);

    /**
     * Spawns a new entity without stacking it..
     * @param location the location to spawn the entity at
     * @param type the entity class type
     * @param spawnReason the spawn reason to be set in the entity
     * @return The new entity object
     */
    <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, CreatureSpawnEvent.SpawnReason spawnReason);

    /**
     * Perform a kill all.
     */
    void performKillAll();

    /**
     * Get a loot-table from a living-entity.
     * @param livingEntity the living-entity
     * @return loot-table
     */
    LootTable getLootTable(LivingEntity livingEntity);

}
