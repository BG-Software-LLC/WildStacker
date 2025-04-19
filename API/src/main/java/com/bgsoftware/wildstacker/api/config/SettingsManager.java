package com.bgsoftware.wildstacker.api.config;

import com.bgsoftware.wildstacker.api.data.structures.*;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackSplit;
import com.bgsoftware.wildstacker.api.names.DisplayNameBuilder;
import com.bgsoftware.wildstacker.api.objects.*;
import com.bgsoftware.wildstacker.api.particles.ParticleEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface SettingsManager {

    /**
     * All settings related to general behavior and tools.
     * Config path: root-level (global)
     */
    Global getGlobal();

    /**
     * All settings related to the database of the plugin.
     * Config path: database
     */
    Database getDatabase();

    /**
     * All settings related to item stacking.
     * Config path: items
     */
    Items getItems();

    /**
     * All settings related to entity stacking.
     * Config path: entities
     */
    Entities getEntities();

    /**
     * All settings related to spawner stacking.
     * Config path: spawners
     */
    Spawners getSpawners();

    /**
     * All settings related to barrel stacking.
     * Config path: barrels
     */
    Barrels getBarrels();

    /**
     * All settings related to bucket stacking.
     * Config path: buckets
     */
    Buckets getBuckets();

    /**
     * All settings related to stew stacking.
     * Config path: stews
     */
    Stews getStews();

    /**
     * Settings related to the kill task.
     * Config path: kill-task
     */
    KillTask getKillTask();

    /**
     * Settings related to spawner upgrades.
     * Config path: spawners.spawner-upgrades
     */
    SpawnerUpgrades getSpawnerUpgrades();

    /**
     * Custom name overrides for entities/items.
     * Config path: custom-names.yml
     */
    NameOverrides getNameOverrides();

    /**
     * Entity stacking checks.
     * Config path: entities.stack-checks
     */
    StackChecks getStackChecks();

    /**
     * Entity stack splitting behavior.
     * Config path: entities.stack-split
     */
    StackSplits getStackSplits();

    interface Global {

        /**
         * Tool used to inspect stacked blocks/entities.
         * Config-path: inspect-tool
         */
        ItemStack getInspectTool();

        /**
         * Tool used to simulate placement.
         * Config-path: simulate-tool
         */
        ItemStack getSimulateTool();

        /**
         * Whether to remove data for invalid worlds.
         * Config-path: database.delete-invalid-worlds
         */
        boolean shouldDeleteInvalidWorlds();

        /**
         * The pattern used when giving stacked items via command.
         * Config-path: give-item-name
         */
        String getGiveItemNamePattern();
    }

    interface Database {

        /**
         * Get the database-type to use (SQLite or MySQL).
         * Config-path: database.type
         */
        String getType();

        /**
         * The address used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.address
         */
        String getAddress();

        /**
         * The port used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.port
         */
        int getPort();

        /**
         * Get the name of the database.
         * Used for MySQL only.
         * Config-path: database.db-name
         */
        String getDBName();

        /**
         * The username used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.user-name
         */
        String getUsername();

        /**
         * The password used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.password
         */
        String getPassword();

        /**
         * The prefix used for tables in the database.
         * Used for MySQL only.
         * Config-path: database.prefix
         */
        String getPrefix();

        /**
         * Whether the database uses SSL or not.
         * Used for MySQL only.
         * Config-path: database.useSSL
         */
        boolean hasSSL();

        /**
         * Whether public-key-retrieval is allowed in the database or not.
         * Used for MySQL only.
         * Config-path: database.allowPublicKeyRetrieval
         */
        boolean hasPublicKeyRetrieval();

        /**
         * The wait-timeout of the database, in milliseconds.
         * Used for MySQL only.
         * Config-path: database.waitTimeout
         */
        long getWaitTimeout();

        /**
         * The max-lifetime of the database, in milliseconds.
         * Used for MySQL only.
         * Config-path: database.maxLifetime
         */
        long getMaxLifetime();
    }

    interface Items {

        /**
         * Whether item stacking is enabled.
         * Config-path: items.enabled
         */
        boolean isEnabled();

        /**
         * Whether particles are enabled for stacked items.
         * Config-path: items.particles
         */
        boolean hasParticles();

        /**
         * List of particle effects for stacked items.
         * Config-path: particles.yml > items
         */
        List<ParticleEffect> getParticles();

        /**
         * Whether the fix stack system is enabled.
         * Config-path: items.fix-stack
         */
        boolean isFixStackEnabled();

        /**
         * Whether item display (floating item) is enabled.
         * Config-path: items.item-display
         */
        boolean isDisplayEnabled();

        /**
         * Whether unstacked items should show a custom name.
         * Config-path: items.unstacked-custom-name
         */
        boolean isUnstackedCustomNameEnabled();

        /**
         * Whether the names-toggle system is enabled for items.
         * Config-path: items.names-toggle.enabled
         */
        boolean isNamesToggleEnabled();

        /**
         * Command used to toggle item names.
         * Config-path: items.names-toggle.command
         */
        String getNamesToggleCommand();

        /**
         * Whether pickup sound is enabled when stacking items.
         * Config-path: items.pickup-sound
         */
        boolean isPickupSoundEnabled();

        /**
         * Whether the maximum pickup delay should be used.
         * Config-path: items.max-pickup-delay
         */
        boolean isMaxPickupDelayEnabled();

        /**
         * Whether items should be stored (e.g., for persistence).
         * Config-path: items.store-items
         */
        boolean isStoreItemsEnabled();

        /**
         * List of worlds where item stacking is disabled.
         * Config-path: items.disabled-worlds
         */
        List<String> getDisabledWorlds();

        /**
         * List of blacklisted materials for stacking.
         * Config-path: items.blacklist
         */
        IFastEnumArray<Material> getBlacklistedItems();

        /**
         * List of whitelisted materials for stacking.
         * Config-path: items.whitelist
         */
        IFastEnumArray<Material> getWhitelistedItems();

        /**
         * Max number of stacked items allowed per chunk.
         * Config-path: items.chunk-limit
         */
        int getChunkLimit();

        /**
         * Custom name format for stacked items.
         * Config-path: items.custom-name
         */
        String getCustomName();

        /**
         * Builder used to generate item display names.
         * Based on the placeholders and format.
         */
        DisplayNameBuilder<StackedItem> getNameBuilder();

        /**
         * Merge radius for item stacking per material.
         * Config-path: items.merge-radius
         */
        IFastEnumMap<Material, Integer> getMergeRadius();

        /**
         * Limits per material for stacking.
         * Config-path: items.limits
         */
        IFastEnumMap<Material, Integer> getLimits();

        /**
         * Delay in ticks between stacking operations.
         * Config-path: items.stack-interval
         */
        long getStackInterval();
    }

    interface Entities {

        /**
         * Whether entity stacking is enabled.
         * Config-path: entities.enabled
         */
        boolean isEnabled();

        /**
         * Whether particle effects are enabled for entities.
         * Config-path: entities.particles
         */
        boolean hasParticles();

        /**
         * List of particle effects used.
         * Config-path: particles.yml (entities section)
         */
        List<ParticleEffect> getParticles();

        /**
         * Whether linked entities are enabled.
         * Config-path: entities.linked-entities.enabled
         */
        boolean isLinkedEntitiesEnabled();

        /**
         * Whether nerfed entities should teleport.
         * Config-path: entities.nerfed-entities.teleport
         */
        boolean isTeleportNerfedEntities();

        /**
         * Maximum distance for linked entities.
         * Config-path: entities.linked-entities.max-distance
         */
        int getLinkedEntitiesMaxDistance();

        /**
         * List of blacklisted entity types and spawn causes.
         * Config-path: entities.blacklist
         */
        IFast2EnumsArray<EntityType, SpawnCause> getBlacklistedEntities();

        /**
         * List of whitelisted entity types and spawn causes.
         * Config-path: entities.whitelist
         */
        IFast2EnumsArray<EntityType, SpawnCause> getWhitelistedEntities();

        /**
         * List of blacklisted names (regex patterns).
         * Config-path: entities.name-blacklist
         */
        List<Pattern> getBlacklistedNames();

        /**
         * List of instant kill mappings.
         * Config-path: entities.instant-kill
         */
        IFast3EnumsArray<EntityType, SpawnCause, EntityDamageEvent.DamageCause> getInstantKills();

        /**
         * Whitelist of nerfed entity types and spawn causes.
         * Config-path: entities.nerfed-entities.whitelist
         */
        IFast2EnumsArray<EntityType, SpawnCause> getNerfedWhitelist();

        /**
         * Blacklist of nerfed entity types and spawn causes.
         * Config-path: entities.nerfed-entities.blacklist
         */
        IFast2EnumsArray<EntityType, SpawnCause> getNerfedBlacklist();

        /**
         * Worlds where nerfing applies.
         * Config-path: entities.nerfed-entities.worlds
         */
        List<String> getNerfedWorlds();

        /**
         * Whether stack-down is enabled.
         * Config-path: entities.stack-down.enabled
         */
        boolean isStackDownEnabled();

        /**
         * Types affected by stack-down.
         * Config-path: entities.stack-down.stack-down-types
         */
        IFast2EnumsArray<EntityType, SpawnCause> getStackDownTypes();

        /**
         * Whether fire is kept after stacking.
         * Config-path: entities.keep-fire
         */
        boolean isKeepFireEnabled();

        /**
         * Whether MythicMobs custom names are used.
         * Config-path: entities.mythic-mobs-custom-name
         */
        boolean isMythicMobsCustomNameEnabled();

        /**
         * Types that keep lowest health.
         * Config-path: entities.keep-lowest-health
         */
        IFast2EnumsArray<EntityType, SpawnCause> getKeepLowestHealthTypes();

        /**
         * Whether stack after breeding is enabled.
         * Config-path: entities.stack-after-breed
         */
        boolean isStackAfterBreedEnabled();

        /**
         * Whether smart breeding is enabled.
         * Config-path: entities.smart-breeding.enabled
         */
        boolean isSmartBreedingEnabled();

        /**
         * Whether smart breeding consumes full inventory.
         * Config-path: entities.smart-breeding.consume-entire-inventory
         */
        boolean isSmartBreedingConsumeInventory();

        /**
         * Whether entity name tags are hidden.
         * Config-path: entities.hide-names
         */
        boolean isNamesHidden();

        /**
         * Whether name toggle is enabled.
         * Config-path: entities.names-toggle.enabled
         */
        boolean isNamesToggleEnabled();

        /**
         * The name toggle command.
         * Config-path: entities.names-toggle.command
         */
        String getNamesToggleCommand();

        /**
         * Whether fast kill is enabled.
         * Config-path: entities.fast-kill
         */
        boolean isFastKillEnabled();

        /**
         * Whether one-shot feature is enabled.
         * Config-path: entities.one-shot.enabled
         */
        boolean isOneShotEnabled();

        /**
         * List of tools used for one-shot.
         * Config-path: entities.one-shot.tools
         */
        List<String> getOneShotTools();

        /**
         * Whitelisted types for one-shot.
         * Config-path: entities.one-shot.whitelist
         */
        IFast2EnumsArray<EntityType, SpawnCause> getOneShotWhitelist();

        /**
         * Whether stacked entities are stored.
         * Config-path: entities.store-entities
         */
        boolean isStoreEntitiesEnabled();

        /**
         * Whether SuperiorSkyblock hook is enabled.
         * Config-path: entities.superiorskyblock-hook
         */
        boolean isSuperiorSkyblockHookEnabled();

        /**
         * Whether drop multiplier is enabled.
         * Config-path: entities.multiply-drops
         */
        boolean isMultiplyDropsEnabled();

        /**
         * Whether EXP multiplier is enabled.
         * Config-path: entities.multiply-exp
         */
        boolean isMultiplyExpEnabled();

        /**
         * Whether spread damage is enabled.
         * Config-path: entities.spread-damage
         */
        boolean isSpreadDamageEnabled();

        /**
         * Whether entities can fill vehicles.
         * Config-path: entities.entities-fill-vehicles
         */
        boolean isFillVehiclesEnabled();

        /**
         * The stack interval for entities.
         * Config-path: entities.stack-interval
         */
        long getStackInterval();

        /**
         * Chunk limit for entities.
         * Config-path: entities.chunk-limit
         */
        int getChunkLimit();

        /**
         * The custom name format.
         * Config-path: entities.custom-name
         */
        String getCustomName();

        /**
         * The name builder used for stacked entities.
         * Config-path: entities.custom-name
         */
        DisplayNameBuilder<StackedEntity> getNameBuilder();

        /**
         * EXP pickup sound.
         * Config-path: entities.exp-pickup-sound
         */
        Sound getExpPickupSound();

        /**
         * Worlds where entity stacking is disabled.
         * Config-path: entities.disabled-worlds
         */
        List<String> getDisabledWorlds();

        /**
         * Regions where entity stacking is disabled.
         * Config-path: entities.disabled-regions
         */
        List<String> getDisabledRegions();

        /**
         * Transform blacklisted types.
         * Config-path: entities.filtered-transforms
         */
        List<String> getFilteredTransforms();

        /**
         * Merge radius per entity type and spawn cause.
         * Config-path: entities.merge-radius
         */
        IFast2EnumsMap<EntityType, SpawnCause, Integer> getMergeRadius();

        /**
         * Stack limits per entity type and spawn cause.
         * Config-path: entities.limits
         */
        IFast2EnumsMap<EntityType, SpawnCause, Integer> getLimits();

        /**
         * Minimum required per entity type and spawn cause.
         * Config-path: entities.minimum-required
         */
        IFast2EnumsMap<EntityType, SpawnCause, Integer> getMinimumRequiredEntities();

        /**
         * Default unstack values.
         * Config-path: entities.default-unstack
         */
        IFast2EnumsMap<EntityType, SpawnCause, Integer> getDefaultUnstack();

        /**
         * Types for auto EXP pickup.
         * Config-path: entities.auto-exp-pickup
         */
        IFast2EnumsArray<EntityType, SpawnCause> getAutoExpPickupTypes();

        /**
         * Whether egg-laying multiplier is enabled.
         * Config-path: entities.egg-lay-multiply
         */
        boolean isEggLayMultiplyEnabled();

        /**
         * Whether scute multiplier is enabled.
         * Config-path: entities.scute-multiply
         */
        boolean isScuteMultiplyEnabled();

        /**
         * Whether entity equipment is cleared.
         * Config-path: entities.clear-equipment
         */
        boolean isClearEquipmentEnabled();

        /**
         * Whether corpses are spawned.
         * Config-path: entities.spawn-corpses
         */
        boolean isSpawnCorpsesEnabled();
    }

    interface Spawners {

        /**
         * Config-path: spawners.enabled
         */
        boolean isEnabled();

        /**
         * Config-path: spawners.per-spawner-limit
         */
        boolean isPerSpawnerLimit();

        /**
         * Config-path: spawners.particles
         */
        boolean hasParticles();

        /**
         * Config-path: particles.yml -> spawners
         */
        List<ParticleEffect> getParticles();

        /**
         * Config-path: spawners.chunk-merge
         */
        boolean isChunkMergeEnabled();

        /**
         * Config-path: spawners.blacklist
         */
        IFastEnumArray<EntityType> getBlacklistedSpawners();

        /**
         * Config-path: spawners.whitelist
         */
        IFastEnumArray<EntityType> getWhitelistedSpawners();

        /**
         * Config-path: spawners.chunk-limit
         */
        int getChunkLimit();

        /**
         * Config-path: spawners.custom-name
         */
        String getCustomName();

        /**
         * Config-path: spawners.custom-name
         */
        DisplayNameBuilder<StackedSpawner> getNameBuilder();

        /**
         * Config-path: spawners.spawner-item.name
         */
        String getItemName();

        Pattern getSpawnersPattern();

        /**
         * Config-path: spawners.spawner-item.lore
         */
        List<String> getItemLore();

        /**
         * Config-path: spawners.silk-touch.enabled
         */
        boolean isSilkTouchEnabled();

        /**
         * Config-path: spawners.silk-touch.drop-to-inventory
         */
        boolean isDropToInventoryEnabled();

        /**
         * Config-path: spawners.silk-touch.worlds
         */
        List<String> getSilkTouchWorlds();

        /**
         * Config-path: spawners.silk-touch.drop-without-silk
         */
        boolean isDropWithoutSilkEnabled();

        /**
         * Config-path: spawners.silk-touch.break-chance
         */
        int getSilkTouchBreakChance();

        /**
         * Config-path: spawners.silk-touch.minimum-level
         */
        int getSilkTouchMinimumLevel();

        /**
         * Config-path: spawners.explosions.enabled
         */
        boolean isExplosionDropEnabled();

        /**
         * Config-path: spawners.explosions.drop-to-inventory
         */
        boolean isExplosionDropToInventoryEnabled();

        /**
         * Config-path: spawners.explosions.worlds
         */
        List<String> getExplosionWorlds();

        /**
         * Config-path: spawners.explosions.break-chance
         */
        int getExplosionBreakChance();

        /**
         * Config-path: spawners.explosions-break-percentage
         */
        int getExplosionBreakPercentage();

        /**
         * Config-path: spawners.explosions-break-minimum
         */
        int getExplosionBreakMinimum();

        /**
         * Config-path: spawners.explosions-amount-percentage
         */
        int getExplosionAmountPercentage();

        /**
         * Config-path: spawners.explosions-amount-minimum
         */
        int getExplosionAmountMinimum();

        /**
         * Config-path: spawners.mine-require-silk
         */
        boolean isMineRequireSilk();

        /**
         * Config-path: spawners.shift-get-whole-stack
         */
        boolean isShiftGetWholeStackEnabled();

        /**
         * Config-path: spawners.drop-stacked-item
         */
        boolean isDropStackedItemEnabled();

        /**
         * Config-path: spawners.floating-names
         */
        boolean hasFloatingNames();

        /**
         * Config-path: spawners.placement-permission
         */
        boolean hasPlacementPermission();

        /**
         * Config-path: spawners.shift-place-stack
         */
        boolean isShiftPlaceStackEnabled();

        /**
         * Config-path: spawners.spawners-override.enabled
         */
        boolean isOverrideEnabled();

        /**
         * Config-path: spawners.listen-paper-pre-spawn-event
         */
        boolean isListenPaperPreSpawnEnabled();

        /**
         * Config-path: spawners.unstacked-custom-name
         */
        boolean hasUnstackedCustomName();

        /**
         * Config-path: spawners.merge-radius
         */
        IFastEnumMap<EntityType, Integer> getMergeRadius();

        /**
         * Config-path: spawners.limits
         */
        IFastEnumMap<EntityType, Integer> getLimits();

        /**
         * Config-path: spawners.disabled-worlds
         */
        List<String> getDisabledWorlds();

        /**
         * Config-path: spawners.inventory-tweaks.enabled
         */
        boolean isInventoryTweaksEnabled();

        /**
         * Config-path: spawners.inventory-tweaks.permission
         */
        String getInventoryTweaksPermission();

        /**
         * Config-path: spawners.inventory-tweaks.toggle-command
         */
        String getInventoryTweaksCommand();

        /**
         * Config-path: spawners.break-charge
         */
        IFastEnumMap<EntityType, Pair<Double, Boolean>> getBreakCharge();

        /**
         * Config-path: spawners.place-charge
         */
        IFastEnumMap<EntityType, Pair<Double, Boolean>> getPlaceCharge();

        // isNextSpawnerPlacement
        /**
         * Config-path: spawners.next-spawner-placement
         */
        boolean isNextSpawnerPlacementEnabled();

        /**
         * Config-path: spawners.only-one-spawner
         */
        boolean isOnlyOneSpawnerEnabled();

        /**
         * Config-path: spawners.change-using-eggs
         */
        boolean isChangeUsingEggsEnabled();

        /**
         * Config-path: spawners.eggs-stack-multiply
         */
        boolean isEggsStackMultiplyEnabled();

        /**
         * Config-path: spawners.manage-menu.amounts-menu or spawners.manage-menu.upgrade-menu
         */
        boolean isManageMenuEnabled();

        /**
         * Config-path: spawners.manage-menu.sneaking-open-menu
         */
        boolean isSneakingOpenMenuEnabled();

        /**
         * Config-path: spawners.manage-menu.amounts-menu
         */
        boolean isAmountsMenuEnabled();

        /**
         * Config-path: spawners.manage-menu.upgrade-menu
         */
        boolean isUpgradeMenuEnabled();

        /**
         * Config-path: spawners.spawner-upgrades.multiply-stack-amount
         */
        boolean isMultiplyStackAmountEnabled();

    }

    interface Barrels {

        /**
         * Whether barrels stacking is enabled.
         * Config-path: barrels.enabled
         */
        boolean isEnabled();

        /**
         * Whether particles are enabled for barrels.
         * Config-path: barrels.particles
         */
        boolean hasParticles();

        /**
         * List of particle effects for stacked barrels.
         * Config-path: particles.yml > barrels
         */
        List<ParticleEffect> getParticles();

        /**
         * Whether chunk merging is enabled for barrels.
         * Config-path: barrels.chunk-merge
         */
        boolean isChunkMergeEnabled();

        /**
         * The maximum number of stacked barrels per chunk.
         * Config-path: barrels.chunk-limit
         */
        int getChunkLimit();

        /**
         * Custom name format for stacked barrels.
         * Config-path: barrels.custom-name
         */
        String getCustomName();

        /**
         * Name builder for stacked barrels.
         */
        DisplayNameBuilder<StackedBarrel> getNameBuilder();

        /**
         * List of disabled worlds for barrels stacking.
         * Config-path: barrels.disabled-worlds
         */
        List<String> getDisabledWorlds();

        /**
         * List of blacklisted barrel materials.
         * Config-path: barrels.blacklist
         */
        IFastEnumArray<Material> getBlacklisted();

        /**
         * List of whitelisted barrel materials.
         * Config-path: barrels.whitelist
         */
        IFastEnumArray<Material> getWhitelisted();

        /**
         * Merge radius per material.
         * Config-path: barrels.merge-radius
         */
        int getMergeRadius(Material material);

        /**
         * Stack limit per material.
         * Config-path: barrels.limits
         */
        int getLimit(Material material);

        /**
         * Whether explosion breaks entire stack.
         * Config-path: barrels.explosions-break-stack
         */
        boolean isExplosionBreakStackEnabled();

        /**
         * Whether the toggle command is enabled.
         * Config-path: barrels.toggle-command.enabled
         */
        boolean isToggleCommandEnabled();

        /**
         * The syntax for the toggle command.
         * Config-path: barrels.toggle-command.command
         */
        String getToggleCommandSyntax();

        /**
         * Whether the place-inventory is enabled.
         * Config-path: barrels.place-inventory.enabled
         */
        boolean isPlaceInventoryEnabled();

        /**
         * The title of the place-inventory GUI.
         * Config-path: barrels.place-inventory.title
         */
        String getPlaceInventoryTitle();

        /**
         * Whether barrels are forced to be cauldrons.
         * Config-path: barrels.force-cauldron
         */
        boolean isCauldronForced();

        /**
         * Required permission to use barrels stacking.
         * Config-path: barrels.required-permission
         */
        String getRequiredPermission();

        /**
         * Whether auto-pickup is enabled.
         * Config-path: barrels.auto-pickup
         */
        boolean isAutoPickupEnabled();

        /**
         * Whether stacked items drop from barrels.
         * Config-path: barrels.drop-stacked-item
         */
        boolean isDropStackedItemEnabled();

        /**
         * Whether shift-placement of full stacks is allowed.
         * Config-path: barrels.shift-place-stack
         */
        boolean isShiftPlaceStackEnabled();
    }

    interface Buckets {
        /** Config-path: buckets.enabled */
        boolean isEnabled();

        /** Config-path: buckets.max-stack */
        int getMaxStack();

        /** Config-path: buckets.name-blacklist */
        List<String> getBlacklistedNames();
    }

    interface Stews {
        /** Config-path: stews.enabled */
        boolean isEnabled();

        /** Config-path: stews.max-stack */
        int getMaxStack();
    }

    interface KillTask {

        /**
         * The interval at which the kill task runs.
         * Config-path: kill-task.interval
         */
        long getInterval();

        /**
         * Whether stacked entities should be killed by the task.
         * Config-path: kill-task.stacked-entities
         */
        boolean isStackedEntitiesKillEnabled();

        /**
         * Whether unstacked entities should be killed by the task.
         * Config-path: kill-task.unstacked-entities
         */
        boolean isUnstackedEntitiesKillEnabled();

        /**
         * Whether stacked items should be killed by the task.
         * Config-path: kill-task.stacked-items
         */
        boolean isStackedItemsKillEnabled();

        /**
         * Whether unstacked items should be killed by the task.
         * Config-path: kill-task.unstacked-items
         */
        boolean isUnstackedItemsKillEnabled();

        /**
         * Whether the task should synchronize with ClearLag.
         * Config-path: kill-task.sync-clear-lagg
         */
        boolean isSyncWithClearLaggEnabled();

        /**
         * The command that displays time remaining until the next kill task.
         * Config-path: kill-task.time-command
         */
        String getTimeCommand();

        /**
         * The whitelist of entities to kill.
         * Config-path: kill-task.kill-entities.whitelist
         */
        IFast2EnumsArray<EntityType, SpawnCause> getEntitiesWhitelist();

        /**
         * The blacklist of entities to not kill.
         * Config-path: kill-task.kill-entities.blacklist
         */
        IFast2EnumsArray<EntityType, SpawnCause> getEntitiesBlacklist();

        /**
         * The worlds where entity killing should take place.
         * Config-path: kill-task.kill-entities.worlds
         */
        List<String> getEntitiesWorlds();

        /**
         * The whitelist of item types to kill.
         * Config-path: kill-task.kill-items.whitelist
         */
        IFastEnumArray<Material> getItemsWhitelist();

        /**
         * The blacklist of item types to exclude from killing.
         * Config-path: kill-task.kill-items.blacklist
         */
        IFastEnumArray<Material> getItemsBlacklist();

        /**
         * The worlds where item killing should take place.
         * Config-path: kill-task.kill-items.worlds
         */
        List<String> getItemsWorlds();
    }

    interface SpawnerUpgrades {
        /** Config-path: spawners.spawner-upgrades.multiply-stack-amount */
        boolean shouldMultiplyStackAmount();
    }

    interface NameOverrides {
        /** Config-path: custom-names.yml */
        Map<String, String> getOverrides();
    }

    interface StackChecks {
        /** Config-path: entities.stack-checks */
        boolean isCheckEnabled(StackCheckType check);
    }

    interface StackSplits {
        /** Config-path: entities.stack-split */
        boolean isSplitEnabled(StackSplit split);
    }
}
