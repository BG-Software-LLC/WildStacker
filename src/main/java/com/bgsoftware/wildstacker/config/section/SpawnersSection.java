package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.api.data.structures.IFastEnumMap;
import com.bgsoftware.wildstacker.api.names.DisplayNameBuilder;
import com.bgsoftware.wildstacker.api.objects.Pair;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.particles.ParticleEffect;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumMap;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SpawnersSection extends SettingsContainerHolder implements SettingsManager.Spawners {

    @Override
    public boolean isEnabled() {
        return getContainer().spawnersStackingEnabled;
    }

    @Override
    public boolean isPerSpawnerLimit() {
        return getContainer().perSpawnerLimit;
    }

    @Override
    public boolean hasParticles() {
        return getContainer().spawnersParticlesEnabled;
    }

    @Override
    public List<ParticleEffect> getParticles() {
        return  new ArrayList<>(getContainer().spawnersParticles);
    }

    @Override
    public boolean isChunkMergeEnabled() {
        return getContainer().chunkMergeSpawners;
    }

    @Override
    public FastEnumArray<EntityType> getBlacklistedSpawners() {
        return getContainer().blacklistedSpawners;
    }

    @Override
    public FastEnumArray<EntityType> getWhitelistedSpawners() {
        return getContainer().whitelistedSpawners;
    }

    @Override
    public int getChunkLimit() {
        return getContainer().spawnersChunkLimit;
    }

    @Override
    public String getCustomName() {
        return getContainer().spawnersCustomName;
    }

    @Override
    public DisplayNameBuilder<StackedSpawner> getNameBuilder() {
        return getContainer().spawnersNameBuilder;
    }

    @Override
    public String getItemName() {
        return getContainer().spawnerItemName;
    }

    @Override
    public Pattern getSpawnersPattern() {
        return getContainer().SPAWNERS_PATTERN;
    }

    @Override
    public List<String> getItemLore() {
        return getContainer().spawnerItemLore;
    }

    @Override
    public boolean isSilkTouchEnabled() {
        return getContainer().silkTouchSpawners;
    }

    @Override
    public boolean isDropToInventoryEnabled() {
        return getContainer().dropToInventory;
    }

    @Override
    public List<String> getSilkTouchWorlds() {
        return getContainer().silkWorlds;
    }

    @Override
    public boolean isDropWithoutSilkEnabled() {
        return getContainer().dropSpawnerWithoutSilk;
    }

    @Override
    public int getSilkTouchBreakChance() {
        return getContainer().silkTouchBreakChance;
    }

    @Override
    public int getSilkTouchMinimumLevel() {
        return getContainer().silkTouchMinimumLevel;
    }

    @Override
    public boolean isExplosionDropEnabled() {
        return getContainer().explosionsDropSpawner;
    }

    @Override
    public boolean isExplosionDropToInventoryEnabled() {
        return getContainer().explosionsDropToInventory;
    }

    @Override
    public List<String> getExplosionWorlds() {
        return getContainer().explosionsWorlds;
    }

    @Override
    public int getExplosionBreakChance() {
        return getContainer().explosionsBreakChance;
    }

    @Override
    public int getExplosionBreakPercentage() {
        return getContainer().explosionsBreakPercentage;
    }

    @Override
    public int getExplosionBreakMinimum() {
        return getContainer().explosionsBreakMinimum;
    }

    @Override
    public int getExplosionAmountPercentage() {
        return getContainer().explosionsAmountPercentage;
    }

    @Override
    public int getExplosionAmountMinimum() {
        return getContainer().explosionsAmountMinimum;
    }

    @Override
    public boolean isMineRequireSilk() {
        return getContainer().spawnersMineRequireSilk;
    }

    @Override
    public boolean isShiftGetWholeStackEnabled() {
        return getContainer().shiftGetWholeSpawnerStack;
    }

    @Override
    public boolean isDropStackedItemEnabled() {
        return getContainer().getStackedItem;
    }

    @Override
    public boolean hasFloatingNames() {
        return getContainer().floatingSpawnerNames;
    }

    @Override
    public boolean hasPlacementPermission() {
        return getContainer().spawnersPlacementPermission;
    }

    @Override
    public boolean isShiftPlaceStackEnabled() {
        return getContainer().spawnersShiftPlaceStack;
    }

    @Override
    public boolean isOverrideEnabled() {
        return getContainer().spawnersOverrideEnabled;
    }

    @Override
    public boolean isListenPaperPreSpawnEnabled() {
        return getContainer().listenPaperPreSpawnEvent;
    }

    @Override
    public boolean hasUnstackedCustomName() {
        return getContainer().spawnersUnstackedCustomName;
    }

    @Override
    public FastEnumMap<EntityType, Integer> getMergeRadius() {
        return getContainer().spawnersMergeRadius;
    }

    @Override
    public FastEnumMap<EntityType, Integer> getLimits() {
        return getContainer().spawnersLimits;
    }

    @Override
    public List<String> getDisabledWorlds() {
        return getContainer().spawnersDisabledWorlds;
    }

    @Override
    public boolean isInventoryTweaksEnabled() {
        return getContainer().inventoryTweaksEnabled;
    }

    @Override
    public String getInventoryTweaksPermission() {
        return getContainer().inventoryTweaksPermission;
    }

    @Override
    public String getInventoryTweaksCommand() {
        return getContainer().inventoryTweaksCommand;
    }

    @Override
    public IFastEnumMap<EntityType, Pair<Double, Boolean>> getBreakCharge() {
        return getContainer().spawnersBreakCharge;
    }

    @Override
    public IFastEnumMap<EntityType, Pair<Double, Boolean>> getPlaceCharge() {
        return getContainer().spawnersPlaceCharge;
    }

    @Override
    public boolean isNextSpawnerPlacementEnabled() {
        return getContainer().nextSpawnerPlacement;
    }

    @Override
    public boolean isOnlyOneSpawnerEnabled() {
        return getContainer().onlyOneSpawner;
    }

    @Override
    public boolean isChangeUsingEggsEnabled() {
        return getContainer().changeUsingEggs;
    }

    public boolean isEggsStackMultiplyEnabled() {
        return getContainer().eggsStackMultiply;
    }

    @Override
    public boolean isManageMenuEnabled() {
        return getContainer().manageMenuEnabled;
    }

    @Override
    public boolean isSneakingOpenMenuEnabled() {
        return  getContainer().sneakingOpenMenu;
    }

    @Override
    public boolean isAmountsMenuEnabled() {
        return  getContainer().amountsMenuEnabled;
    }

    @Override
    public boolean isUpgradeMenuEnabled() {
        return  getContainer().upgradeMenuEnabled;
    }

    @Override
    public boolean isMultiplyStackAmountEnabled() {
        return getContainer().spawnerUpgradesMultiplyStackAmount;
    }
}