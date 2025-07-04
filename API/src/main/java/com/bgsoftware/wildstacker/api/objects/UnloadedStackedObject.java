package com.bgsoftware.wildstacker.api.objects;

import org.bukkit.Location;
import org.bukkit.World;

public interface UnloadedStackedObject {

    /**
     * Get the location of the object.
     *
     * @deprecated World is not guaranteed to be loaded
     */
    @Deprecated
    Location getLocation();

    /**
     * Get the world of the object.
     *
     * @deprecated World is not guaranteed to be loaded
     */
    @Deprecated
    World getWorld();

    /**
     * Get the name of the world of the object.
     */
    String getWorldName();

    /**
     * Get the x-coords of the object.
     */
    int getX();

    /**
     * Get the y-coords of the object.
     */
    int getY();

    /**
     * Get the z-coords of the object.
     */
    int getZ();

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
