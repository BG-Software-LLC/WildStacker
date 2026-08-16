package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemDescriptor;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Resolves the canonical item registered by an optional custom-item plugin.
 */
public interface LocalizedItemNameProvider {

    enum ProviderState {
        ABSENT,
        LOADING,
        READY,
        FAILED,
        INCOMPATIBLE,
        DISABLED
    }

    /**
     * Stable provider ID in lowercase ASCII (e.g. "craftengine", "itemsadder", "nexo", "oraxen").
     */
    String getId();

    /**
     * Bukkit plugin name for lifecycle registration matching.
     */
    String getPluginName();

    /**
     * Current readiness state of the provider.
     */
    default ProviderState getState() {
        return ProviderState.READY;
    }

    /**
     * Resolve pure canonical ItemStack from third-party registry or null.
     */
    @Nullable
    ItemStack resolveRegistryItem(ItemStack itemStack);

    /**
     * Resolve a lightweight descriptor or null.
     */
    @Nullable
    default LocalizedItemDescriptor resolveDescriptor(ItemStack itemStack) {
        ItemStack registryItem = resolveRegistryItem(itemStack);
        return registryItem == null ? null : LocalizedItemDescriptor.ofCanonicalItem(getId(), getId() + ":" + itemStack.getType().name(), registryItem);
    }

}
