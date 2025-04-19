package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.particles.ParticleEffect;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import com.bgsoftware.wildstacker.utils.names.NameBuilder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class BarrelsSection extends SettingsContainerHolder implements SettingsManager.Barrels {

    @Override
    public boolean isEnabled() {
        return getContainer().barrelsStackingEnabled;
    }

    @Override
    public boolean hasParticles() {
        return getContainer().barrelsParticlesEnabled;
    }

    @Override
    public List<ParticleEffect> getParticles() {
        return new ArrayList<>(getContainer().barrelsParticles);
    }

    @Override
    public boolean isChunkMergeEnabled() {
        return getContainer().chunkMergeBarrels;
    }

    @Override
    public int getChunkLimit() {
        return getContainer().barrelsChunkLimit;
    }

    @Override
    public String getCustomName() {
        return getContainer().barrelsCustomName;
    }

    @Override
    public NameBuilder<StackedBarrel> getNameBuilder() {
        return getContainer().barrelsNameBuilder;
    }

    @Override
    public List<String> getDisabledWorlds() {
        return getContainer().barrelsDisabledWorlds;
    }

    public FastEnumArray<Material> getBlacklisted() {
        return getContainer().blacklistedBarrels;
    }

    public FastEnumArray<Material> getWhitelisted() {
        return getContainer().whitelistedBarrels;
    }

    @Override
    public int getMergeRadius(Material material) {
        return getContainer().barrelsMergeRadius.getOrDefault(material, 0);
    }

    @Override
    public int getLimit(Material material) {
        return getContainer().barrelsLimits.getOrDefault(material, Integer.MAX_VALUE);
    }

    @Override
    public boolean isExplosionBreakStackEnabled() {
        return getContainer().explosionsBreakBarrelStack;
    }

    @Override
    public boolean isToggleCommandEnabled() {
        return getContainer().barrelsToggleCommand;
    }

    @Override
    public String getToggleCommandSyntax() {
        return getContainer().barrelsToggleCommandSyntax;
    }

    @Override
    public boolean isPlaceInventoryEnabled() {
        return getContainer().barrelsPlaceInventory;
    }

    @Override
    public String getPlaceInventoryTitle() {
        return getContainer().barrelsPlaceInventoryTitle;
    }

    @Override
    public boolean isCauldronForced() {
        return getContainer().forceCauldron;
    }

    @Override
    public String getRequiredPermission() {
        return getContainer().barrelsRequiredPermission;
    }

    @Override
    public boolean isAutoPickupEnabled() {
        return getContainer().barrelsAutoPickup;
    }

    @Override
    public boolean isDropStackedItemEnabled() {
        return getContainer().dropStackedItem;
    }

    @Override
    public boolean isShiftPlaceStackEnabled() {
        return getContainer().barrelsShiftPlaceStack;
    }
}
