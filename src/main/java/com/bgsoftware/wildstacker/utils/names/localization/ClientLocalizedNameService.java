package com.bgsoftware.wildstacker.utils.names.localization;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.handlers.SettingsHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Applies client-translatable Adventure names when the running server exposes the API.
 * Unsupported servers and malformed third-party components fail back to WildStacker's legacy String names.
 */
public final class ClientLocalizedNameService {

    private static final String NAME_TOKEN = "\u0001wildstacker_localized_name\u0002";
    private static final int ITEM_NAME_CACHE_MAX_SIZE = 256;
    private static final long ITEM_NAME_CACHE_LIFETIME_NANOS = TimeUnit.MINUTES.toNanos(5);
    private static final AdventureBridge ADVENTURE = AdventureBridge.create();
    private static final Map<ItemStack, CachedItemName> ITEM_NAME_CACHE =
            new LinkedHashMap<ItemStack, CachedItemName>(ITEM_NAME_CACHE_MAX_SIZE, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<ItemStack, CachedItemName> eldest) {
                    return size() > ITEM_NAME_CACHE_MAX_SIZE;
                }
            };

    private static int cachedProviderConfiguration = -1;

    private ClientLocalizedNameService() {
    }

    public static boolean setItemName(Entity itemEntity, ItemStack itemStack, String pattern, int amount) {
        if (ADVENTURE == null)
            return false;

        try {
            Object itemName = ADVENTURE.findEmbeddedName(itemStack);
            if (itemName == null)
                itemName = resolveCachedItemName(itemStack);
            if (itemName == null)
                return false;

            return ADVENTURE.apply(itemEntity, pattern.replace("{0}", String.valueOf(amount)), itemName);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Object resolveCachedItemName(ItemStack itemStack) throws Exception {
        WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
        int providerConfiguration = getProviderConfiguration(plugin.getSettings());
        ItemStack cacheKey = itemStack.clone();
        cacheKey.setAmount(1);
        long currentTime = System.nanoTime();

        synchronized (ITEM_NAME_CACHE) {
            if (cachedProviderConfiguration != providerConfiguration) {
                ITEM_NAME_CACHE.clear();
                cachedProviderConfiguration = providerConfiguration;
            }

            CachedItemName cachedItemName = ITEM_NAME_CACHE.get(cacheKey);
            if (cachedItemName != null) {
                if (currentTime - cachedItemName.expirationTimeNanos < 0)
                    return cachedItemName.component;
                ITEM_NAME_CACHE.remove(cacheKey);
            }

            Object itemName = null;
            ItemStack registryItem = plugin.getProviders().resolveLocalizedItemName(itemStack);
            if (registryItem != null)
                itemName = ADVENTURE.findEmbeddedName(registryItem);

            if (itemName == null) {
                String translationKey = ADVENTURE.getItemTranslationKey(itemStack);
                if (translationKey != null)
                    itemName = ADVENTURE.translatable(translationKey);
            }

            ITEM_NAME_CACHE.put(cacheKey,
                    new CachedItemName(itemName, currentTime + ITEM_NAME_CACHE_LIFETIME_NANOS));
            return itemName;
        }
    }

    private static int getProviderConfiguration(SettingsHandler settings) {
        int configuration = settings.itemsLocalizedNamesCraftEngine ? 1 : 0;
        configuration |= settings.itemsLocalizedNamesItemsAdder ? 1 << 1 : 0;
        configuration |= settings.itemsLocalizedNamesNexo ? 1 << 2 : 0;
        configuration |= settings.itemsLocalizedNamesOraxen ? 1 << 3 : 0;
        return configuration;
    }

    public static boolean setEntityName(Entity entity, EntityType entityType, String pattern, int amount,
                                        String upgradeDisplayName) {
        if (ADVENTURE == null)
            return false;

        try {
            String translationKey = ADVENTURE.getEntityTranslationKey(entityType);
            if (translationKey == null)
                return false;

            String preparedPattern = pattern.replace("{0}", String.valueOf(amount))
                    .replace("{3}", upgradeDisplayName == null ? "" : upgradeDisplayName);
            return ADVENTURE.apply(entity, preparedPattern, ADVENTURE.translatable(translationKey));
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static final class AdventureBridge {

        private final Class<?> componentClass;
        private final Object legacySerializer;
        private final Method deserializeMethod;
        private final Method translatableMethod;
        private final Method replaceTextMethod;
        private final Method customNameMethod;
        private final Method itemHasCustomNameMethod;
        private final Method itemCustomNameMethod;
        private final Method itemHasItemNameMethod;
        private final Method itemNameMethod;
        private final Method itemHasDisplayNameMethod;
        private final Method itemDisplayNameMethod;
        private final Method itemTranslationKeyMethod;
        private final Method entityTranslationKeyMethod;

        private AdventureBridge(Class<?> componentClass, Object legacySerializer, Method deserializeMethod,
                                Method translatableMethod, Method replaceTextMethod, Method customNameMethod,
                                Method itemHasCustomNameMethod, Method itemCustomNameMethod,
                                Method itemHasItemNameMethod, Method itemNameMethod,
                                Method itemHasDisplayNameMethod, Method itemDisplayNameMethod,
                                Method itemTranslationKeyMethod, Method entityTranslationKeyMethod) {
            this.componentClass = componentClass;
            this.legacySerializer = legacySerializer;
            this.deserializeMethod = deserializeMethod;
            this.translatableMethod = translatableMethod;
            this.replaceTextMethod = replaceTextMethod;
            this.customNameMethod = customNameMethod;
            this.itemHasCustomNameMethod = itemHasCustomNameMethod;
            this.itemCustomNameMethod = itemCustomNameMethod;
            this.itemHasItemNameMethod = itemHasItemNameMethod;
            this.itemNameMethod = itemNameMethod;
            this.itemHasDisplayNameMethod = itemHasDisplayNameMethod;
            this.itemDisplayNameMethod = itemDisplayNameMethod;
            this.itemTranslationKeyMethod = itemTranslationKeyMethod;
            this.entityTranslationKeyMethod = entityTranslationKeyMethod;
        }

        static AdventureBridge create() {
            try {
                Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
                Class<?> componentLikeClass = Class.forName("net.kyori.adventure.text.ComponentLike");
                Class<?> serializerClass = Class.forName(
                        "net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
                Class<?> nameableClass = Class.forName("org.bukkit.Nameable");

                Object serializer = serializerClass.getMethod("legacySection").invoke(null);
                Method deserialize = serializerClass.getMethod("deserialize", String.class);
                Method translatable = componentClass.getMethod("translatable", String.class);
                Method replaceText = componentClass.getMethod("replaceText", String.class, componentLikeClass);
                Method customName = nameableClass.getMethod("customName", componentClass);

                return new AdventureBridge(componentClass, serializer, deserialize, translatable, replaceText,
                        customName,
                        getOptionalMethod(ItemMeta.class, "hasCustomName"),
                        getOptionalMethod(ItemMeta.class, "customName"),
                        getOptionalMethod(ItemMeta.class, "hasItemName"),
                        getOptionalMethod(ItemMeta.class, "itemName"),
                        getOptionalMethod(ItemMeta.class, "hasDisplayName"),
                        getOptionalMethod(ItemMeta.class, "displayName"),
                        firstMethod(ItemStack.class, "getTranslationKey", "translationKey"),
                        firstMethod(EntityType.class, "getTranslationKey", "translationKey"));
            } catch (Throwable ignored) {
                return null;
            }
        }

        private static Method getOptionalMethod(Class<?> owner, String methodName) {
            try {
                return owner.getMethod(methodName);
            } catch (Throwable ignored) {
                return null;
            }
        }

        private static Method firstMethod(Class<?> owner, String... methodNames) {
            for (String methodName : methodNames) {
                Method method = getOptionalMethod(owner, methodName);
                if (method != null)
                    return method;
            }
            return null;
        }

        Object translatable(String translationKey) throws Exception {
            return translatableMethod.invoke(null, translationKey);
        }

        Object findEmbeddedName(ItemStack itemStack) {
            if (!itemStack.hasItemMeta())
                return null;

            ItemMeta itemMeta = itemStack.getItemMeta();
            Object component = invokeComponent(itemMeta, itemHasCustomNameMethod, itemCustomNameMethod);
            if (component == null)
                component = invokeComponent(itemMeta, itemHasItemNameMethod, itemNameMethod);
            if (component == null)
                component = invokeComponent(itemMeta, itemHasDisplayNameMethod, itemDisplayNameMethod);
            return component;
        }

        String getItemTranslationKey(ItemStack itemStack) {
            return invokeString(itemStack, itemTranslationKeyMethod);
        }

        String getEntityTranslationKey(EntityType entityType) {
            return invokeString(entityType, entityTranslationKeyMethod);
        }

        private Object invokeComponent(Object instance, Method hasMethod, Method componentMethod) {
            if (hasMethod == null || componentMethod == null)
                return null;

            try {
                return Boolean.TRUE.equals(hasMethod.invoke(instance)) ? componentMethod.invoke(instance) : null;
            } catch (Throwable ignored) {
                return null;
            }
        }

        private String invokeString(Object instance, Method method) {
            if (method == null)
                return null;

            try {
                Object result = method.invoke(instance);
                return result instanceof String && !((String) result).isEmpty() ? (String) result : null;
            } catch (Throwable ignored) {
                return null;
            }
        }

        boolean apply(Entity entity, String pattern, Object localizedName) throws Exception {
            if (!componentClass.isInstance(localizedName) ||
                    (!pattern.contains("{1}") && !pattern.contains("{2}"))) {
                return false;
            }

            String tokenizedPattern = pattern.replace("{1}", NAME_TOKEN).replace("{2}", NAME_TOKEN);
            Object patternComponent = deserializeMethod.invoke(legacySerializer, tokenizedPattern);
            Object localizedComponent = replaceTextMethod.invoke(patternComponent, NAME_TOKEN, localizedName);
            customNameMethod.invoke(entity, localizedComponent);
            return true;
        }
    }

    private static final class CachedItemName {

        private final Object component;
        private final long expirationTimeNanos;

        private CachedItemName(Object component, long expirationTimeNanos) {
            this.component = component;
            this.expirationTimeNanos = expirationTimeNanos;
        }

    }
}
