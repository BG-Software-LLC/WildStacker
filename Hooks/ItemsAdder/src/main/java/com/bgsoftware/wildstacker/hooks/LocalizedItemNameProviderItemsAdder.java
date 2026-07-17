package com.bgsoftware.wildstacker.hooks;

import org.bukkit.inventory.ItemStack;

public final class LocalizedItemNameProviderItemsAdder extends ReflectionLocalizedItemNameProvider {

    @Override
    public String getPluginName() {
        return "ItemsAdder";
    }

    @Override
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        try {
            Class<?> apiClass = loadProviderClass("dev.lone.itemsadder.api.CustomStack");
            if (apiClass == null)
                return null;

            Object customStack = getMethod(apiClass, "byItemStack", ItemStack.class).invoke(null, itemStack);
            return customStack == null ? null : invokeItemStack(customStack, "getItemStack");
        } catch (Throwable ignored) {
            return null;
        }
    }

}
