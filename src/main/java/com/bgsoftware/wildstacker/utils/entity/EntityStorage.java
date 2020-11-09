package com.bgsoftware.wildstacker.utils.entity;

import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

public final class EntityStorage {

    private static final WeakHashMap<UUID, Map<String, Object>> entityStorage = new WeakHashMap<>();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    public static void setMetadata(Entity entity, String key, Object value){
        UUID uuid = entity.getUniqueId();
        write(entityStorage -> entityStorage.computeIfAbsent(uuid, s -> new HashMap<>()).put(key, value));
    }

    public static boolean hasMetadata(Entity entity, String key){
        Map<String, Object> map = read(entityStorage -> entityStorage.get(entity.getUniqueId()));
        return map != null && map.containsKey(key);
    }

    public static <T> T getMetadata(Entity entity, String key, Class<T> type){
        UUID uuid = entity.getUniqueId();
        return read(entityStorage -> {
            Map<String, Object> map = entityStorage.get(uuid);
            return map == null ? null : type.cast(map.get(key));
        });
    }

    public static void removeMetadata(Entity entity, String key){
        UUID uuid = entity.getUniqueId();
        write(entityStorage -> {
            Map<String, Object> map = entityStorage.get(uuid);
            if(map != null)
                map.remove(key);
        });
    }

    public static void clearMetadata(Entity entity){
        Map<?, ?> map = writeAndGet(entityStorage -> entityStorage.remove(entity.getUniqueId()));
        if(map != null)
            map.clear();
    }

    public static void clearCache(){
        write(entityStorage -> {
            for(Map map : entityStorage.values())
                map.clear();
            entityStorage.clear();
        });
    }

    private static void write(Consumer<WeakHashMap<UUID, Map<String, Object>>> consumer){
        try{
            lock.writeLock().lock();
            consumer.accept(entityStorage);
        }finally {
            lock.writeLock().unlock();
        }
    }

    private static <R> R writeAndGet(Function<WeakHashMap<UUID, Map<String, Object>>, R> function){
        try{
            lock.writeLock().lock();
            return function.apply(entityStorage);
        }finally {
            lock.writeLock().unlock();
        }
    }

    private static <R> R read(Function<WeakHashMap<UUID, Map<String, Object>>, R> function){
        try{
            lock.readLock().lock();
            return function.apply(entityStorage);
        }finally {
            lock.readLock().unlock();
        }
    }

}
