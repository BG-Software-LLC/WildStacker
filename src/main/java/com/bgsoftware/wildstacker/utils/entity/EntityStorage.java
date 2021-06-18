package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("unchecked")
public final class EntityStorage {

    private static final Map<UUID, FlagsMap> entityStorage = new ConcurrentHashMap<>();

    public static void setMetadata(Entity entity, EntityFlag entityFlag, Object value) {
        entityStorage.computeIfAbsent(entity.getUniqueId(), s -> new FlagsMap()).put(entityFlag, value);
    }

    public static boolean hasMetadata(Entity entity, EntityFlag entityFlag) {
        FlagsMap flagsMap = entityStorage.get(entity.getUniqueId());
        return flagsMap != null && flagsMap.containsKey(entityFlag);
    }

    public static <T> T getMetadata(Entity entity, EntityFlag entityFlag) {
        return getMetadata(entity, entityFlag, null);
    }

    public static <T> T getMetadata(Entity entity, EntityFlag entityFlag, T def) {
        FlagsMap flagsMap = entityStorage.get(entity.getUniqueId());
        return flagsMap == null ? null : (T) entityFlag.getValueClass().cast(flagsMap.getOrDefault(entityFlag, def));
    }

    public static void removeMetadata(Entity entity, EntityFlag entityFlag) {
        FlagsMap flagsMap = entityStorage.get(entity.getUniqueId());
        if(flagsMap != null)
            flagsMap.remove(entityFlag);
    }

    public static void clearMetadata(Entity entity) {
        entityStorage.remove(entity.getUniqueId());
    }

    public static void clearCache() {
        entityStorage.clear();
    }

    private static class FlagsMap {

        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final Object[] values = new Object[EntityFlag.values().length];

        public Object get(EntityFlag entityFlag) {
            return getOrDefault(entityFlag, null);
        }

        public Object getOrDefault(EntityFlag entityFlag, Object defaultValue) {
            try {
                lock.readLock().lock();
                Object value = values[entityFlag.ordinal()];
                return value == null ? defaultValue : value;
            } finally {
                lock.readLock().unlock();
            }
        }

        public boolean containsKey(EntityFlag entityFlag) {
            try {
                lock.readLock().lock();
                return values[entityFlag.ordinal()] != null;
            } finally {
                lock.readLock().unlock();
            }
        }

        public Object put(EntityFlag entityFlag, Object value) {
            try {
                lock.writeLock().lock();
                Object oldValue = values[entityFlag.ordinal()];
                values[entityFlag.ordinal()] = value;
                return oldValue;
            } finally {
                lock.writeLock().unlock();
            }
        }

        public Object remove(EntityFlag entityFlag){
            return put(entityFlag, null);
        }

    }

}
