package com.bgsoftware.wildstacker.utils;

import java.util.Objects;

public class Holder<T> {

    private T handle;

    public Holder(T handle) {
        this.handle = Objects.requireNonNull(handle, "Handle cannot be null");
    }

    public T getHandle() {
        return handle;
    }

    public T setHandle(T handle) {
        T oldHandle = this.handle;
        this.handle = Objects.requireNonNull(handle, "Handle cannot be null");
        return oldHandle;
    }

}
