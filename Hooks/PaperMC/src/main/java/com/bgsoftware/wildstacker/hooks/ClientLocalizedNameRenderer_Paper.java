package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.utils.names.localization.ClientLocalizedNameRenderer;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemDescriptor;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemNameSourceType;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedNameApplyResult;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedNameTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Nameable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class ClientLocalizedNameRenderer_Paper implements ClientLocalizedNameRenderer {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    @Override
    public boolean isSupported() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            Class.forName("org.bukkit.Nameable");
            Nameable.class.getMethod("customName", Component.class);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public LocalizedNameApplyResult applyItemName(
            Entity itemEntity,
            ItemStack itemStack,
            LocalizedNameTemplate template,
            int amount,
            @Nullable LocalizedItemDescriptor descriptor
    ) {
        if (!(itemEntity instanceof Nameable) || !itemEntity.isValid())
            return LocalizedNameApplyResult.ENTITY_INVALID;

        try {
            Component finalName = renderItemName(itemStack, template, amount, descriptor);
            if (finalName == null)
                return LocalizedNameApplyResult.NO_NAME_SOURCE;

            applyCustomName((Nameable) itemEntity, finalName);
            return LocalizedNameApplyResult.APPLIED;
        } catch (Error error) {
            throw error;
        } catch (Throwable ignored) {
            return LocalizedNameApplyResult.COMPONENT_BUILD_FAILED;
        }
    }

    @Override
    public LocalizedNameApplyResult applyEntityName(
            Entity entity,
            EntityType entityType,
            LocalizedNameTemplate template,
            int amount,
            @Nullable String upgradeDisplayName
    ) {
        if (!(entity instanceof Nameable) || !entity.isValid())
            return LocalizedNameApplyResult.ENTITY_INVALID;

        String localizedTranslationKey = null;
        try {
            String key = entityType.translationKey();
            if (key != null && !key.isEmpty()) {
                localizedTranslationKey = key;
            }
        } catch (Throwable ignored) {
        }

        if (localizedTranslationKey == null)
            return LocalizedNameApplyResult.NO_NAME_SOURCE;

        try {
            Component finalName = template.renderEntityTranslation(
                    amount, localizedTranslationKey, upgradeDisplayName);
            applyCustomName((Nameable) entity, finalName);
            return LocalizedNameApplyResult.APPLIED;
        } catch (Error error) {
            throw error;
        } catch (Throwable ignored) {
            return LocalizedNameApplyResult.COMPONENT_BUILD_FAILED;
        }
    }

    @Nullable
    private static Component renderItemName(ItemStack itemStack, LocalizedNameTemplate template, int amount,
                                            @Nullable LocalizedItemDescriptor descriptor) {
        if (descriptor != null) {
            Component providerName = renderProviderItemName(descriptor, template, amount);
            if (providerName != null)
                return providerName;
        }

        Component embeddedName = extractItemStackComponent(itemStack);
        if (embeddedName != null)
            return template.renderItemName(amount, embeddedName);

        if (descriptor != null)
            return null;

        String translationKey = getItemTranslationKey(itemStack);
        return translationKey == null ? null : template.renderItemTranslation(amount, translationKey);
    }

    @Nullable
    private static Component renderProviderItemName(LocalizedItemDescriptor descriptor,
                                                    LocalizedNameTemplate template, int amount) {
        LocalizedItemNameSourceType sourceType = descriptor.getSourceType();
        switch (sourceType) {
            case CUSTOM_COMPONENT:
                Object customComponent = descriptor.getCustomComponent();
                return customComponent instanceof Component ?
                        template.renderItemName(amount, (Component) customComponent) : null;
            case CUSTOM_TRANSLATION_KEY:
                String translationKey = descriptor.getCustomTranslationKey();
                return translationKey == null ? null : template.renderItemTranslation(amount, translationKey);
            case CUSTOM_LITERAL_NAME:
                String literalName = descriptor.getCustomLiteralName();
                return literalName == null ? null :
                        template.renderItemName(amount, LEGACY_SERIALIZER.deserialize(literalName));
            case CANONICAL_EFFECTIVE_NAME:
                Component canonicalName = extractCanonicalItemComponent(descriptor.getCanonicalItem());
                return canonicalName == null ? null : template.renderItemName(amount, canonicalName);
            default:
                return null;
        }
    }

    @Nullable
    private static Component extractCanonicalItemComponent(@Nullable ItemStack itemStack) {
        if (itemStack == null)
            return null;

        Component embeddedName = extractItemStackComponent(itemStack);
        if (embeddedName != null)
            return embeddedName;

        Component effectiveName = getOptionalItemComponent(itemStack, "effectiveName");
        return effectiveName == null || isBaseMaterialTranslationKey(effectiveName, itemStack) ?
                null : effectiveName;
    }

    @Nullable
    private static Component extractItemStackComponent(ItemStack itemStack) {
        if (!itemStack.hasItemMeta())
            return null;

        ItemMeta meta = itemStack.getItemMeta();
        return meta == null ? null : extractItemMetaComponent(meta);
    }

    @Nullable
    private static String getItemTranslationKey(ItemStack itemStack) {
        try {
            String translationKey = itemStack.translationKey();
            return translationKey == null || translationKey.isEmpty() ? null : translationKey;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void applyCustomName(Nameable entity, Component component) {
        /*
         * Paper stores Adventure names in a lazy NMS wrapper. Comparing an old wrapper with a new one
         * can serialize both components to JSON inside Entity#setCustomName. Clearing the optional
         * value first keeps both equality checks constant-time; entity metadata only exposes the final
         * value when the tracker flushes after this synchronous update.
         */
        entity.customName(null);
        entity.customName(component);
    }

    private static Component extractItemMetaComponent(ItemMeta meta) {
        if (meta.hasDisplayName()) {
            try {
                Component comp = meta.displayName();
                if (comp != null)
                    return comp;
            } catch (Throwable ignored) {
            }
        }

        Component comp = getOptionalComponent(meta, "customName");
        if (comp != null)
            return comp;

        comp = getOptionalComponent(meta, "itemName");
        if (comp != null)
            return comp;

        return null;
    }

    private static Component getOptionalComponent(ItemMeta meta, String methodName) {
        try {
            Method method = meta.getClass().getMethod(methodName);
            Object res = method.invoke(meta);
            if (res instanceof Component)
                return (Component) res;
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Component getOptionalItemComponent(ItemStack item, String methodName) {
        try {
            Method method = item.getClass().getMethod(methodName);
            Object res = method.invoke(item);
            if (res instanceof Component)
                return (Component) res;
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static boolean isBaseMaterialTranslationKey(Component component, ItemStack item) {
        if (component instanceof net.kyori.adventure.text.TranslatableComponent) {
            String key = ((net.kyori.adventure.text.TranslatableComponent) component).key();
            try {
                String baseKey = item.translationKey();
                if (key.equals(baseKey))
                    return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

}
