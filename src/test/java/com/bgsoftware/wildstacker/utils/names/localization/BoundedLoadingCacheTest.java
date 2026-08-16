package com.bgsoftware.wildstacker.utils.names.localization;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class BoundedLoadingCacheTest {

    private BoundedLoadingCacheTest() {
    }

    public static void main(String[] args) {
        reusesLoadedValue();
        evictsLeastRecentlyUsedEntry();
        handlesMillionRepeatedLookupsWithoutGrowth();
    }

    private static void reusesLoadedValue() {
        BoundedLoadingCache<String, Object> cache = new BoundedLoadingCache<>(2);
        AtomicInteger loads = new AtomicInteger();

        cache.get("item.minecraft.diamond", () -> {
            loads.incrementAndGet();
            return new Object();
        });
        cache.get("item.minecraft.diamond", () -> {
            loads.incrementAndGet();
            return new Object();
        });

        if (loads.get() != 1)
            throw new AssertionError("Cache loader ran more than once for the same key");
    }

    private static void evictsLeastRecentlyUsedEntry() {
        BoundedLoadingCache<String, String> cache = new BoundedLoadingCache<>(2);
        AtomicInteger reloads = new AtomicInteger();

        cache.get("a", () -> "a");
        cache.get("b", () -> "b");
        cache.get("a", () -> "unused");
        cache.get("c", () -> "c");
        cache.get("b", () -> {
            reloads.incrementAndGet();
            return "b2";
        });

        if (reloads.get() != 1)
            throw new AssertionError("Least-recently-used entry was not evicted");
        if (cache.size() != 2)
            throw new AssertionError("Cache exceeded its configured maximum size");
    }

    private static void handlesMillionRepeatedLookupsWithoutGrowth() {
        BoundedLoadingCache<String, Object> cache = new BoundedLoadingCache<>(512);
        AtomicInteger loads = new AtomicInteger();
        Supplier<Object> loader = () -> {
            loads.incrementAndGet();
            return new Object();
        };

        for (int i = 0; i < 1_000_000; ++i)
            cache.get("item.minecraft.diamond", loader);

        if (loads.get() != 1 || cache.size() != 1)
            throw new AssertionError("Repeated lookups caused duplicate loads or cache growth");
    }
}
