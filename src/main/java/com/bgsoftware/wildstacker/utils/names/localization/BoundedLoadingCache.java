package com.bgsoftware.wildstacker.utils.names.localization;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

final class BoundedLoadingCache<K, V> {

    private final Map<K, V> values;

    BoundedLoadingCache(final int maximumSize) {
        if (maximumSize <= 0)
            throw new IllegalArgumentException("maximumSize must be positive");

        values = new LinkedHashMap<K, V>(maximumSize, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maximumSize;
            }
        };
    }

    synchronized V get(K key, Supplier<V> loader) {
        V value = values.get(key);
        if (value != null)
            return value;

        value = loader.get();
        if (value != null)
            values.put(key, value);
        return value;
    }

    synchronized int size() {
        return values.size();
    }
}
