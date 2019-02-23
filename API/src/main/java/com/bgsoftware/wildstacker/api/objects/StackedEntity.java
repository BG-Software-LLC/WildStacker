package com.bgsoftware.wildstacker.api.objects;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface StackedEntity extends StackedObject<LivingEntity> {

    /**
     * Get the living-entity object of bukkit.
     * @return living-entity
     */
    LivingEntity getLivingEntity();

    /**
     * Get the uuid of the living-entity.
     * @return uuid
     */
    UUID getUniqueId();

    /**
     * Get the entity-type of the living-entity.
     * @return entity-type
     */
    EntityType getType();

    /**
     * Set the health to the living-entity.
     * @param health new health to set
     */
    void setHealth(double health);

    /**
     * Get the health of the living-entity.
     * @return health
     */
    double getHealth();

    /**
     * Set a custom-name to the living-entity.
     * @param customName a new custom name
     */
    void setCustomName(String customName);

    /**
     * Set visible custom name flag to the living-entity.
     * @param visible should the custom name of the living-entity be visible or not
     */
    void setCustomNameVisible(boolean visible);

    /**
     * Try to stack the living-entity the same as if it was spawned by a spawner.
     * @param stackedSpawner the spawner
     * @return the entity it got stacked into. If none, null will be returned.
     */
    LivingEntity trySpawnerStack(StackedSpawner stackedSpawner);

    /**
     * Spawn a duplicate entity with a specific stack amount.
     * @param amount the stack amount it will have
     * @return The stacked entity object of the new entity that was spawned
     */
    StackedEntity spawnDuplicate(int amount);

    /**
     * Get the drops of this entity with a specific fortune level.
     * SHOULD BE USED ASYNC OR MAY CAUSE LAG WITH LARGE STACK SIZES!!!
     * @param lootBonusLevel the fortune level
     * @return The drops of the entity
     */
    List<ItemStack> getDrops(int lootBonusLevel);

    /**
     * Get the drops of this entity with a specific fortune level and a stack size.
     * SHOULD BE USED ASYNC OR MAY CAUSE LAG WITH LARGE STACK SIZES!!!
     * @param lootBonusLevel the fortune level
     * @param stackAmount the stack size
     * @return The drops of the entity
     */
    List<ItemStack> getDrops(int lootBonusLevel, int stackAmount);

    /**
     * Ignore the death event of this entity.
     * Should be used if you want to override the behaviour of the entity.
     */
    void ignoreDeathEvent();

    /**
     * Check if the entity should ignore the death event upon death.
     * @return True if death event is ignored, otherwise false
     */
    boolean isIgnoreDeathEvent();

}
