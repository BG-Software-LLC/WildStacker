package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedObject;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("WeakerAccess")
public abstract class WUnloadedStackedObject implements UnloadedStackedObject {

    protected static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Location location;
    private int stackAmount;

    protected WUnloadedStackedObject(Location location, int stackAmount) {
        this.location = location;
        this.stackAmount = stackAmount;
    }

    @Override
    public int getStackAmount(){
        try {
            lock.readLock().lock();
            return stackAmount;
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void setStackAmount(int stackAmount, boolean updateName){
        try {
            lock.writeLock().lock();
            this.stackAmount = stackAmount;
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Location getLocation(){
        return location.clone();
    }

    @Override
    public World getWorld() {
        return getLocation().getWorld();
    }

    @Override
    public abstract void remove();

}
