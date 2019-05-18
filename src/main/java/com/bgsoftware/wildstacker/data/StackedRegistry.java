package com.bgsoftware.wildstacker.data;

import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class StackedRegistry<K, V> implements Iterable<V>{

    private final Map<Chunk, Map<K, V>> registryMap = new ConcurrentHashMap<>();

    public void put(Chunk chunk, K key, V value){
        if(!registryMap.containsKey(chunk))
            registryMap.put(chunk, new ConcurrentHashMap<>());
        getMap(chunk).put(key, value);
    }

    public boolean contains(Chunk chunk, K key){
        return getMap(chunk).containsKey(key);
    }

    public void remove(Chunk chunk, K key){
        getMap(chunk).remove(key);
    }

    public void remove(Chunk chunk){
        registryMap.remove(chunk);
    }

    @Nullable
    public V get(Chunk chunk, K key){
        return getMap(chunk).get(key);
    }

    @NotNull
    public Set<Chunk> getChunks(){
        return registryMap.keySet();
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        Map<K, V> valuesMap = new HashMap<>();

        for(Map<K, V> map : registryMap.values())
            valuesMap.putAll(map);

        return valuesMap.values().iterator();
    }

    public Iterator<V> iterator(Chunk chunk) {
        return getMap(chunk).values().iterator();
    }

    public Iterator<Map.Entry<K, V>> entryIterator(Chunk chunk){
        return getMap(chunk).entrySet().iterator();
    }

    @Override
    public String toString() {
        return registryMap.toString();
    }

    private Map<K, V> getMap(Chunk chunk){
        return registryMap.getOrDefault(chunk, new ConcurrentHashMap<>());
    }

}
