package com.bgsoftware.wildstacker.api.data.structures;

import java.util.List;

/**
 * Represents a fast, memory-efficient storage for combinations of two enum types.
 *
 * @param <E> the first enum type
 * @param <T> the second enum type
 */
public interface IFast2EnumsArray<E extends Enum<E>, T extends Enum<T>> {

    /**
     * Checks if the structure contains the given second key.
     *
     * @param second the second enum key
     * @return true if contained, false otherwise
     */
    boolean contains(T second);

    /**
     * Checks if the structure contains the given pair of keys.
     *
     * @param first the first enum key
     * @param second the second enum key
     * @return true if the pair is contained, false otherwise
     */
    boolean contains(E first, T second);

    /**
     * Returns the total number of stored entries.
     *
     * @return the number of entries
     */
    int size();

    /**
     * Returns a raw 2D array of the internal ordinal-based data.
     *
     * @return the array of entries by ordinal index
     */
    byte[][] combinedKeyArray();
}
