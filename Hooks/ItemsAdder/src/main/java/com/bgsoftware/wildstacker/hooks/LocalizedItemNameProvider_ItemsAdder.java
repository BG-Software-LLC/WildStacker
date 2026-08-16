package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemDescriptor;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class LocalizedItemNameProvider_ItemsAdder implements LocalizedItemNameProvider, Listener {

    private volatile boolean ready = false;
    private volatile ProviderState state = ProviderState.LOADING;

    public LocalizedItemNameProvider_ItemsAdder(WildStackerPlugin plugin) {
        try {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public String getId() {
        return "itemsadder";
    }

    @Override
    public String getPluginName() {
        return "ItemsAdder";
    }

    @Override
    public ProviderState getState() {
        if (state == ProviderState.INCOMPATIBLE)
            return ProviderState.INCOMPATIBLE;
        return ready ? ProviderState.READY : ProviderState.LOADING;
    }

    @Override
    @Nullable
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        if (state == ProviderState.INCOMPATIBLE)
            return null;

        try {
            CustomStack customStack = CustomStack.byItemStack(itemStack);
            return customStack == null ? null : customStack.getItemStack();
        } catch (LinkageError error) {
            markIncompatible("Installed ItemsAdder API signature is incompatible with hook: " + error.getMessage());
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
            CustomStack customStack = CustomStack.byItemStack(itemStack);
            if (customStack == null)
                return null;
            return LocalizedItemDescriptor.ofCanonicalItem(getId(), "itemsadder:" + customStack.getNamespacedID(), customStack.getItemStack());
        } catch (LinkageError error) {
            markIncompatible("Installed ItemsAdder API signature is incompatible with hook: " + error.getMessage());
            return null;
        } catch (VirtualMachineError error) {
            throw error;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @EventHandler
    public void onItemAdderLoadData(ItemsAdderLoadDataEvent event) {
        try {
            ready = true;
            WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
            if (plugin != null && plugin.getProviders() != null) {
                plugin.getProviders().registerLocalizedItemNameProvider(this);
            }
        } catch (Throwable ignored) {
        }
    }

    private void markIncompatible(String reason) {
        if (state != ProviderState.INCOMPATIBLE) {
            state = ProviderState.INCOMPATIBLE;
            try {
                WildStackerPlugin.getPlugin().getLogger().warning(
                        "[WildStacker] Disabled localized-name provider 'itemsadder': " + reason +
                        ". Vanilla and other providers remain active.");
            } catch (Throwable ignored) {
            }
        }
    }

}
