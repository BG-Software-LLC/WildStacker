package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Chunk;

import java.util.Optional;
import java.util.function.Consumer;

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
    public abstract boolean isBlacklisted();

    @Override
    public abstract boolean isWhitelisted();

    @Override
    public abstract boolean isWorldDisabled();

    @Override
    public abstract void remove();

    @Override
    public abstract void updateName();

    @Override
    public abstract boolean canStackInto(StackedObject stackedObject);

    @Override
    public abstract void runStackAsync(Consumer<Optional<T>> result);

    @Override
    public T tryStack(){
        new UnsupportedOperationException("tryStack method is no longer supported.").printStackTrace();
        runStackAsync(null);
        return null;
    }

    @Override
    public void runStackAsync(StackedObject stackedObject, Consumer<StackResult> stackResult){
        StackService.execute(() -> {
            StackResult _stackResult = runStack(stackedObject);
            if(stackResult != null)
                stackResult.accept(_stackResult);
        });
    }

    @Override
    public abstract StackResult runStack(StackedObject stackedObject);

    @Override
    public boolean tryStackInto(StackedObject stackedObject){
        new UnsupportedOperationException("tryStackInto method is no longer supported.").printStackTrace();
        return runStack(stackedObject) == StackResult.SUCCESS;
    }


    @Override
    public abstract UnstackResult runUnstack(int amount);

    @Override
    public boolean tryUnstack(int amount){
        new UnsupportedOperationException("tryUnstack method is no longer supported.").printStackTrace();
        return runUnstack(amount) == UnstackResult.SUCCESS;
    }

    @Override
    public abstract boolean isSimilar(StackedObject stackedObject);

}
