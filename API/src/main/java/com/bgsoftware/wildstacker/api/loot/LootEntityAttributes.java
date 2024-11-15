package com.bgsoftware.wildstacker.api.loot;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nullable;

public interface LootEntityAttributes {

    /**
     * Create a custom {@link Builder} for the given entity type.
     *
     * @param entityType The type of the entity to mimic.
     * @return The new {@link Builder}
     */
    static LootEntityAttributes.Builder newBuilder(EntityType entityType) {
        return WildStackerAPI.getWildStacker().getSystemManager().createLootContextBuilder(entityType);
    }

    /**
     * Create a {@link Builder} for the given entity.
     * You can overwrite its attributes using the returned {@link Builder}
     *
     * @param stackedEntity The entity to get the attributes from.
     * @return The new {@link Builder}
     */
    static LootEntityAttributes.Builder newBuilder(StackedEntity stackedEntity) {
        return WildStackerAPI.getWildStacker().getSystemManager().createLootContextBuilder(stackedEntity);
    }

    /**
     * Create a {@link Builder} for the given entity.
     * You can overwrite its attributes using the returned {@link Builder}
     *
     * @param livingEntity The entity to get the attributes from.
     * @return The new {@link Builder}
     */
    static LootEntityAttributes.Builder newBuilder(LivingEntity livingEntity) {
        return WildStackerAPI.getWildStacker().getSystemManager().createLootContextBuilder(livingEntity);
    }

    /**
     * Get the type of the entity.
     */
    EntityType getEntityType();

    /**
     * Get the killer of the entity, if exists.
     */
    @Nullable
    LootEntityAttributes getKiller();

    /**
     * Whether to ignore killer related checks when filtering out drops.
     */
    boolean isIgnoreEntityKiller();

    /**
     * Get the upgrade of the entity, if exists.
     */
    @Nullable
    SpawnerUpgrade getUpgrade();

    /**
     * Whether to ignore upgrades related checks when filtering out drops.
     */
    boolean isIgnoreUpgrade();

    /**
     * Get the spawn-cause of the entity, if exists.
     */
    @Nullable
    SpawnCause getSpawnCause();

    /**
     * Whether to ignore spawn-cause related checks when filtering out drops.
     */
    boolean isIgnoreSpawnCause();

    /**
     * Get the death-cause of the entity, if exists.
     */
    @Nullable
    EntityDamageEvent.DamageCause getDeathCause();

    /**
     * Whether to ignore death-cause related checks when filtering out drops.
     */
    boolean isIgnoreDeathCause();

    /**
     * Check whether the entity is burning.
     */
    boolean isBurning();

    /*
     * SLIME ONLY
     */

    /**
     * Get the slime size of the entity.
     *
     * @throws UnsupportedOperationException If the entity is not a Slime or a Magma Cube.
     */
    int getSlimeSize();

    /**
     * Whether to ignore slime-size related checks when filtering out drops.
     *
     * @throws UnsupportedOperationException If the entity is not a Slime or a Magma Cube.
     */
    boolean isIgnoreSlimeSize();

    /*
     * CREEPER ONLY
     */

    /**
     * Get whether the creeper is powered.
     *
     * @throws UnsupportedOperationException If the entity is not a Creeper.
     */
    boolean isCreeperCharged();

    /**
     * Whether to ignore charged related checks when filtering out drops.
     *
     * @throws UnsupportedOperationException If the entity is not a Creeper.
     */
    boolean isIgnoreCreeperCharged();

    /*
     * PILLAGER/VINDICATOR/EVOKER ONLY
     */

    /**
     * Get whether the entity is a raid captain.
     *
     * @throws UnsupportedOperationException If the entity is not a pillager, vindicator or evoker.
     */
    boolean isRaidCaptain();

    /**
     * Whether to ignore raid captain related checks when filtering out drops.
     *
     * @throws UnsupportedOperationException If the entity is not a pillager, vindicator or evoker.
     */
    boolean isIgnoreRaidCaptain();

