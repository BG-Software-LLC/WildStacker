package com.bgsoftware.wildstacker.utils.names.localization;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import javax.annotation.Nullable;

public final class LocalizedNameTemplate {

    private final Component templateComponent;
    private final boolean hasAmountPlaceholder;
    private final boolean hasNamePlaceholder;
    private final boolean hasUpgradePlaceholder;
    private final String rawPattern;

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
}
