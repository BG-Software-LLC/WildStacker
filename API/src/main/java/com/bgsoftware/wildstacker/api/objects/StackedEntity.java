package com.bgsoftware.wildstacker.api.objects;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface StackedEntity extends AsyncStackedObject<LivingEntity>, UpgradeableStackedObject {

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
     * Get the health of the living-entity.
     */
    double getHealth();

    /**
     * Set the health to the living-entity.
     *
     * @param health new health to set
     */
    void setHealth(double health);

    /**
     * Get the custom name of the living-entity.
     */
    String getCustomName();

    /**
     * Set a custom-name to the living-entity.
     *
     * @param customName a new custom name
     */
    void setCustomName(String customName);

    /**
     * Check whether or not the name of the living entity is visible.
     */
    boolean isCustomNameVisible();

    /**
     * Set visible custom name flag to the living-entity.
     *
     * @param visible should the custom name of the living-entity be visible or not
     */
    void setCustomNameVisible(boolean visible);

    /**
     * Try to stack the living-entity the same as if it was spawned by a spawner.
     *
     * @param stackedSpawner The spawner that spawned the entity.
     * @param result         The entity that this entity was stacked into.
     */
    void runSpawnerStackAsync(StackedSpawner stackedSpawner, Consumer<Optional<LivingEntity>> result);

    /**
     * Try to stack the living-entity the same as if it was spawned by a spawner.
     *
     * @param stackedSpawner the spawner
     * @return the entity it got stacked into. If none, null will be returned.
     * @deprecated Should not be used. See runSpawnerStackAsync
     */
    @Deprecated
    LivingEntity trySpawnerStack(StackedSpawner stackedSpawner);

    /**
     * Spawn a duplicate entity with a specific stack amount.
     *
     * @param amount the stack amount it will have
     * @return The stacked entity object of the new entity that was spawned
     */
    StackedEntity spawnDuplicate(int amount);

    /**
     * Spawn a duplicate entity with a specific stack amount and a specific spawn cause.
     *
     * @param amount     the stack amount it will have
     * @param spawnCause The spawn cause of the new entity.
     * @return The stacked entity object of the new entity that was spawned
     */
    StackedEntity spawnDuplicate(int amount, SpawnCause spawnCause);

    /**
     * Spawn a corpse for this entity.
     */
    void spawnCorpse();

    /**
     * Get the drops of this entity with a specific fortune level.
     * SHOULD BE USED ASYNC OR MAY CAUSE LAG WITH LARGE STACK SIZES!!!
     *
     * @param lootBonusLevel the fortune level
     */
    List<ItemStack> getDrops(int lootBonusLevel);

    /**
     * Get the drops of this entity with a specific fortune level and a stack size.
     * SHOULD BE USED ASYNC OR MAY CAUSE LAG WITH LARGE STACK SIZES!!!
     *
     * @param lootBonusLevel the fortune level
     * @param stackAmount    the stack size
     */
    List<ItemStack> getDrops(int lootBonusLevel, int stackAmount);

    /**
     * Set a temporary loot-table for this entity.
     * This loot table can be used one, and getDrops method will remove the temp loot-table.
     *
     * @param itemStacks The loot to set
     */
    void setDrops(List<ItemStack> itemStacks);

    /**
     * Set a temporary loot-table for this entity.
     * This loot table can be used one, and getDrops method will remove the temp loot-table.
     *
     * @param itemStacks The loot to set
     * @deprecated see setDrops
     */
    @Deprecated
    void setTempLootTable(List<ItemStack> itemStacks);

    /**
     * Set a multiplier for loot. It will multiply all the drops by the given multiplier.
     *
     * @param multiplier The multiplier
     */
    void setDropsMultiplier(int multiplier);

    /**
     * Set a multiplier for loot. It will multiply all the drops by the given multiplier.
     *
     * @param multiplier The multiplier
     * @deprecated See setDropsMultiplier
     */
    @Deprecated
    void setLootMultiplier(int multiplier);

    /**
     * Get the exp of this entity with a stack size.
     *
     * @param stackAmount the stack size
     * @param defaultExp  the default exp to return if not found
     */
    int getExp(int stackAmount, int defaultExp);

    /**
     * Returns the spawn cause of this entity
     */
    SpawnCause getSpawnCause();

    /**
     * Set the spawn cause of the entity.
     *
     * @param spawnCause The spawn cause
     */
    void setSpawnCause(SpawnCause spawnCause);

    /**
     * Checks if the entity is nerfed or not.
     *
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
     */
    boolean isNameBlacklisted();

    /**
     * Checks if the entity should be instant-killed or not.
     *
     * @param damageCause The damage to check (can be null)
     */
    boolean isInstantKill(EntityDamageEvent.DamageCause damageCause);

    /**
     * Gets the default unstack amount for this entity.
     */
    int getDefaultUnstack();

    /**
     * Checks if entity has a name tag.
     */
    boolean hasNameTag();

    /**
     * Check if this entity has the flag in memory.
     *
     * @param entityFlag The flag to check.
     */
    boolean hasFlag(EntityFlag entityFlag);

    /**
     * Get the value associated with a requested flag.
     *
     * @param entityFlag The flag to check.
     */
    <T> T getFlag(EntityFlag entityFlag);

    /**
     * Set a value associated with a requested flag.
     *
     * @param entityFlag The flag to set.
     * @param value      The value to set.
     */
    void setFlag(EntityFlag entityFlag, Object value);

    /**
     * Remove a flag from memory.
     *
     * @param entityFlag The flag to set.
     */
    void removeFlag(EntityFlag entityFlag);

    /**
     * Get a flag and remove it afterward if it exists.
     *
     * @param entityFlag The flag to get and remove.
     */
    <T> T getAndRemoveFlag(EntityFlag entityFlag);

    /**
     * Clear all flags from this entity.
     */
    void clearFlags();

}
