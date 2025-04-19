package com.bgsoftware.wildstacker.api.data.structures;

/**
 * Represents a fast key-value map where the keys are of enum type.
 *
 * @param <E> the enum key type
 * @param <V> the value type
 */
public interface IFastEnumMap<E extends Enum<E>, V> {

    /**
     * Gets the value for the given key.
     *
     * @param key the enum key
     * @return the value, or global default if set, or null
     */
    V get(E key);

    /**
     * Gets the value for the given key or returns the default if none is present.
     *
     * @param key the enum key
     * @param defaultValue the fallback value
     * @return the stored value or default
     */
    V getOrDefault(E key, V defaultValue);

    /**
     * Gets the number of entries stored.
     *
     * @return the size of the map
     */
    int size();
}
