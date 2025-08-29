package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.particles.ParticleEffect;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumMap;

import com.bgsoftware.wildstacker.utils.names.NameBuilder;
import org.bukkit.Material;

import java.util.List;

public class ItemsSection extends SettingsContainerHolder implements SettingsManager.Items {

    @Override
    public boolean isEnabled() {
        return getContainer().itemsStackingEnabled;
    }

    @Override
    public boolean hasParticles() {
        return getContainer().itemsParticlesEnabled;
    }

    @Override
    public List<ParticleEffect> getParticles() {
        return getContainer().itemsParticles;
    }

    @Override
    public boolean isFixStackEnabled() {
        return getContainer().itemsFixStackEnabled;
    }

    @Override
    public boolean isDisplayEnabled() {
        return getContainer().itemsDisplayEnabled;
    }

    @Override
    public boolean isUnstackedCustomNameEnabled() {
        return getContainer().itemsUnstackedCustomName;
    }

    @Override
    public boolean isNamesToggleEnabled() {
        return getContainer().itemsNamesToggleEnabled;
    }

    @Override
    public String getNamesToggleCommand() {
        return getContainer().itemsNamesToggleCommand;
    }

    @Override
    public boolean isPickupSoundEnabled() {
        return getContainer().itemsSoundEnabled;
    }

    @Override
    public boolean isMaxPickupDelayEnabled() {
        return getContainer().itemsMaxPickupDelay;
    }

    @Override
    public boolean isStoreItemsEnabled() {
        return getContainer().storeItems;
    }

    @Override
    public List<String> getDisabledWorlds() {
        return getContainer().itemsDisabledWorlds;
    }

    public FastEnumArray<Material> getBlacklistedItems() {
        return getContainer().blacklistedItems;
    }

    public FastEnumArray<Material> getWhitelistedItems() {
        return getContainer().whitelistedItems;
    }

    @Override
    public int getChunkLimit() {
        return getContainer().itemsChunkLimit;
    }

    @Override
    public String getCustomName() {
        return getContainer().itemsCustomName;
    }


    public NameBuilder<StackedItem> getNameBuilder() {
        return getContainer().itemsNameBuilder;
    }

    public FastEnumMap<Material, Integer> getMergeRadius() {
        return getContainer().itemsMergeRadius;
    }

    public FastEnumMap<Material, Integer> getLimits() {
        return getContainer().itemsLimits;
    }

    @Override
    public long getStackInterval() {
        return getContainer().itemsStackInterval;
    }
}
