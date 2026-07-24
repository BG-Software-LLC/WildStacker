package com.bgsoftware.wildstacker.utils.names.localization;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class LocalizedItemDescriptor {

    private final String providerId;
    private final String stableItemId;
    private final Object component;
    private final String translationKey;
    private final ItemStack canonicalItem;

    public LocalizedItemDescriptor(String providerId, String stableItemId, @Nullable Object component,
                                   @Nullable String translationKey, @Nullable ItemStack canonicalItem) {
        this.providerId = providerId;
        this.stableItemId = stableItemId;
        this.component = component;
        this.translationKey = translationKey;
        this.canonicalItem = canonicalItem;
    }

    public static LocalizedItemDescriptor ofComponent(String providerId, String stableItemId, Object component) {
        return new LocalizedItemDescriptor(providerId, stableItemId, component, null, null);
    }

    public static LocalizedItemDescriptor ofTranslationKey(String providerId, String stableItemId, String translationKey) {
        return new LocalizedItemDescriptor(providerId, stableItemId, null, translationKey, null);
    }

    public static LocalizedItemDescriptor ofItemStack(String providerId, String stableItemId, ItemStack itemStack) {
        return new LocalizedItemDescriptor(providerId, stableItemId, null, null, itemStack);
    }

    public String getProviderId() {
        return providerId;
    }

    public String getStableItemId() {
        return stableItemId;
    }

    @Nullable
    public Object getComponent() {
        return component;
    }

    @Nullable
    public String getTranslationKey() {
        return translationKey;
    }

    @Nullable
    public ItemStack getCanonicalItem() {
        return canonicalItem;
    }
}
