package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
public abstract class WStackedObject<T> implements StackedObject<T> {

    protected static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    protected final T object;
    private int stackAmount;
    protected boolean saveData = true;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private String cachedDisplayName = "";

    protected WStackedObject(T object, int stackAmount) {
        this.object = object;
        this.stackAmount = stackAmount;
    }

    @Override
    public int getStackAmount(){
        try {
            lock.readLock().lock();
            return Math.max(stackAmount, 0);
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

        if(updateName)
            updateName();
    }

    public void setSaveData(boolean saveData){
        this.saveData = saveData && isCached();
    }

    public String getCachedDisplayName(){
        return cachedDisplayName;
    }

    public void setCachedDisplayName(String cachedDisplayName){
        this.cachedDisplayName = cachedDisplayName;
    }

    @Override
    public abstract Location getLocation();

    public abstract Chunk getChunk();

    @Override
    public abstract int getStackLimit();

    @Override
    public abstract int getMergeRadius();

    @Override
    public abstract boolean isBlacklisted();

    @Override
    public abstract boolean isWhitelisted();

    @Override
    public abstract boolean isWorldDisabled();

    @Override
    public boolean isCached(){
        return !isBlacklisted() && isWhitelisted() && !isWorldDisabled();
    }

    @Override
    public abstract void remove();

    @Override
    public abstract void updateName();

    @Override
    public boolean canStackInto(StackedObject stackedObject){
        new UnsupportedOperationException("canStackInto method is no longer supported.").printStackTrace();
        return runStackCheck(stackedObject) == StackCheckResult.SUCCESS;
    }

    @Override
    public StackCheckResult runStackCheck(StackedObject stackedObject){
        if(equals(stackedObject) || !isSimilar(stackedObject))
            return StackCheckResult.NOT_SIMILAR;

        if(!isWhitelisted())
            return StackCheckResult.NOT_WHITELISTED;

        if(isBlacklisted())
            return StackCheckResult.BLACKLISTED;

        if(isWorldDisabled())
            return StackCheckResult.DISABLED_WORLD;

//        Removed chunk loading check, as stacked objects in unloaded chunks are not cached anymore.
//        if(!GeneralUtils.isChunkLoaded(getLocation()))
//            return StackCheckResult.CHUNK_NOT_LOADED;

        if(!stackedObject.isWhitelisted())
            return StackCheckResult.TARGET_NOT_WHITELISTED;

        if(stackedObject.isBlacklisted())
            return StackCheckResult.TARGET_BLACKLISTED;

        if(stackedObject.isWorldDisabled())
            return StackCheckResult.TARGET_DISABLED_WORLD;

//        Removed chunk loading check, as stacked objects in unloaded chunks are not cached anymore.
//        if(!GeneralUtils.isChunkLoaded(stackedObject.getLocation()))
//            return StackCheckResult.TARGET_CHUNK_NOT_LOADED;

        int newStackAmount = this.getStackAmount() + stackedObject.getStackAmount();

        if(newStackAmount <= 0 || getStackLimit() < newStackAmount)
            return StackCheckResult.LIMIT_EXCEEDED;

        return StackCheckResult.SUCCESS;
    }

    @Override
    @Deprecated
    public void runStackAsync(Consumer<Optional<T>> result) {
        Optional<T> stackResult = runStack();
        if(result != null)
            result.accept(stackResult);
    }

    @Override
    public abstract Optional<T> runStack();

    @Override
    public T tryStack(){
        new UnsupportedOperationException("tryStack method is no longer supported.").printStackTrace();
        return runStack().orElse(null);
    }

    @Override
    public abstract StackResult runStack(StackedObject stackedObject);

    @Override
    public boolean tryStackInto(StackedObject stackedObject){
        new UnsupportedOperationException("tryStackInto method is no longer supported.").printStackTrace();
        return runStack(stackedObject) == StackResult.SUCCESS;
    }

    @Override
    public UnstackResult runUnstack(int amount){
        return runUnstack(amount, null);
    }

    @Override
    public abstract UnstackResult runUnstack(int amount, Entity entity);

    @Override
    public boolean tryUnstack(int amount){
        new UnsupportedOperationException("tryUnstack method is no longer supported.").printStackTrace();
        return runUnstack(amount, null) == UnstackResult.SUCCESS;
    }

    @Override
    public abstract boolean isSimilar(StackedObject stackedObject);

    @Override
    public abstract void spawnStackParticle(boolean checkEnabled);

}
