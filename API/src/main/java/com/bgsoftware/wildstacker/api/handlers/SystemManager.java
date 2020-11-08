package com.bgsoftware.wildstacker.api.handlers;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.loot.LootTable;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedBarrel;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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
     * Get the stacked spawner object of a creature-spawner by location.
     * @param location The location of the creature-spawner.
     * @return stacked spawner object
     */
    StackedSpawner getStackedSpawner(Location location);

    /**
     * Get the stacked barrel object of a block.
     * @param block The block
     * @return stacked barrel object
     */
    StackedBarrel getStackedBarrel(Block block);

    /**
     * Get the stacked barrel object of a block by location.
     * @param location The location of the block.
     * @return stacked barrel object
     */
    StackedBarrel getStackedBarrel(Location location);

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
     * Get all the stacked spawners from cache in a chunk.
     * @param chunk The chunk to retrieve spawners from.
     * @return A list of stacked spawners
     */
    List<StackedSpawner> getStackedSpawners(Chunk chunk);

    /**
     * Get all the stacked spawners from cache in a chunk.
     * @param world The world of the chunk.
     * @param chunkX The chunk's x-coords.
     * @param chunkZ The chunk's z-coords.
     * @return A list of stacked spawners
     */
    List<StackedSpawner> getStackedSpawners(World world, int chunkX, int chunkZ);

    /**
     * Get all the stacked spawners on the server.
     * @return A list of pairs for locations and stack amounts of the spawners.
     */
    List<UnloadedStackedSpawner> getAllStackedSpawners();

    /**
     * Get all the stacked barrels from cache.
     * @return A list of stacked barrels
     */
    List<StackedBarrel> getStackedBarrels();

    /**
     * Get all the stacked barrels from cache in a chunk.
     * @param chunk The chunk to retrieve barrels from.
     * @return A list of stacked barrels
     */
    List<StackedBarrel> getStackedBarrels(Chunk chunk);

    /**
     * Get all the stacked barrels from cache in a chunk.
     * @param world The world of the chunk.
     * @param chunkX The chunk's x-coords.
     * @param chunkZ The chunk's z-coords.
     * @return A list of stacked barrels
     */
    List<StackedBarrel> getStackedBarrels(World world, int chunkX, int chunkZ);

    /**
     * Get all the stacked barrels on the server.
     * @return A list of pairs for locations and stack amounts of the barrels.
     */
    List<UnloadedStackedBarrel> getAllStackedBarrels();

    /**
     * Checks if a block is a stacked spawner.
     * @return True if block is a stacked spawner, otherwise false
     */
    boolean isStackedSpawner(Block block);

    /**
     * Checks if a block is a stacked spawner.
     * @param location The location of the spawner.
     * @return True if block is a stacked spawner, otherwise false
     */
    boolean isStackedSpawner(Location location);

    /**
     * Checks if a block is a stacked barrel.
     * Unlike isStackedBlock(Location), this will also check if the block is a cauldron.
     * @return True if block is a stacked barrel, otherwise false
     */
    boolean isStackedBarrel(Block block);

    /**
     * Checks if a block is a stacked barrel.
     * @param location The location of the block.
     * @return True if block is a stacked barrel, otherwise false
     */
    boolean isStackedBarrel(Location location);

    /**
     * Performs a cache clear.
     */
    void performCacheClear();

    /**
     * Performs a cache save.
     */
    void performCacheSave();

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
     * @deprecated see spawnEntityWithoutStacking(Location location, Class<T> type, SpawnCause spawnCause)
     */
    @Deprecated
    <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, CreatureSpawnEvent.SpawnReason spawnReason);

    /**
     * Spawns a new entity without stacking it.
     * @param location the location to spawn the entity at
     * @param type the entity class type
     * @param spawnCause the spawn cause to be set in the entity
     * @return The new entity object
     */
    <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, SpawnCause spawnCause);

    /**
     * Drops an item with the amount of the item stack.
     * @param location the location to spawn the item at
     * @param itemStack the item to drop.
     * @return The new item object
     */
    StackedItem spawnItemWithAmount(Location location, ItemStack itemStack);

    /**
     * Drops an item with a specific amount for the stack.
     * @param location the location to spawn the item at
     * @param itemStack the item to drop.
     * @param amount the amount of the item to drop.
     * @return The new item object
     */
    StackedItem spawnItemWithAmount(Location location, ItemStack itemStack, int amount);

    /**
     * Spawn a corpse for a stacked entity.
     * @param stackedEntity the stacked entity.
     */
    void spawnCorpse(StackedEntity stackedEntity);

    /**
     * Perform a kill all.
     */
    void performKillAll();

    /**
     * Perform a kill all.
     */
    void performKillAll(boolean applyTaskFilter);

    /**
     * Perform a kill all.
     */
    void performKillAll(Predicate<Entity> entityPredicate, Predicate<Item> itemPredicate);

    /**
     * Perform a kill all.
     */
    void performKillAll(Predicate<Entity> entityPredicate, Predicate<Item> itemPredicate, boolean applyTaskFilter);

    /**
     * Get a loot-table from a living-entity.
     * @param livingEntity the living-entity
     * @return loot-table
     */
    LootTable getLootTable(LivingEntity livingEntity);

    /**
     * Get a stacked snapshot of a chunk.
     * @param chunk The chunk
     * @param loadData Should data be loaded if isn't already?
     * @return StackedSnapshot object
     *
     * @deprecated see getStackedSnapshot(Chunk chunk)
     */
    @Deprecated
    StackedSnapshot getStackedSnapshot(Chunk chunk, boolean loadData);

    /**
     * Get a stacked snapshot of a chunk.
     * @param chunk The chunk
     * @return StackedSnapshot object
     */
    StackedSnapshot getStackedSnapshot(Chunk chunk);

    /**
     * Check whether or not a player has item names toggled off.
     * @param player The player to check.
     */
    boolean hasItemNamesToggledOff(Player player);

    /**
     * Toggle item names for a player.
     * @param player The player to toggle for.
     */
    void toggleItemNames(Player player);

    /**
     * Check whether or not a player has entity names toggled off.
     * @param player The player to check.
     */
    boolean hasEntityNamesToggledOff(Player player);

    /**
     * Toggle entity names for a player.
     * @param player The player to toggle for.
     */
    void toggleEntityNames(Player player);

    /**
     * Add a spawn condition to entities.
     * @param spawnCondition The spawn condition to add.
     * @param entityTypes All the entity types to add the spawn condition to.
     */
    void addSpawnCondition(SpawnCondition spawnCondition, EntityType... entityTypes);

    /**
     * Get all the spawn conditions of an entity.
     * @param entityType The entity's type.
     */
    Collection<SpawnCondition> getSpawnConditions(EntityType entityType);

    /**
     * Remove a spawn condition from an entity.
     * @param entityType The entity's type
     * @param spawnCondition The spawn condition to remove.
     */
    void removeSpawnCondition(EntityType entityType, SpawnCondition spawnCondition);

    /**
     * Clear all the spawn conditions of an entity.
     * @param entityType The entity's type.
     */
    void clearSpawnConditions(EntityType entityType);

    /**
     * Get a spawn condition by it's id.
     * @param id The id of the spawn condition.
     */
    Optional<SpawnCondition> getSpawnCondition(String id);

    /**
     * Register a new spawn condition into the system.
     * If a spawn condition already exists with a similar id, the new one will override the old one.
     * @param spawnCondition The spawn condition to register.
     */
    SpawnCondition registerSpawnCondition(SpawnCondition spawnCondition);

}
