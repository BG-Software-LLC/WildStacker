package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemDescriptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class LocalizedItemNameProvider_CraftEngineModern implements LocalizedItemNameProvider {

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
            BukkitItemDefinition definition = CraftEngineItems.byItemStack(itemStack);
            if (definition == null)
                return null;
            return definition.buildBukkitItem();
        } catch (LinkageError error) {
            markIncompatible("Installed CraftEngine API signature is incompatible with 26.x hook: " + error.getMessage());
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
            BukkitItemDefinition definition = CraftEngineItems.byItemStack(itemStack);
            if (definition == null)
                return null;
            ItemStack canonical = definition.buildBukkitItem();
            if (canonical == null)
                return null;
            Key customIdKey = CraftEngineItems.getCustomItemId(itemStack);
            String itemId = customIdKey != null ? customIdKey.asString() : itemStack.getType().name();
            return LocalizedItemDescriptor.ofCanonicalItem(getId(), "craftengine:" + itemId, canonical);
        } catch (LinkageError error) {
            markIncompatible("Installed CraftEngine API signature is incompatible with 26.x hook: " + error.getMessage());
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
