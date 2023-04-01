package com.bgsoftware.wildstacker.stacker.scheduler;

import com.bgsoftware.wildstacker.stacker.WStackedObject;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StackerScheduler<T extends WStackedObject<?>> {

    private static final AtomicInteger schedulerIdGenerator = new AtomicInteger(0);

    private final int schedulerId = schedulerIdGenerator.getAndIncrement();
    private ExecutorService executor;

    private final List<WeakReference<T>> stackingObjects = new LinkedList<>();
    private final Lock schedulerLock = new ReentrantLock();
    private final AtomicInteger refCount = new AtomicInteger(0);
    private boolean stopped = false;

    public StackerScheduler() {
        addRefCount();
    }

    public void addStackedObject(T object) {
        ensureActive("Called addStackedObject on an already stopped scheduler");

        try {
            this.schedulerLock.lock();
            this.stackingObjects.add(new WeakReference<>(object));
        } finally {
            this.schedulerLock.unlock();
        }
    }

    public void schedule(Runnable task) {
        ensureActive("Called schedule on an already stopped scheduler");

        if (isStackerThread()) {
            runTaskLocked(task);
        } else {
            getExecutor().submit(() -> runTaskLocked(task));
        }
    }

    private void runTaskLocked(Runnable task) {
        try {
            this.schedulerLock.lock();
            task.run();
        } finally {
            this.schedulerLock.unlock();
        }
    }

    public List<WeakReference<T>> getStackingObjects() {
        return this.stackingObjects;
    }

    public boolean checkInactive() {
        return stopped || refCount.get() <= 0;
    }

    public void addRefCount() {
        ensureActive("Called addRefCount on an already stopped scheduler");
        this.refCount.incrementAndGet();
    }

    public void removeRefCount() {
        ensureActive("Called removeRefCount on an already stopped scheduler");
        int newRefCount = this.refCount.decrementAndGet();
        if (newRefCount <= 0)
            stop();
    }

    public boolean isStackerThread() {
        Thread current = Thread.currentThread();
        return current instanceof StackerThread && ((StackerThread) current).id == schedulerId;
    }

    public void mergeInto(StackerScheduler<T> scheduler) {
        ensureActive("Called mergeInto on an already stopped scheduler");

        try {
            this.schedulerLock.lock();

            if (this.executor != null) {
                List<Runnable> leftOverTasks = this.executor.shutdownNow();
                leftOverTasks.forEach(scheduler::schedule);
                this.executor = null;
            }

            this.stopped = true;

            scheduler.addRefCount();
        } finally {
            this.schedulerLock.unlock();
        }
    }

    public void stop() {
        if (!this.stopped) {
            this.stopped = true;
            if (this.executor != null) {
                this.executor.shutdownNow();
                this.executor = null;
            }
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    private void ensureActive(String message) {
        if (this.stopped)
            throw new IllegalStateException(message);
    }

    private ExecutorService getExecutor() {
        if (this.executor == null) {
            this.executor = Executors.newSingleThreadExecutor(task -> {
                StackerThread stackerThread = new StackerThread(task, schedulerId);
                stackerThread.setName("StackerScheduler Thread #" + schedulerId);
                return stackerThread;
            });
        }

        return this.executor;
    }

    private static class StackerThread extends Thread {

        private final int id;

        StackerThread(Runnable task, int id) {
            super(task);
            this.id = id;
        }

    }

}
