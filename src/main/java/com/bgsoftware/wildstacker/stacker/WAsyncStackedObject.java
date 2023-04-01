package com.bgsoftware.wildstacker.stacker;

import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.objects.AsyncStackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.stacker.scheduler.StackerScheduler;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class WAsyncStackedObject<T, R extends WStackedObject<?>> extends WStackedObject<T> implements AsyncStackedObject<T> {

    protected final StackerScheduler<R> scheduler;

    protected WAsyncStackedObject(StackerScheduler<R> scheduler, T object, int stackAmount) {
        super(object, stackAmount);
        this.scheduler = scheduler;
    }

    public abstract int getId();

    @Override
    public void runStackAsync(StackedObject stackedObject, Consumer<StackResult> stackResultCallback) {
        scheduler.schedule(() -> {
            StackResult stackResult = runStack(stackedObject);
            if (stackResultCallback != null)
                stackResultCallback.accept(stackResult);
        });
    }

    @Override
    public void runStackAsync(Consumer<Optional<T>> result) {
        scheduler.schedule(() -> {
            Iterator<WeakReference<R>> stackedObjects = scheduler.getStackingObjects().iterator();

            while (stackedObjects.hasNext()) {
                WeakReference<R> weakStackedReference = stackedObjects.next();
                R stackedObject = weakStackedReference.get();

                if (stackedObject == null) {
                    stackedObjects.remove();
                    continue;
                }
            }
        });
    }

    @Override
    public Optional<T> runStack() {
        throw new UnsupportedOperationException("Cannot stack async object using the sync method.");
    }

    @Override
    public T tryStack() {
        new UnsupportedOperationException("tryStack method is no longer supported.").printStackTrace();
        runStackAsync(null);
        return null;
    }

}
