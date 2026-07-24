package com.bgsoftware.wildstacker.hooks;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class LocalizedItemNameProvider_CraftEngine implements LocalizedItemNameProvider {

    @Override
    public String getId() {
        return "craftengine";
    }

    @Override
    public String getPluginName() {
        return "CraftEngine";
    }

    @Override
    @Nullable
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        try {
            CustomItem<ItemStack> customItem = CraftEngineItems.byItemStack(itemStack);
            if (customItem == null)
                return null;
            Item<ItemStack> item = customItem.buildItem((ItemBuildContext) null);
            return item == null ? null : item.getItem();
        } catch (Throwable error) {
            if (error instanceof Error)
                throw (Error) error;
            return null;
        }
    }

}
