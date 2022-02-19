package com.bgsoftware.wildstacker.api;

import com.bgsoftware.wildstacker.api.loot.LootTable;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class WildStackerAPI {

    private static WildStacker instance;

    /**
     * Set the plugin's instance for the API.
     * Do not use this method on your own, as it may cause an undefined behavior when using the API.
     *
     * @param instance The instance of the plugin to set to the API.
     */
    public static void setPluginInstance(WildStacker instance) {
        if (WildStackerAPI.instance != null) {
            throw new UnsupportedOperationException("You cannot initialize the plugin instance after it was initialized.");
        }

        WildStackerAPI.instance = instance;
    }

    /**
     * Get a stacked-item object for an item.
     *
     * @param item an item to check
     */
    public static StackedItem getStackedItem(Item item) {
        return instance.getSystemManager().getStackedItem(item);
    }

    /**
     * Get a stacked-amount for an item.
     *
     * @param item an item to check
     */
    public static int getItemAmount(Item item) {
        return getStackedItem(item).getStackAmount();
    }

    /**
     * Get a stacked-entity object for a living entity.
     *
     * @param livingEntity a living-entity to check
     */
    public static StackedEntity getStackedEntity(LivingEntity livingEntity) {
        return instance.getSystemManager().getStackedEntity(livingEntity);
    }

    /**
     * Get a stacked-amount for an entity.
     *
     * @param livingEntity an entity to check
     */
    public static int getEntityAmount(LivingEntity livingEntity) {
        return getStackedEntity(livingEntity).getStackAmount();
    }

    /**
     * Get a stacked-spawner object for a spawner.
     *
     * @param spawner a spawner to check
     */
    public static StackedSpawner getStackedSpawner(CreatureSpawner spawner) {
        return instance.getSystemManager().getStackedSpawner(spawner);
    }

    /**
     * Get a stacked-amount for a spawner.
     *
     * @param spawner a spawner to check
     */
    public static int getSpawnersAmount(CreatureSpawner spawner) {
        return getStackedSpawner(spawner).getStackAmount();
    }

    /**
     * Get a stacked-barrel object for a block.
     *
     * @param block a block to check
     */
    public static StackedBarrel getStackedBarrel(Block block) {
        return instance.getSystemManager().getStackedBarrel(block);
    }

    /**
     * Get a stacked-amount for a barrel.
     *
     * @param block a barrel to check
     */
    public static int getBarrelAmount(Block block) {
        return getStackedBarrel(block).getStackAmount();
    }

    /**
     * This method will spawn an entity without stacking it to another entity.
     *
     * @param location location to spawn the entity in
     * @param type     type of entity to spawn
     */
    public static Entity spawnEntityWithoutStacking(Location location, EntityType type) {
        return instance.getSystemManager().spawnEntityWithoutStacking(location, type.getEntityClass());
    }

    /**
     * Returns the loot loot of an entity.
     *
     * @param livingEntity An entity to check
     */
    public static LootTable getLootTable(LivingEntity livingEntity) {
        return instance.getSystemManager().getLootTable(livingEntity);
    }

    /**
     * Get the wildstacker object.
     */
    public static WildStacker getWildStacker() {
        return instance;
    }

}
