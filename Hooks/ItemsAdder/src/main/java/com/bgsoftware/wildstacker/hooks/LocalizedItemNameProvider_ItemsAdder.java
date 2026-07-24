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

public final class LocalizedItemNameProvider_ItemsAdder implements LocalizedItemNameProvider, Listener {

    private volatile boolean ready = false;

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
        return ready ? ProviderState.READY : ProviderState.LOADING;
    }

    @Override
    @Nullable
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        try {
            CustomStack customStack = CustomStack.byItemStack(itemStack);
            return customStack == null ? null : customStack.getItemStack();
        } catch (Throwable error) {
            if (error instanceof Error)
                throw (Error) error;
            return null;
        }
    }

    @Override
    @Nullable
    public LocalizedItemDescriptor resolveDescriptor(ItemStack itemStack) {
        try {
            CustomStack customStack = CustomStack.byItemStack(itemStack);
            if (customStack == null)
                return null;
            return LocalizedItemDescriptor.ofItemStack(getId(), "itemsadder:" + customStack.getNamespacedID(), customStack.getItemStack());
        } catch (Throwable error) {
            if (error instanceof Error)
                throw (Error) error;
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

}
