package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import org.bukkit.Chunk;

@SuppressWarnings("WeakerAccess")
public abstract class WStackedObject<T> implements StackedObject<T> {

    protected static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    protected final T object;
    protected int stackAmount;

    protected WStackedObject(T object, int stackAmount) {
        this.object = object;
        this.stackAmount = stackAmount;
    }

    @Override
    public int getStackAmount(){
        //return instance.getSystemManager().getStackAmount(this);
        return stackAmount;
    }

    @Override
    public void setStackAmount(int stackAmount, boolean updateName){
        //instance.getSystemManager().setStackAmount(this, stackAmount);
        this.stackAmount = stackAmount;
        if(updateName)
            updateName();
    }

    public abstract Chunk getChunk();

    @Override
    public abstract int getStackLimit();

    @Override
    public abstract void remove();

    @Override
    public abstract void updateName();

    @Override
    public abstract T tryStack();

    @Override
    public abstract boolean canStackInto(StackedObject stackedObject);

    @Override
    public abstract boolean tryStackInto(StackedObject stackedObject);

    @Override
    public abstract boolean tryUnstack(int amount);

    @Override
    public abstract boolean isSimilar(StackedObject stackedObject);

}
