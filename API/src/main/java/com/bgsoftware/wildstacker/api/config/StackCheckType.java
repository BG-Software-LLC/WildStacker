package com.bgsoftware.wildstacker.api.config;

/**
 * Represents a generic stack check.
 * Implemented internally by core.StackCheck.
 */
public interface StackCheckType {

    /**
     * Get the unique name of this check.
     */
    String name();

    boolean isEnabled();
}
