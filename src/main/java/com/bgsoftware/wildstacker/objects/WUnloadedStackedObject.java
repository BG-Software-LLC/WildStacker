package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedObject;
import com.bgsoftware.wildstacker.utils.data.structures.Location2ObjectMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("WeakerAccess")
public abstract class WUnloadedStackedObject implements UnloadedStackedObject, Location2ObjectMap.ILocationEntity {

    protected static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final String worldName;
    private final int locX;
    private final int locY;
    private final int locZ;
    private int stackAmount;

    protected WUnloadedStackedObject(String worldName, int locX, int locY, int locZ, int stackAmount) {
        this.worldName = worldName;
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.stackAmount = stackAmount;
    }

    @Override
    @Deprecated
    public Location getLocation() {
        World bukkitWorld = getWorld();
        return new Location(bukkitWorld, this.locX, this.locY, this.locZ);
    }

    @Override
    @Deprecated
    public World getWorld() {
        return Bukkit.getWorld(this.worldName);
    }

    @Override
    public String getWorldName() {
        return this.worldName;
    }

    @Override
    public int getX() {
        return this.locX;
    }

    @Override
    public int getY() {
        return this.locY;
    }

    @Override
    public int getZ() {
        return this.locZ;
    }

    @Override
    public int getStackAmount() {
        try {
            lock.readLock().lock();
            return stackAmount;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void setStackAmount(int stackAmount, boolean updateName) {
        try {
            lock.writeLock().lock();
            this.stackAmount = stackAmount;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public abstract void remove();

}
