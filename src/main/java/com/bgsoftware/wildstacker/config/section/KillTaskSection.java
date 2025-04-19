package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;
import com.bgsoftware.wildstacker.utils.data.structures.Fast2EnumsArray;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;

public class KillTaskSection extends SettingsContainerHolder implements SettingsManager.KillTask {


    @Override
    public long getInterval() {
        return getContainer().killTaskInterval;
    }

    @Override
    public boolean isStackedEntitiesKillEnabled() {
        return getContainer().killTaskStackedEntities;
    }

    @Override
    public boolean isUnstackedEntitiesKillEnabled() {
        return getContainer().killTaskUnstackedEntities;
    }

    @Override
    public boolean isStackedItemsKillEnabled() {
        return getContainer().killTaskStackedItems;
    }

    @Override
    public boolean isUnstackedItemsKillEnabled() {
        return getContainer().killTaskUnstackedItems;
    }

    @Override
    public boolean isSyncWithClearLaggEnabled() {
        return getContainer().killTaskSyncClearLagg;
    }

    @Override
    public String getTimeCommand() {
        return getContainer().killTaskTimeCommand;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getEntitiesWhitelist() {
        return getContainer().killTaskEntitiesWhitelist;
    }

    @Override
    public Fast2EnumsArray<EntityType, SpawnCause> getEntitiesBlacklist() {
        return getContainer().killTaskEntitiesBlacklist;
    }

    @Override
    public List<String> getEntitiesWorlds() {
        return getContainer().killTaskEntitiesWorlds;
    }

    @Override
    public FastEnumArray<Material> getItemsWhitelist() {
        return getContainer().killTaskItemsWhitelist;
    }

    @Override
    public FastEnumArray<Material> getItemsBlacklist() {
        return getContainer().killTaskItemsBlacklist;
    }

    @Override
    public List<String> getItemsWorlds() {
        return getContainer().killTaskItemsWorlds;
    }
}