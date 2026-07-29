package com.bgsoftware.wildstacker.utils.names.localization;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import javax.annotation.Nullable;
import java.util.Objects;

public final class LocalizedNameTemplate {

    private static final int RENDER_CACHE_MAX_SIZE = 512;

    private final Component templateComponent;
    private final boolean hasAmountPlaceholder;
    private final boolean hasNamePlaceholder;
    private final boolean hasUpgradePlaceholder;
    private final String rawPattern;
    private final BoundedLoadingCache<TranslationRenderKey, Component> translationRenderCache =
            new BoundedLoadingCache<>(RENDER_CACHE_MAX_SIZE);

    private LocalizedNameTemplate(Component templateComponent, String rawPattern) {
        this.templateComponent = templateComponent;
        this.rawPattern = rawPattern == null ? "" : rawPattern;
        this.hasAmountPlaceholder = this.rawPattern.contains("{0}");
        this.hasNamePlaceholder = this.rawPattern.contains("{1}") || this.rawPattern.contains("{2}");
        this.hasUpgradePlaceholder = this.rawPattern.contains("{3}");
    }

    public static LocalizedNameTemplate compile(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return new LocalizedNameTemplate(Component.empty(), "");
        }
        String colorized = pattern.replace('§', '&');
        Component comp;
        try {
            comp = LegacyComponentSerializer.legacyAmpersand().deserialize(colorized);
        } catch (Throwable ignored) {
            comp = Component.text(pattern);
        }
        return new LocalizedNameTemplate(comp, pattern);
    }

    public String getRawPattern() {
        return rawPattern;
    }

    public Component renderItemName(int amount, Component nameComponent) {
        Component result = templateComponent;
        if (hasAmountPlaceholder) {
            result = result.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("{0}")
                    .replacement(Component.text(String.valueOf(amount)))
                    .build());
        }
        if (hasNamePlaceholder) {
            result = result.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("{1}")
                    .replacement(nameComponent)
                    .build());
            result = result.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("{2}")
                    .replacement(nameComponent)
                    .build());
        }
        return result;
    }

    public Component renderItemTranslation(int amount, String translationKey) {
        TranslationRenderKey cacheKey = new TranslationRenderKey(translationKey, amount, null, false);
        return translationRenderCache.get(cacheKey,
                () -> renderItemName(amount, Component.translatable(translationKey)));
    }

    public Component renderEntityName(int amount, Component nameComponent, @Nullable String upgradeDisplayName) {
        Component result = renderItemName(amount, nameComponent);
        if (hasUpgradePlaceholder && upgradeDisplayName != null && !upgradeDisplayName.isEmpty()) {
            result = result.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("{3}")
                    .replacement(Component.text(upgradeDisplayName))
                    .build());
        } else if (hasUpgradePlaceholder) {
            result = result.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("{3}")
                    .replacement(Component.empty())
                    .build());
        }
        return result;
    }

    public Component renderEntityTranslation(int amount, String translationKey, @Nullable String upgradeDisplayName) {
        TranslationRenderKey cacheKey = new TranslationRenderKey(translationKey, amount, upgradeDisplayName, true);
        return translationRenderCache.get(cacheKey,
                () -> renderEntityName(amount, Component.translatable(translationKey), upgradeDisplayName));
    }

    private static final class TranslationRenderKey {
        private final String translationKey;
        private final int amount;
        private final String upgradeDisplayName;
        private final boolean entity;

        private TranslationRenderKey(String translationKey, int amount, @Nullable String upgradeDisplayName,
                                     boolean entity) {
            this.translationKey = translationKey;
            this.amount = amount;
            this.upgradeDisplayName = upgradeDisplayName;
            this.entity = entity;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (!(other instanceof TranslationRenderKey))
                return false;

            TranslationRenderKey that = (TranslationRenderKey) other;
            return amount == that.amount && entity == that.entity && translationKey.equals(that.translationKey) &&
                    Objects.equals(upgradeDisplayName, that.upgradeDisplayName);
        }

        @Override
        public int hashCode() {
            int result = translationKey.hashCode();
            result = 31 * result + amount;
            result = 31 * result + (upgradeDisplayName == null ? 0 : upgradeDisplayName.hashCode());
            result = 31 * result + (entity ? 1 : 0);
            return result;
        }
    }
}
