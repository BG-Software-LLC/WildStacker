package com.bgsoftware.wildstacker.hooks;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Resolves the canonical item registered by an optional custom-item plugin.
 */
public interface LocalizedItemNameProvider {

    String getPluginName();

    @Nullable
    ItemStack resolveRegistryItem(ItemStack itemStack);

}
