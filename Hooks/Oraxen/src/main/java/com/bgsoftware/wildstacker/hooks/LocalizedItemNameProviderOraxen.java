package com.bgsoftware.wildstacker.hooks;

import org.bukkit.inventory.ItemStack;

public final class LocalizedItemNameProviderOraxen extends ReflectionLocalizedItemNameProvider {

    @Override
    public String getPluginName() {
        return "Oraxen";
    }

    @Override
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        try {
            Class<?> apiClass = loadProviderClass("io.th0rgal.oraxen.api.OraxenItems");
            if (apiClass == null)
                return null;

            Object itemId = getMethod(apiClass, "getIdByItem", ItemStack.class).invoke(null, itemStack);
            if (itemId == null)
                return null;

            Object builder = getMethod(apiClass, "getItemById", String.class).invoke(null, itemId.toString());
            return builder == null ? null : invokeItemStack(builder, "build");
        } catch (Throwable ignored) {
            return null;
        }
    }

}
