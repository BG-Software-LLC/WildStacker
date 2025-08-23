package com.bgsoftware.wildstacker.api.config;

public interface StackCheckType {

    /**
     * Get the unique name of this check.
     */
    String name();

    boolean isEnabled();
}