    /**
     * Builder class to build a new {@link LootEntityAttributes} object.
     * Can be obtained by calling {@link #newBuilder(EntityType)} or {@link #newBuilder(StackedEntity)}
     */
    interface Builder {

        /**
         * Set the attributes of the entity's killer.
         *
         * @param killerEntityAttributes The killer entity attributes.
         */
        Builder setKiller(LootEntityAttributes killerEntityAttributes);

        /**
         * Set whether to ignore killer related checks when filtering out drops.
         *
         * @param ignoreEntityKiller Whether to ignore killer related checks.
         */
        Builder setIgnoreEntityKiller(boolean ignoreEntityKiller);

        /**
         * Set the upgrade of the entity.
         *
         * @param upgrade The upgrade of the entity.
         */
        Builder setUpgrade(SpawnerUpgrade upgrade);

        /**
         * Set whether to ignore upgrade related checks when filtering out drops.
         *
         * @param ignoreUpgrade Whether to ignore upgrade related checks.
         */
        Builder setIgnoreUpgrade(boolean ignoreUpgrade);

        /**
         * Set the spawn-cause of the entity.
         *
         * @param spawnCause The spawn-cause of the entity.
         */
        Builder setSpawnCause(SpawnCause spawnCause);

        /**
         * Set whether to ignore spawn-cause related checks when filtering out drops.
         *
         * @param ignoreSpawnCause Whether to ignore spawn-cause related checks.
         */
        Builder setIgnoreSpawnCause(boolean ignoreSpawnCause);

        /**
         * Set the death-cause of the entity.
         *
         * @param deathCause The death-cause of the entity.
         */
        Builder setDeathCause(EntityDamageEvent.DamageCause deathCause);

        /**
         * Set whether to ignore death-cause related checks when filtering out drops.
         *
         * @param ignoreDeathCause Whether to ignore death-cause related checks.
         */
        Builder setIgnoreDeathCause(boolean ignoreDeathCause);

        /**
         * Set whether the entity is burning.
         *
         * @param burning Whether the entity is burning.
         */
        Builder setBurning(boolean burning);

        /**
         * Set the slime-size of the entity.
         *
         * @param slimeSize The slime-size of the entity.
         * @throws UnsupportedOperationException If the entity is not a Slime or a Magma Cube
         */
        Builder setSlimeSize(int slimeSize);

        /**
         * Set whether to ignore slime-size related checks when filtering out drops.
         *
         * @param ignoreSlimeSize Whether to ignore slime-size related checks.
         * @throws UnsupportedOperationException If the entity is not a slime or a Magma Cube
         */
        Builder setIgnoreSlimeSize(boolean ignoreSlimeSize);

        /**
         * Set whether the creeper is powered.
         *
         * @param charged Whether the creeper is charged.
         * @throws UnsupportedOperationException If the entity is not a Creeper.
         */
        Builder setCreeperCharged(boolean charged);

        /**
         * Set whether to ignore charged related checks when filtering out drops.
         *
         * @param ignoreCharged Whether to ignore charged related checks.
         * @throws UnsupportedOperationException If the entity is not a Creeper.
         */
        Builder setIgnoreCreeperCharged(boolean ignoreCharged);

        /**
         * Set whether the entity is raid captain.
         *
         * @param captain Whether the entity is raid captain.
         * @throws UnsupportedOperationException If the entity is not a Pillager, Vindicator or Evoker.
         */
        Builder setRaidCaptain(boolean captain);

        /**
         * Set whether to ignore raid captain related checks when filtering out drops.
         *
         * @param ignoreRaidCaptain Whether to ignore raid captain related checks.
         * @throws UnsupportedOperationException If the entity is not a Pillager, Vindicator or Evoker.
         */
        Builder setIgnoreRaidCaptain(boolean ignoreRaidCaptain);

        /**
         * Create a new {@link LootEntityAttributes} out of this builder.
         */
        LootEntityAttributes build();

    }

}
