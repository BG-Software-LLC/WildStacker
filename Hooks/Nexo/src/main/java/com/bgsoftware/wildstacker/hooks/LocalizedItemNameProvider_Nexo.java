package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemDescriptor;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class LocalizedItemNameProvider_Nexo implements LocalizedItemNameProvider {

    private volatile ProviderState state = ProviderState.READY;

    @Override
    public String getId() {
        return "nexo";
    }

    @Override
    public String getPluginName() {
        return "Nexo";
    }

    @Override
    public ProviderState getState() {
        return state;
    }

    @Override
    @Nullable
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        if (state == ProviderState.INCOMPATIBLE)
            return null;

        try {
            ItemBuilder builder = NexoItems.builderFromItem(itemStack);
            if (builder == null)
                return null;
            ItemStack finalItem = builder.getFinalItemStack();
            return finalItem == null ? builder.build() : finalItem;
        } catch (LinkageError error) {
            markIncompatible("Installed Nexo API signature is incompatible with hook: " + error.getMessage());
            return null;
        } catch (Throwable error) {
            if (error instanceof VirtualMachineError)
                throw (VirtualMachineError) error;
            return null;
        }
    }

    @Override
    @Nullable
    public LocalizedItemDescriptor resolveDescriptor(ItemStack itemStack) {
        if (state == ProviderState.INCOMPATIBLE)
            return null;

        try {
            String itemId = NexoItems.idFromItem(itemStack);
            if (itemId == null)
                return null;
            ItemBuilder builder = NexoItems.builderFromItem(itemStack);
            ItemStack registryItem = builder == null ? null : (builder.getFinalItemStack() == null ? builder.build() : builder.getFinalItemStack());
            return registryItem == null ? null : LocalizedItemDescriptor.ofCanonicalItem(getId(), "nexo:" + itemId, registryItem);
        } catch (LinkageError error) {
            markIncompatible("Installed Nexo API signature is incompatible with hook: " + error.getMessage());
            return null;
        } catch (Throwable error) {
            if (error instanceof VirtualMachineError)
                throw (VirtualMachineError) error;
            return null;
        }
    }

    private void markIncompatible(String reason) {
        if (state != ProviderState.INCOMPATIBLE) {
            state = ProviderState.INCOMPATIBLE;
            try {
                WildStackerPlugin.getPlugin().getLogger().warning(
                        "[WildStacker] Disabled localized-name provider 'nexo': " + reason +
                        ". Vanilla and other providers remain active.");
            } catch (Throwable ignored) {
            }
        }
    }

}
