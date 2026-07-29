package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemDescriptor;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class LocalizedItemNameProvider_Oraxen implements LocalizedItemNameProvider {

    private volatile ProviderState state = ProviderState.READY;

    @Override
    public String getId() {
        return "oraxen";
    }

    @Override
    public String getPluginName() {
        return "Oraxen";
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
            String itemId = OraxenItems.getIdByItem(itemStack);
            if (itemId == null)
                return null;
            ItemBuilder builder = OraxenItems.getItemById(itemId);
            return builder == null ? null : builder.build();
        } catch (LinkageError error) {
            markIncompatible("Installed Oraxen API signature is incompatible with hook: " + error.getMessage());
            return null;
        } catch (VirtualMachineError error) {
            throw error;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    @Nullable
    public LocalizedItemDescriptor resolveDescriptor(ItemStack itemStack) {
        if (state == ProviderState.INCOMPATIBLE)
            return null;

        try {
            String itemId = OraxenItems.getIdByItem(itemStack);
            if (itemId == null)
                return null;
            ItemBuilder builder = OraxenItems.getItemById(itemId);
            ItemStack registryItem = builder == null ? null : builder.build();
            return registryItem == null ? null : LocalizedItemDescriptor.ofCanonicalItem(getId(), "oraxen:" + itemId, registryItem);
        } catch (LinkageError error) {
            markIncompatible("Installed Oraxen API signature is incompatible with hook: " + error.getMessage());
            return null;
        } catch (VirtualMachineError error) {
            throw error;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void markIncompatible(String reason) {
        if (state != ProviderState.INCOMPATIBLE) {
            state = ProviderState.INCOMPATIBLE;
            try {
                WildStackerPlugin.getPlugin().getLogger().warning(
                        "[WildStacker] Disabled localized-name provider 'oraxen': " + reason +
                        ". Vanilla and other providers remain active.");
            } catch (Throwable ignored) {
            }
        }
    }

}
