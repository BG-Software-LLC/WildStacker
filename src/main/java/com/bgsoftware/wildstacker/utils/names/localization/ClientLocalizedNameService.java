package com.bgsoftware.wildstacker.utils.names.localization;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ClientLocalizedNameService {

    private static final int CACHE_MAX_SIZE = 512;
    private static final long CACHE_LIFETIME_NANOS = TimeUnit.MINUTES.toNanos(5);
    private static final ClientLocalizedNameRenderer RENDERER = createRenderer();
    private static final Map<String, CachedDescriptor> DESCRIPTOR_CACHE =
            new LinkedHashMap<String, CachedDescriptor>(CACHE_MAX_SIZE, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CachedDescriptor> eldest) {
                    return size() > CACHE_MAX_SIZE;
                }
            };

    private static long cachedProviderRevision = -1L;
    private static boolean warnedUnsupported = false;

    private ClientLocalizedNameService() {
    }

    private static ClientLocalizedNameRenderer createRenderer() {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.wildstacker.hooks.ClientLocalizedNameRenderer_Paper");
            ClientLocalizedNameRenderer renderer = (ClientLocalizedNameRenderer) clazz.newInstance();
            if (renderer.isSupported())
                return renderer;
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static void invalidateCaches() {
        synchronized (DESCRIPTOR_CACHE) {
            DESCRIPTOR_CACHE.clear();
            cachedProviderRevision = -1L;
        }
    }

    public static LocalizedNameApplyResult setItemName(Entity itemEntity, ItemStack itemStack, LocalizedNameTemplate template, int amount) {
        if (RENDERER == null) {
            logUnsupportedOnce();
            return LocalizedNameApplyResult.PLATFORM_UNSUPPORTED;
        }

        try {
            if (isPristineVanilla(itemStack)) {
                return RENDERER.applyItemName(itemEntity, itemStack, template, amount, null);
            }

            LocalizedItemDescriptor descriptor = resolveCachedDescriptor(itemStack);
            return RENDERER.applyItemName(itemEntity, itemStack, template, amount, descriptor);
        } catch (Throwable error) {
            if (error instanceof VirtualMachineError)
                throw (VirtualMachineError) error;
            return LocalizedNameApplyResult.COMPONENT_BUILD_FAILED;
        }
    }

    public static LocalizedNameApplyResult setEntityName(Entity entity, EntityType entityType, LocalizedNameTemplate template, int amount, @Nullable String upgradeDisplayName) {
        if (RENDERER == null) {
            logUnsupportedOnce();
            return LocalizedNameApplyResult.PLATFORM_UNSUPPORTED;
        }

        try {
            return RENDERER.applyEntityName(entity, entityType, template, amount, upgradeDisplayName);
        } catch (Throwable error) {
            if (error instanceof VirtualMachineError)
                throw (VirtualMachineError) error;
            return LocalizedNameApplyResult.COMPONENT_BUILD_FAILED;
        }
    }

    private static boolean isPristineVanilla(ItemStack itemStack) {
        return !itemStack.hasItemMeta();
    }

    private static LocalizedItemDescriptor resolveCachedDescriptor(ItemStack itemStack) {
        WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
        long currentRevision = plugin.getProviders().getLocalizedItemNameProviderRevision();
        String cacheKey = buildCacheKey(itemStack);
        long currentTime = System.nanoTime();

        synchronized (DESCRIPTOR_CACHE) {
            if (cachedProviderRevision != currentRevision) {
                DESCRIPTOR_CACHE.clear();
                cachedProviderRevision = currentRevision;
            }

            CachedDescriptor cached = DESCRIPTOR_CACHE.get(cacheKey);
            if (cached != null) {
                if (currentTime - cached.expirationTimeNanos < 0) {
                    return cached.descriptor;
                }
                DESCRIPTOR_CACHE.remove(cacheKey);
            }
        }

        LocalizedItemDescriptor descriptor = plugin.getProviders().resolveLocalizedItemDescriptor(itemStack);

        synchronized (DESCRIPTOR_CACHE) {
            if (cachedProviderRevision == currentRevision) {
                DESCRIPTOR_CACHE.put(cacheKey, new CachedDescriptor(descriptor, currentTime + CACHE_LIFETIME_NANOS));
            }
        }

        return descriptor;
    }

    private static String buildCacheKey(ItemStack itemStack) {
        if (!itemStack.hasItemMeta())
            return itemStack.getType().name();

        ItemMeta meta = itemStack.getItemMeta();
        return itemStack.getType().name() + ":meta:" + (meta == null ? 0 : meta.hashCode());
    }

    private static void logUnsupportedOnce() {
        if (!warnedUnsupported) {
            warnedUnsupported = true;
            try {
                WildStackerPlugin.getPlugin().getLogger().info(
                        "[WildStacker] Client-localized names require Paper 1.19.3+ with Adventure API. Falling back to legacy String names.");
            } catch (Throwable ignored) {
            }
        }
    }

    private static final class CachedDescriptor {
        private final LocalizedItemDescriptor descriptor;
        private final long expirationTimeNanos;

        private CachedDescriptor(LocalizedItemDescriptor descriptor, long expirationTimeNanos) {
            this.descriptor = descriptor;
            this.expirationTimeNanos = expirationTimeNanos;
        }
    }
}
