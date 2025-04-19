package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.api.data.structures.IFast2EnumsArray;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.names.DisplayNameBuilder;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.particles.ParticleEffect;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;
import com.bgsoftware.wildstacker.utils.data.structures.*;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.regex.Pattern;

public class EntitiesSection extends SettingsContainerHolder implements SettingsManager.Entities {

    @Override
    public boolean isEnabled() {
        return getContainer().entitiesStackingEnabled;
    }

    @Override
    public boolean hasParticles() {
        return getContainer().entitiesParticlesEnabled;
    }

    @Override
    public List<ParticleEffect> getParticles() {
        return (List<ParticleEffect>)(List<?>) getContainer().entitiesParticles;
    }

    @Override
    public boolean isLinkedEntitiesEnabled() {
        return getContainer().linkedEntitiesEnabled;
    }

    @Override
    public boolean isTeleportNerfedEntities() {
        return getContainer().nerfedEntitiesTeleport;
    }

    @Override
    public int getLinkedEntitiesMaxDistance() {
        return getContainer().linkedEntitiesMaxDistance;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getBlacklistedEntities() {
        return getContainer().blacklistedEntities;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getWhitelistedEntities() {
        return getContainer().whitelistedEntities;
    }

    @Override
    public List<Pattern> getBlacklistedNames() {
        return getContainer().blacklistedEntitiesNames;
    }

    @Override
    public Fast3EnumsArray<EntityType, SpawnCause, EntityDamageEvent.DamageCause> getInstantKills() {
        return getContainer().entitiesInstantKills;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getNerfedWhitelist() {
        return getContainer().entitiesNerfedWhitelist;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getNerfedBlacklist() {
        return getContainer().entitiesNerfedBlacklist;
    }

    @Override
    public List<String> getNerfedWorlds() {
        return getContainer().entitiesNerfedWorlds;
    }

    @Override
    public boolean isStackDownEnabled() {
        return getContainer().stackDownEnabled;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getStackDownTypes() {
        return getContainer().stackDownTypes;
    }

    @Override
    public boolean isKeepFireEnabled() {
        return getContainer().keepFireEnabled;
    }

    @Override
    public boolean isMythicMobsCustomNameEnabled() {
        return getContainer().mythicMobsCustomNameEnabled;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getKeepLowestHealthTypes() {
        return getContainer().keepLowestHealth;
    }

    @Override
    public boolean isStackAfterBreedEnabled() {
        return getContainer().stackAfterBreed;
    }

    @Override
    public boolean isSmartBreedingEnabled() {
        return getContainer().smartBreedingEnabled;
    }

    @Override
    public boolean isSmartBreedingConsumeInventory() {
        return getContainer().smartBreedingConsumeEntireInventory;
    }

    @Override
    public boolean isNamesHidden() {
        return getContainer().entitiesHideNames;
    }

    @Override
    public boolean isNamesToggleEnabled() {
        return getContainer().entitiesNamesToggleEnabled;
    }

    @Override
    public String getNamesToggleCommand() {
        return getContainer().entitiesNamesToggleCommand;
    }

    @Override
    public boolean isFastKillEnabled() {
        return getContainer().entitiesFastKill;
    }

    @Override
    public boolean isOneShotEnabled() {
        return getContainer().entitiesOneShotEnabled;
    }

    @Override
    public List<String> getOneShotTools() {
        return getContainer().entitiesOneShotTools;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getOneShotWhitelist() {
        return getContainer().entitiesOneShotWhitelist;
    }

    @Override
    public boolean isStoreEntitiesEnabled() {
        return getContainer().storeEntities;
    }

    @Override
    public boolean isSuperiorSkyblockHookEnabled() {
        return getContainer().superiorSkyblockHook;
    }

    @Override
    public boolean isMultiplyDropsEnabled() {
        return getContainer().multiplyDrops;
    }

    @Override
    public boolean isMultiplyExpEnabled() {
        return getContainer().multiplyExp;
    }

    @Override
    public boolean isSpreadDamageEnabled() {
        return getContainer().spreadDamage;
    }

    @Override
    public boolean isFillVehiclesEnabled() {
        return getContainer().entitiesFillVehicles;
    }

    @Override
    public long getStackInterval() {
        return getContainer().entitiesStackInterval;
    }

    @Override
    public int getChunkLimit() {
        return getContainer().entitiesChunkLimit;
    }

    @Override
    public String getCustomName() {
        return getContainer().entitiesCustomName;
    }

    @Override
    public DisplayNameBuilder<StackedEntity> getNameBuilder() {
        return getContainer().entitiesNameBuilder;
    }

    @Override
    public Sound getExpPickupSound() {
        return getContainer().entitiesExpPickupSound;
    }

    @Override
    public List<String> getDisabledWorlds() {
        return getContainer().entitiesDisabledWorlds;
    }

    @Override
    public List<String> getDisabledRegions() {
        return getContainer().entitiesDisabledRegions;
    }

    @Override
    public List<String> getFilteredTransforms() {
        return getContainer().entitiesFilteredTransforms;
    }

    @Override
    public Fast2EnumsMap<EntityType, SpawnCause, Integer> getMergeRadius() {
        return getContainer().entitiesMergeRadius;
    }

    @Override
    public Fast2EnumsMap<EntityType, SpawnCause, Integer> getLimits() {
        return getContainer().entitiesLimits;
    }

    @Override
    public Fast2EnumsMap<EntityType, SpawnCause, Integer> getMinimumRequiredEntities() {
        return getContainer().minimumRequiredEntities;
    }

    @Override
    public Fast2EnumsMap<EntityType, SpawnCause, Integer> getDefaultUnstack() {
        return getContainer().defaultUnstack;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getAutoExpPickupTypes() {
        return getContainer().entitiesAutoExpPickup;
    }

    @Override
    public boolean isEggLayMultiplyEnabled() {
        return getContainer().eggLayMultiply;
    }

    @Override
    public boolean isScuteMultiplyEnabled() {
        return getContainer().scuteMultiply;
    }

    @Override
    public boolean isClearEquipmentEnabled() {
        return getContainer().entitiesClearEquipment;
    }

    @Override
    public boolean isSpawnCorpsesEnabled() {
        return getContainer().spawnCorpses;
    }
}