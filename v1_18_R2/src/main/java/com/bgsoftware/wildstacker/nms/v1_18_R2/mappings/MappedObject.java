package com.bgsoftware.wildstacker.nms.v1_18_R2.mappings;

public abstract class MappedObject<E> {

    protected final E handle;

    protected MappedObject(E handle) {
        this.handle = handle;
    }

    public E getHandle() {
        return handle;
    }

}
