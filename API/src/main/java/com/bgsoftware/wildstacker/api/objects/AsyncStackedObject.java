package com.bgsoftware.wildstacker.api.objects;

import com.bgsoftware.wildstacker.api.enums.StackResult;

import java.util.Optional;
import java.util.function.Consumer;

public interface AsyncStackedObject<T> extends StackedObject<T> {

    /**
     * Stack this object into other objects around it.
     * @param result The object that this object was stacked into.
     */
    void runStackAsync(Consumer<Optional<T>> result);

    /**
     * Stack this object into another object.
     * @param stackedObject another object to stack into
     * @param stackResult The result for the stacking operation.
     */
    void runStackAsync(StackedObject stackedObject, Consumer<StackResult> stackResult);

}
