package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemDescriptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class LocalizedItemNameProvider_CraftEngineLegacy implements LocalizedItemNameProvider {

    private volatile ProviderState state = ProviderState.READY;

    @Override
    public String getId() {
        return "craftengine";
    }

    @Override
    public String getPluginName() {
        return "CraftEngine";
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
            CustomItem<ItemStack> customItem = CraftEngineItems.byItemStack(itemStack);
            if (customItem == null)
                return null;
            Item<ItemStack> item = customItem.buildItem((ItemBuildContext) null);
            return item == null ? null : item.getItem();
        } catch (LinkageError error) {
            markIncompatible("Installed CraftEngine API signature is incompatible with legacy 0.0.x hook: " + error.getMessage());
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
            CustomItem<ItemStack> customItem = CraftEngineItems.byItemStack(itemStack);
            if (customItem == null)
                return null;
            Item<ItemStack> item = customItem.buildItem((ItemBuildContext) null);
            ItemStack registryItem = item == null ? null : item.getItem();
            return registryItem == null ? null : LocalizedItemDescriptor.ofCanonicalItem(getId(), "craftengine:" + customItem.id(), registryItem);
        } catch (LinkageError error) {
            markIncompatible("Installed CraftEngine API signature is incompatible with legacy 0.0.x hook: " + error.getMessage());
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
                        "[WildStacker] Disabled localized-name provider 'craftengine': " + reason +
                        ". Vanilla and other providers remain active.");
            } catch (Throwable ignored) {
            }
        }
    }

}
