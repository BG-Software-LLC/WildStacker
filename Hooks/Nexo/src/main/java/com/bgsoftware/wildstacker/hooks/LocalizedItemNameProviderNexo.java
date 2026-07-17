package com.bgsoftware.wildstacker.hooks;

import org.bukkit.inventory.ItemStack;

public final class LocalizedItemNameProviderNexo extends ReflectionLocalizedItemNameProvider {

    @Override
    public String getPluginName() {
        return "Nexo";
    }

    @Override
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        try {
            Class<?> apiClass = loadProviderClass("com.nexomc.nexo.api.NexoItems");
            if (apiClass == null)
                return null;

            Object builder;
            try {
                builder = getMethod(apiClass, "builderFromItem", ItemStack.class).invoke(null, itemStack);
            } catch (NoSuchMethodException ignored) {
                Object itemId = getMethod(apiClass, "idFromItem", ItemStack.class).invoke(null, itemStack);
                builder = itemId == null ? null : getMethod(apiClass, "itemFromId", String.class)
                        .invoke(null, itemId.toString());
            }

            if (builder == null)
                return null;

            ItemStack resolved = invokeItemStack(builder, "getFinalItemStack");
            return resolved == null ? invokeItemStack(builder, "build") : resolved;
        } catch (Throwable ignored) {
            return null;
        }
    }

}
