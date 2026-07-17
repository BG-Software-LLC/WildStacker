package com.bgsoftware.wildstacker.hooks;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared reflection boundary for custom-item hooks that support multiple API versions.
 */
abstract class ReflectionLocalizedItemNameProvider implements LocalizedItemNameProvider {

    private final Map<String, Class<?>> providerClasses = new ConcurrentHashMap<>();
    private final Set<String> missingProviderClasses = ConcurrentHashMap.newKeySet();
    private final Map<String, Method> methods = new ConcurrentHashMap<>();
    private final Object providerCacheLock = new Object();
    private WeakReference<Plugin> loadedProvider = new WeakReference<>(null);

    protected final Class<?> loadProviderClass(String className) throws ClassNotFoundException {
        synchronized (providerCacheLock) {
            Plugin provider = Bukkit.getPluginManager().getPlugin(getPluginName());
            if (provider == null || !provider.isEnabled())
                return null;

            if (loadedProvider.get() != provider) {
                providerClasses.clear();
                missingProviderClasses.clear();
                methods.clear();
                loadedProvider = new WeakReference<>(provider);
            }

            Class<?> providerClass = providerClasses.get(className);
            if (providerClass != null)
                return providerClass;
            if (missingProviderClasses.contains(className))
                return null;

            try {
                providerClass = provider.getClass().getClassLoader().loadClass(className);
                providerClasses.put(className, providerClass);
                return providerClass;
            } catch (ClassNotFoundException error) {
                missingProviderClasses.add(className);
                throw error;
            }
        }
    }

    protected final Method getMethod(Class<?> owner, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        StringBuilder cacheKey = new StringBuilder(owner.getName()).append('#').append(methodName);
        for (Class<?> parameterType : parameterTypes)
            cacheKey.append(':').append(parameterType.getName());

        String methodKey = cacheKey.toString();
        Method method = methods.get(methodKey);
        if (method == null) {
            method = owner.getMethod(methodName, parameterTypes);
            Method cachedMethod = methods.putIfAbsent(methodKey, method);
            if (cachedMethod != null)
                method = cachedMethod;
        }
        return method;
    }

    protected final ItemStack invokeItemStack(Object instance, String methodName) {
        try {
            Object result = getMethod(instance.getClass(), methodName).invoke(instance);
            return result instanceof ItemStack ? ((ItemStack) result).clone() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

}
