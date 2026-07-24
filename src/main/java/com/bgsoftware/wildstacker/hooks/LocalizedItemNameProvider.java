package com.bgsoftware.wildstacker.hooks;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Resolves the canonical item registered by an optional custom-item plugin.
 */
public interface LocalizedItemNameProvider {

    /**
     * Stable provider ID in lowercase ASCII (e.g. "craftengine", "itemsadder", "nexo", "oraxen").
     */
    String getId();

    /**
     * Bukkit plugin name for lifecycle registration matching.
     */
    String getPluginName();

    /**
     * Resolve pure canonical ItemStack from third-party registry or null.
     */
    @Nullable
    ItemStack resolveRegistryItem(ItemStack itemStack);

}
