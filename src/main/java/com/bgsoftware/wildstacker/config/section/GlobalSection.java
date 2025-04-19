package com.bgsoftware.wildstacker.config.section;

import com.bgsoftware.wildstacker.api.config.SettingsManager;
import com.bgsoftware.wildstacker.config.SettingsContainerHolder;
import org.bukkit.inventory.ItemStack;

public class GlobalSection extends SettingsContainerHolder implements SettingsManager.Global {

    @Override
    public String getGiveItemNamePattern() {
        return getContainer().giveItemName;
    }

    @Override
    public ItemStack getInspectTool() {
        return getContainer().inspectTool;
    }

    @Override
    public ItemStack getSimulateTool() {
        return getContainer().simulateTool;
    }

    @Override
    public boolean shouldDeleteInvalidWorlds() {
        return getContainer().deleteInvalidWorlds;
    }
}
