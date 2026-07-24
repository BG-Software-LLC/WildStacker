package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class LocalizedItemNameProvider_ItemsAdder implements LocalizedItemNameProvider, Listener {

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

    @EventHandler
    public void onItemAdderLoadData(ItemsAdderLoadDataEvent event) {
        try {
            WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
            if (plugin != null && plugin.getProviders() != null) {
                plugin.getProviders().registerLocalizedItemNameProvider(this);
            }
        } catch (Throwable ignored) {
        }
    }

}
