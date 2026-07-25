package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemDescriptor;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class LocalizedItemNameProvider_CraftEngine implements LocalizedItemNameProvider {

    private final LocalizedItemNameProvider_CraftEngineLegacy delegate = new LocalizedItemNameProvider_CraftEngineLegacy();

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getPluginName() {
        return delegate.getPluginName();
    }

    @Override
    public ProviderState getState() {
        return delegate.getState();
    }

    @Override
    @Nullable
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        return delegate.resolveRegistryItem(itemStack);
    }

    @Override
    @Nullable
    public LocalizedItemDescriptor resolveDescriptor(ItemStack itemStack) {
        return delegate.resolveDescriptor(itemStack);
    }

}
