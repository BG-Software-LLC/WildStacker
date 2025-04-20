package com.bgsoftware.wildstacker.api.objects;

import java.util.Map;

/**
 * A generic immutable implementation of {@link Map.Entry} representing a key-value pair.
 * <p>
 * This class is useful for returning two related values, such as a mapping or association,
 * without creating a full map structure. The {@code Pair} is immutable and does not support
 * the {@link #setValue(Object)} operation.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public final class Pair<K, V> implements Map.Entry<K, V> {

    /**
     * The key of the pair.
     */
    private final K key;

    /**
     * The value associated with the key.
     */
    private final V value;

    /**
     * Constructs a new {@code Pair} with the specified key and value.
     *
     * @param key   the key element of the pair
     * @param value the value element of the pair
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key of this pair.
     *
     * @return the key of the pair
     */
    @Override
    public K getKey() {
        return key;
    }

    /**
     * Returns the value of this pair.
     *
     * @return the value associated with the key
     */
    @Override
    public V getValue() {
        return value;
    }

    /**
     * Unsupported operation. This implementation is immutable and does not support modification.
     *
     * @param value the new value
     * @return never returns normally
     * @throws UnsupportedOperationException always thrown to indicate the operation is unsupported
     */
    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("Cannot use the setValue method of Pair");
    }

    /**
     * Returns a string representation of this pair.
     *
     * @return a string in the format {@code Pair{key=value}}
     */
    @Override
    public String toString() {
        return "Pair{" + key + "=" + value + '}';
    }
}
