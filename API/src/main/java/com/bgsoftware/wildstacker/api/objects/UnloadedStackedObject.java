package com.bgsoftware.wildstacker.api.objects;

import org.bukkit.Location;
import org.bukkit.World;

public interface UnloadedStackedObject {

    /**
     * Get the location of the object.
     */
    Location getLocation();

    /**
     * Get the world of the object.
     */
    World getWorld();

    /**
     * Get the stack amount
     *
     * @return stack amount
     */
    int getStackAmount();

    /**
     * Update the stack amount
     *
     * @param stackAmount a new stack amount
     * @param updateName  should the name of the object get updated or not
     */
    void setStackAmount(int stackAmount, boolean updateName);

    /**
     * Remove the stack object from cache and the server
     */
    void remove();

}
