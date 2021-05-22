package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import org.bukkit.entity.Entity;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public final class EntityStorage {

    private static final Map<UUID, EnumMap<EntityFlag, Object>> entityStorage = new HashMap<>();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    public static void setMetadata(Entity entity, EntityFlag entityFlag, Object value){
        UUID uuid = entity.getUniqueId();
        write(entityStorage -> entityStorage.computeIfAbsent(uuid, s -> new EnumMap<>(EntityFlag.class)).put(entityFlag, value));
    }

    public static boolean hasMetadata(Entity entity, EntityFlag entityFlag){
        EnumMap<EntityFlag, Object> map = read(entityStorage -> entityStorage.get(entity.getUniqueId()));
        return map != null && map.get(entityFlag) != null;
    }

    public static <T> T getMetadata(Entity entity, EntityFlag entityFlag){
        UUID uuid = entity.getUniqueId();
        return read(entityStorage -> {
            EnumMap<EntityFlag, Object> map = entityStorage.get(uuid);
            return map == null ? null : (T) entityFlag.getValueClass().cast(map.get(entityFlag));
        });
    }

    public static void removeMetadata(Entity entity, EntityFlag entityFlag){
        UUID uuid = entity.getUniqueId();
        write(entityStorage -> {
            EnumMap<EntityFlag, Object> map = entityStorage.get(uuid);
            if(map != null)
                map.remove(entityFlag);
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

    private static void write(Consumer<Map<UUID, EnumMap<EntityFlag, Object>>> consumer){
        try{
            lock.writeLock().lock();
            consumer.accept(entityStorage);
        }finally {
            lock.writeLock().unlock();
        }
    }

    private static <R> R writeAndGet(Function<Map<UUID, EnumMap<EntityFlag, Object>>, R> function){
        try{
            lock.writeLock().lock();
            return function.apply(entityStorage);
        }finally {
            lock.writeLock().unlock();
        }
    }

    private static <R> R read(Function<Map<UUID, EnumMap<EntityFlag, Object>>, R> function){
        try{
            lock.readLock().lock();
            return function.apply(entityStorage);
        }finally {
            lock.readLock().unlock();
        }
    }

}
