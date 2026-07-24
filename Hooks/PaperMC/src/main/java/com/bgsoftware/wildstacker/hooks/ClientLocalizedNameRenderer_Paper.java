package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.utils.names.localization.ClientLocalizedNameRenderer;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedItemDescriptor;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedNameApplyResult;
import com.bgsoftware.wildstacker.utils.names.localization.LocalizedNameTemplate;
import net.kyori.adventure.text.Component;
import org.bukkit.Nameable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public final class ClientLocalizedNameRenderer_Paper implements ClientLocalizedNameRenderer {

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

        Component localizedNameComponent = null;

        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                localizedNameComponent = extractItemMetaComponent(meta);
            }
        }

        if (localizedNameComponent == null && descriptor != null) {
            if (descriptor.getComponent() instanceof Component) {
                localizedNameComponent = (Component) descriptor.getComponent();
            } else if (descriptor.getTranslationKey() != null) {
                localizedNameComponent = Component.translatable(descriptor.getTranslationKey());
            } else if (descriptor.getCanonicalItem() != null) {
                ItemStack canonical = descriptor.getCanonicalItem();
                if (canonical.hasItemMeta()) {
                    ItemMeta meta = canonical.getItemMeta();
                    if (meta != null) {
                        localizedNameComponent = extractItemMetaComponent(meta);
                    }
                }
                if (localizedNameComponent == null) {
                    try {
                        String key = canonical.translationKey();
                        if (key != null && !key.isEmpty()) {
                            localizedNameComponent = Component.translatable(key);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        if (localizedNameComponent == null) {
            try {
                String key = itemStack.translationKey();
                if (key != null && !key.isEmpty()) {
                    localizedNameComponent = Component.translatable(key);
                }
            } catch (Throwable ignored) {
            }
        }

        if (localizedNameComponent == null)
            return LocalizedNameApplyResult.NO_NAME_SOURCE;

        try {
            Component finalName = template.renderItemName(amount, localizedNameComponent);
            ((Nameable) itemEntity).customName(finalName);
            itemEntity.setCustomNameVisible(true);
            return LocalizedNameApplyResult.APPLIED;
        } catch (Throwable error) {
            if (error instanceof Error)
                throw (Error) error;
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

        Component localizedNameComponent = null;
        try {
            String key = entityType.translationKey();
            if (key != null && !key.isEmpty()) {
                localizedNameComponent = Component.translatable(key);
            }
        } catch (Throwable ignored) {
        }

        if (localizedNameComponent == null)
            return LocalizedNameApplyResult.NO_NAME_SOURCE;

        try {
            Component finalName = template.renderEntityName(amount, localizedNameComponent, upgradeDisplayName);
            ((Nameable) entity).customName(finalName);
            entity.setCustomNameVisible(true);
            return LocalizedNameApplyResult.APPLIED;
        } catch (Throwable error) {
            if (error instanceof Error)
                throw (Error) error;
            return LocalizedNameApplyResult.COMPONENT_BUILD_FAILED;
        }
    }

    private static Component extractItemMetaComponent(ItemMeta meta) {
        Component comp = getOptionalComponent(meta, "customName");
        if (comp != null)
            return comp;

        comp = getOptionalComponent(meta, "itemName");
        if (comp != null)
            return comp;

        if (meta.hasDisplayName()) {
            try {
                return meta.displayName();
            } catch (Throwable ignored) {
            }
        }

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

}
