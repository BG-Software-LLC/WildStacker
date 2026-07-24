package com.bgsoftware.wildstacker.hooks;

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class LocalizedItemNameProvider_Oraxen implements LocalizedItemNameProvider {

    @Override
    public String getId() {
        return "oraxen";
    }

    @Override
    public String getPluginName() {
        return "Oraxen";
    }

    @Override
    @Nullable
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        try {
            String itemId = OraxenItems.getIdByItem(itemStack);
            if (itemId == null)
                return null;
            ItemBuilder builder = OraxenItems.getItemById(itemId);
            return builder == null ? null : builder.build();
        } catch (Throwable error) {
            if (error instanceof Error)
                throw (Error) error;
            return null;
        }
    }

}
