package com.bgsoftware.wildstacker.api.names;

/**
 * Represents a dynamic name builder for stacked objects.
 *
 * @param <T> The object type (item, entity, spawner, etc.)
 */
public interface DisplayNameBuilder<T> {

    /**
     * Builds the name for the given instance.
     *
     * @param object The stacked object
     * @return A formatted display name
     */
    String build(T object);
}