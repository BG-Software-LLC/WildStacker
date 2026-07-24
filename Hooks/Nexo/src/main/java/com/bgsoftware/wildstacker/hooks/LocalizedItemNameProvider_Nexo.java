package com.bgsoftware.wildstacker.hooks;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class LocalizedItemNameProvider_Nexo implements LocalizedItemNameProvider {

    @Override
    public String getId() {
        return "nexo";
    }

    @Override
    public String getPluginName() {
        return "Nexo";
    }

    @Override
    @Nullable
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        try {
            ItemBuilder builder = NexoItems.builderFromItem(itemStack);
            if (builder == null)
                return null;
            ItemStack finalItem = builder.getFinalItemStack();
            return finalItem == null ? builder.build() : finalItem;
        } catch (Throwable error) {
            if (error instanceof Error)
                throw (Error) error;
            return null;
        }
    }

}
