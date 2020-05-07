package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.objects.AsyncStackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.utils.threads.StackService;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
public abstract class WAsyncStackedObject<T> extends WStackedObject<T> implements AsyncStackedObject<T> {

    protected WAsyncStackedObject(T object, int stackAmount) {
        super(object, stackAmount);
    }

    @Override
    public Optional<T> runStack(){
        throw new UnsupportedOperationException("Cannot stack async object using the sync method.");
    }

    @Override
    public T tryStack() {
        new UnsupportedOperationException("tryStack method is no longer supported.").printStackTrace();
        runStackAsync(null);
        return null;
    }

    @Override
    public void runStackAsync(StackedObject stackedObject, Consumer<StackResult> stackResult){
        StackService.execute(stackedObject, () -> {
            StackResult _stackResult = runStack(stackedObject);
            if(stackResult != null)
                stackResult.accept(_stackResult);
        });
    }

    @Override
    public abstract void runStackAsync(Consumer<Optional<T>> result);

}
