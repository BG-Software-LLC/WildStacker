package com.bgsoftware.wildstacker.data;

import com.bgsoftware.wildstacker.api.objects.StackedObject;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractEntityDataStore<T extends StackedObject<?>> {

    private final Map<Integer, T> store = createStore();
    private final Map<UUID, Unloaded<T>> unloadedStore = new ConcurrentHashMap<>();

    private final ReadWriteLock storeLock = new ReentrantReadWriteLock();

    @Nullable
    public T get(int id) {
        try {
            this.storeLock.readLock().lock();
            return this.store.get(id);
        } finally {
            this.storeLock.readLock().unlock();
        }
    }

    public void store(int id, T object) {
        try {
            this.storeLock.writeLock().lock();
            this.store.put(id, object);
        } finally {
            this.storeLock.writeLock().unlock();
        }
    }

    @Nullable
    public T remove(int id) {
        try {
            this.storeLock.writeLock().lock();
            return this.store.remove(id);
        } finally {
            this.storeLock.writeLock().unlock();
        }
    }

    public void loadUnloaded(UUID uuid, T object) {
        loadUnloadedInternal(uuid, object);
    }

    protected boolean loadUnloadedInternal(UUID uuid, T object) {
        Unloaded<T> unloadedData = this.unloadedStore.remove(uuid);
        if (unloadedData != null) {
            unloadedData.load(object);
            return true;
        }
        return false;
    }

    public void storeUnloaded(UUID uuid, Unloaded<T> unloaded) {
        this.unloadedStore.put(uuid, unloaded);
    }

    public List<T> getAll() {
        if(this.store.isEmpty())
            return Collections.emptyList();

        List<T> all = new LinkedList<>();
        collect(all);
        return all;
    }

    public void collect(List<? super T> list) {
        if (this.store.isEmpty())
            return;

        try {
            this.storeLock.readLock().lock();
            list.addAll(this.store.values());
        } finally {
            this.storeLock.readLock().unlock();
        }
    }

    public int size() {
        return this.store.size();
    }

    public int sizeUnloaded() {
        return this.unloadedStore.size();
    }

    private static <T> Map<Integer, T> createStore() {
        try {
            return new it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap<>();
        } catch (Throwable error) {
            return new LinkedHashMap<>();
        }
    }

    public interface Unloaded<T> {

        void load(T object);

    }

}
