package com.bgsoftware.wildstacker.api.data.structures;

/**
 * Represents a fast, memory-efficient storage for combinations of three enum types.
 *
 * @param <E> the first enum type
 * @param <T> the second enum type
 * @param <S> the third enum type
 */
public interface IFast3EnumsArray<E extends Enum<E>, T extends Enum<T>, S extends Enum<S>> {

    /**
     * Checks if the structure contains the given (first, second) pair.
     *
     * @param first the first enum value
     * @param second the second enum value
     * @return true if the pair is contained, false otherwise
     */
    boolean containsFirst(E first, T second);

    /**
     * Checks if the structure contains the given (first, third) pair.
     *
     * @param first the first enum value
     * @param third the third enum value
     * @return true if the pair is contained, false otherwise
     */
    boolean containsSecond(E first, S third);

    /**
     * Returns the number of stored entries in the structure.
     *
     * @return the size
     */
    int size();
}
