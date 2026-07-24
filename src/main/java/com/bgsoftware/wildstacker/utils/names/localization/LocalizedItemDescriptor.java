package com.bgsoftware.wildstacker.utils.names.localization;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class LocalizedItemDescriptor {

    private final String providerId;
    private final String stableItemId;
    private final LocalizedItemNameSourceType sourceType;
    private final Object customComponent;
    private final String customTranslationKey;
    private final String customLiteralName;
    private final ItemStack canonicalItem;

    public LocalizedItemDescriptor(String providerId, String stableItemId, LocalizedItemNameSourceType sourceType,
                                   @Nullable Object customComponent, @Nullable String customTranslationKey,
                                   @Nullable String customLiteralName, @Nullable ItemStack canonicalItem) {
        this.providerId = providerId;
        this.stableItemId = stableItemId;
        this.sourceType = sourceType;
        this.customComponent = customComponent;
        this.customTranslationKey = customTranslationKey;
        this.customLiteralName = customLiteralName;
        this.canonicalItem = canonicalItem;
    }

    public static LocalizedItemDescriptor ofComponent(String providerId, String stableItemId, Object component) {
        return new LocalizedItemDescriptor(providerId, stableItemId, LocalizedItemNameSourceType.CUSTOM_COMPONENT, component, null, null, null);
    }

    public static LocalizedItemDescriptor ofTranslationKey(String providerId, String stableItemId, String translationKey) {
        return new LocalizedItemDescriptor(providerId, stableItemId, LocalizedItemNameSourceType.CUSTOM_TRANSLATION_KEY, null, translationKey, null, null);
    }

    public static LocalizedItemDescriptor ofLiteralName(String providerId, String stableItemId, String literalName) {
        return new LocalizedItemDescriptor(providerId, stableItemId, LocalizedItemNameSourceType.CUSTOM_LITERAL_NAME, null, null, literalName, null);
    }

    public static LocalizedItemDescriptor ofCanonicalItem(String providerId, String stableItemId, ItemStack itemStack) {
        return new LocalizedItemDescriptor(providerId, stableItemId, LocalizedItemNameSourceType.CANONICAL_EFFECTIVE_NAME, null, null, null, itemStack);
    }

    public static LocalizedItemDescriptor ofItemStack(String providerId, String stableItemId, ItemStack itemStack) {
        return ofCanonicalItem(providerId, stableItemId, itemStack);
    }

    public String getProviderId() {
        return providerId;
    }

    public String getStableItemId() {
        return stableItemId;
    }

    public LocalizedItemNameSourceType getSourceType() {
        return sourceType;
    }

    @Nullable
    public Object getCustomComponent() {
        return customComponent;
    }

    @Nullable
    public Object getComponent() {
        return customComponent;
    }

    @Nullable
    public String getCustomTranslationKey() {
        return customTranslationKey;
    }

    @Nullable
    public String getTranslationKey() {
        return customTranslationKey;
    }

    @Nullable
    public String getCustomLiteralName() {
        return customLiteralName;
    }

    @Nullable
    public ItemStack getCanonicalItem() {
        return canonicalItem;
    }
}
