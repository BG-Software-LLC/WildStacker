package com.bgsoftware.wildstacker.api.data.structures;

/**
 * Represents a fast map for two enum keys to a value.
 *
 * @param <E> the first enum type
 * @param <T> the second enum type
 * @param <V> the value type
 */
public interface IFast2EnumsMap<E extends Enum<E>, T extends Enum<T>, V> {

    /**
     * Retrieves the value for the given pair of keys.
     *
     * @param first the first enum key
     * @param second the second enum key
     * @return the associated value, or null if not found
     */
    V get(E first, T second);

    /**
     * Retrieves the value for the given pair of keys, or a default value if none found.
     *
     * @param first the first enum key
     * @param second the second enum key
     * @param defaultValue the default value
     * @return the associated value or the default
     */
    V getOrDefault(E first, T second, V defaultValue);

    /**
     * Gets the number of stored entries.
     *
     * @return the size
     */
    int size();
}
