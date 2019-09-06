package com.bgsoftware.wildstacker.api.objects;

import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;

import java.util.Optional;
import java.util.function.Consumer;

public interface StackedObject<T> {

    /**
     * Get the stack amount
     * @return stack amount
     */
    int getStackAmount();

    /**
     * Update the stack amount
     * @param stackAmount a new stack amount
     * @param updateName should the name of the object get updated or not
     */
    void setStackAmount(int stackAmount, boolean updateName);

    /**
     * Get the stack limit
     * @return stack limit
     */
    int getStackLimit();

    /**
     * Checks if an object is blacklisted.
     * @return True if the object is blacklisted, otherwise false.
     */
    boolean isBlacklisted();

    /**
     * Checks if an object is whitelisted.
     * @return True if the object is whitelisted, otherwise false.
     */
    boolean isWhitelisted();

    /**
     * Checks if the world of the object is disabled.
     * @return True if the world is disabled, otherwise false.
     */
    boolean isWorldDisabled();

    /**
     * Remove the stack object from cache and the server
     */
    void remove();

    /**
     * Update the name of the object.
     */
    void updateName();

    /**
     * Try to stack this object into other objects around it.
     * If succeed to stack, the object it got stacked to will be returned.
     * If failed, null will be returned.
     */

    /**
     * Checks if this object can stack into another object.
     * @param stackedObject other object to check
     *
     * @return True if can stack into, otherwise false
     */
    boolean canStackInto(StackedObject stackedObject);

    void runStackAsync(Consumer<Optional<T>> result);

    //T tryStack();

    void runStackAsync(StackedObject stackedObject, Consumer<StackResult> stackResult);

    StackResult runStack(StackedObject stackedObject);

    //boolean tryStackInto(StackedObject stackedObject);

    UnstackResult runUnstack(int amount);

    //boolean tryUnstack(int amount);

    /**
     * Checks if this object is similar to another object.
     * @param stackedObject the object to check
     * @return True if they are similar objects, otherwise false
     */
    boolean isSimilar(StackedObject stackedObject);

}
