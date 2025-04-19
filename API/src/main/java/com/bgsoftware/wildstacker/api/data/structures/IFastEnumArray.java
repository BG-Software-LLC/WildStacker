package com.bgsoftware.wildstacker.api.data.structures;

import java.util.Collection;

/**
 * Represents a fast-access enum flag set.
 * Used to check whether specific enum constants are enabled or accepted,
 * typically for whitelists/blacklists.
 *
 * @param <E> the enum type this structure operates on
 */
public interface IFastEnumArray<E extends Enum<E>> {

    /**
     * Checks whether the given enum value is present in the set.
     * May return true for any value if \"ALL\" is enabled.
     *
     * @param value the enum constant to check
     * @return true if the value is present or \"ALL\" is enabled
     */
    boolean contains(E value);

    /**
     * Gets the number of explicitly stored enum constants.
     * Does not count \"ALL\".
     *
     * @return number of entries
     */
    int size();

    /**
     * Returns a collection of all explicitly enabled enum constants.
     * Does not reflect \"ALL\".
     *
     * @return list of contained enum values
     */
    Collection<E> collect();

    /**
     * Whether \"ALL\" is enabled, meaning all enum values are accepted.
     *
     * @return true if all values are enabled
     */
    boolean isAllEnabled();
}