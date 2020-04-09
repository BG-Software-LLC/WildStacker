package com.bgsoftware.wildstacker.utils.cache;

import com.bgsoftware.wildstacker.utils.pair.MutablePair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class TTLCache<K, V> {

    private final Map<K, MutablePair<V, Integer>> elements = new ConcurrentHashMap<>();
    private final Map<Integer, Set<K>> timers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService ttl = Executors.newSingleThreadScheduledExecutor();

    private final AtomicInteger currentTime = new AtomicInteger(0);

    public TTLCache(){
        this.ttl.scheduleAtFixedRate(() -> {
            int currentTime = this.currentTime.get();

            Set<K> elementsToRemove = timers.get(currentTime);

            if(elementsToRemove != null){
                elementsToRemove.forEach(elements::remove);
                timers.remove(currentTime);
            }

            this.currentTime.set(currentTime + 1);
        }, 1, 1, TimeUnit.SECONDS);
    }

    public V put(K key, V value, int ttl){
        MutablePair<V, Integer> result = elements.put(key, new MutablePair<>(value, -1));
        refreshTTL(key, ttl);
        return result == null ? null : result.getKey();
    }

    public V remove(K key){
        MutablePair<V, Integer> result = elements.remove(key);
        return result == null ? null : result.getKey();
    }

    public boolean containsKey(K key){
        return elements.containsKey(key);
    }

    public V get(K key){
        MutablePair<V, Integer> result = elements.get(key);
        return result == null ? null : result.getKey();
    }

    public int size(){
        return elements.size();
    }

    public void clear(){
        elements.clear();
        timers.clear();
    }

    public void refreshTTL(K key, int ttl){
        MutablePair<V, Integer> pair = elements.get(key);
        Set<K> set = pair == null ? null : timers.get(pair.getValue());

        if(set == null)
            return;

        set.remove(key);
        int newTime = this.currentTime.get() + ttl;
        pair.setValue(newTime);
        timers.computeIfAbsent(newTime, s -> new HashSet<>()).add(key);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        clear();
        ttl.shutdown();
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}
