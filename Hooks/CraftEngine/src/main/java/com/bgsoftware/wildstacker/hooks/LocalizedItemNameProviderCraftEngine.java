package com.bgsoftware.wildstacker.hooks;

import org.bukkit.inventory.ItemStack;

public final class LocalizedItemNameProviderCraftEngine extends ReflectionLocalizedItemNameProvider {

    @Override
    public String getPluginName() {
        return "CraftEngine";
    }

    @Override
    public ItemStack resolveRegistryItem(ItemStack itemStack) {
        try {
            Class<?> apiClass = loadProviderClass("net.momirealms.craftengine.bukkit.api.CraftEngineItems");
            if (apiClass == null)
                return null;

            Object definition = getMethod(apiClass, "byItemStack", ItemStack.class).invoke(null, itemStack);
            return definition == null ? null : invokeItemStack(definition, "buildBukkitItem");
        } catch (Throwable ignored) {
            return null;
        }
    }

}
