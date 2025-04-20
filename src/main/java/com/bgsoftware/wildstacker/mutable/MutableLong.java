package com.bgsoftware.wildstacker.mutable;

public class MutableLong {

    private long value;

    public MutableLong(long value) {
        this.value = value;
    }

    public long get() {
        return this.value;
    }

    public void set(long value) {
        this.value = value;
    }

}
