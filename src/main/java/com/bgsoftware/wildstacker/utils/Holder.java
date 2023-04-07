package com.bgsoftware.wildstacker.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Holder<T> {

    private final List<Long> holders = new LinkedList<>();
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

    public void addHolder(long holder) {
        this.holders.add(holder);
    }

    public void removeHolder(long holder) {
        this.holders.remove(holder);
    }

    public List<Long> getHolders() {
        return Collections.unmodifiableList(this.holders);
    }

}
