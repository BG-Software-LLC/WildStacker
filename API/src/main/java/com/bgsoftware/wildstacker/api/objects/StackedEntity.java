package com.bgsoftware.wildstacker.api.objects;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface StackedEntity extends StackedObject<LivingEntity> {

    /**
     * Get the living-entity object of bukkit.
     */
    LivingEntity getLivingEntity();

    /**
     * Get the uuid of the living-entity.
     */
    UUID getUniqueId();

    /**
     * Get the entity-type of the living-entity.
     */
    EntityType getType();

    /**
     * Set the health to the living-entity.
     * @param health new health to set
     */
    void setHealth(double health);

    /**
     * Get the health of the living-entity.
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
     * @param stackedSpawner The spawner that spawned the entity.
     * @param result The entity that this entity was stacked into.
     */
    void runSpawnerStackAsync(StackedSpawner stackedSpawner, Consumer<Optional<LivingEntity>> result);

    /**
     * Try to stack the living-entity the same as if it was spawned by a spawner.
     * @param stackedSpawner the spawner
     * @return the entity it got stacked into. If none, null will be returned.
     *
     * @deprecated Should not be used. See runSpawnerStackAsync
     */
    @Deprecated
    LivingEntity trySpawnerStack(StackedSpawner stackedSpawner);

    /**
     * Spawn a duplicate entity with a specific stack amount.
     * @param amount the stack amount it will have
     * @return The stacked entity object of the new entity that was spawned
     */
    StackedEntity spawnDuplicate(int amount);

    /**
     * Spawn a corpse for this entity.
     */
    void spawnCorpse();

    /**
     * Get the drops of this entity with a specific fortune level.
     * SHOULD BE USED ASYNC OR MAY CAUSE LAG WITH LARGE STACK SIZES!!!
     * @param lootBonusLevel the fortune level
     */
    List<ItemStack> getDrops(int lootBonusLevel);

    /**
     * Get the drops of this entity with a specific fortune level and a stack size.
     * SHOULD BE USED ASYNC OR MAY CAUSE LAG WITH LARGE STACK SIZES!!!
     * @param lootBonusLevel the fortune level
     * @param stackAmount the stack size
     */
    List<ItemStack> getDrops(int lootBonusLevel, int stackAmount);

    /**
     * Set a temporary loot-table for this entity.
     * This loot table can be used one, and getDrops method will remove the temp loot-table.
     * @param itemStacks The loot to set
     */
    void setDrops(List<ItemStack> itemStacks);

    /**
     * Set a temporary loot-table for this entity.
     * This loot table can be used one, and getDrops method will remove the temp loot-table.
     * @param itemStacks The loot to set
     *
     * @deprecated see setDrops
     */
    @Deprecated
    void setTempLootTable(List<ItemStack> itemStacks);

    /**
     * Set a multiplier for loot. It will multiply all the drops by the given multiplier.
     * @param multiplier The multiplier
     */
    void setDropsMultiplier(int multiplier);

    /**
     * Set a multiplier for loot. It will multiply all the drops by the given multiplier.
     * @param multiplier The multiplier
     *
     * @deprecated See setDropsMultiplier
     */
    @Deprecated
    void setLootMultiplier(int multiplier);

    /**
     * Get the exp of this entity with a stack size.
     * @param stackAmount the stack size
     * @param defaultExp the default exp to return if not found
     */
    int getExp(int stackAmount, int defaultExp);

    /**
     * Ignore the death event of this entity.
     * Should be used if you want to override the behaviour of the entity.
     *
     * @deprecated No longer supported.
     */
    @Deprecated
    void ignoreDeathEvent();

    /**
     * Check if the entity should ignore the death event upon death.
     * @return True if death event is ignored, otherwise false
     *
     * @deprecated No longer supported.
     */
    @Deprecated
    boolean isIgnoreDeathEvent();

    /**
     * Returns the spawn cause of this entity
     */
    SpawnCause getSpawnCause();

    /**
     * Set the spawn cause of the entity.
     * @param spawnCause The spawn cause
     */
    void setSpawnCause(SpawnCause spawnCause);

    /**
     * Checks if the entity is nerfed or not.
     * @return True if entity is nerfed, otherwise false.
     */
    boolean isNerfed();

    /**
     * Set whether the entity should be nerfed or not.
     */
    void setNerfed(boolean nerfed);

    /**
     * Set whether the entity should be nerfed or not using the isNerfed flag.
     */
    void updateNerfed();

    /**
     * Checks if the name of the entity is blacklisted.
     * @return True if the name is blacklisted, otherwise false.
     */
    boolean isNameBlacklisted();

}
