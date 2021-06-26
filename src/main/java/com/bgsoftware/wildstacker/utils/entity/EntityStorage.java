package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("unchecked")
public final class EntityStorage {

    private static final Map<UUID, FlagsMap> entityStorage = new ConcurrentHashMap<>();
    private static List<UUID> trackUUIDs = new ArrayList<>();

    public static void setMetadata(Entity entity, EntityFlag entityFlag, Object value) {
        setMetadata(entity.getUniqueId(), entityFlag, value);
    }

    public static void setMetadata(UUID entityUUID, EntityFlag entityFlag, Object value) {
        if(entityFlag == EntityFlag.DEMO_ENTITY)
            trackUUIDs.add(entityUUID);

        if(trackUUIDs.contains(entityUUID))
            Bukkit.broadcastMessage(String.format("setMetadata(%s, %s, %s)", entityUUID + "", entityFlag + "", value + ""));

        entityStorage.computeIfAbsent(entityUUID, s -> new FlagsMap()).put(entityFlag, value);
    }

    public static boolean hasMetadata(Entity entity, EntityFlag entityFlag) {
        return hasMetadata(entity.getUniqueId(), entityFlag);
    }

    public static boolean hasMetadata(UUID entityUUID, EntityFlag entityFlag) {
        if(trackUUIDs.contains(entityUUID))
            Bukkit.broadcastMessage(String.format("hasMetadata(%s, %s)", entityUUID + "", entityFlag + ""));

        FlagsMap flagsMap = entityStorage.get(entityUUID);
        return flagsMap != null && flagsMap.containsKey(entityFlag);
    }

    public static <T> T getMetadata(Entity entity, EntityFlag entityFlag) {
        return getMetadata(entity.getUniqueId(), entityFlag);
    }

    public static <T> T getMetadata(UUID entityUUID, EntityFlag entityFlag) {
        return getMetadata(entityUUID, entityFlag, null);
    }

    public static <T> T getMetadata(Entity entity, EntityFlag entityFlag, T def) {
        return getMetadata(entity.getUniqueId(), entityFlag, def);
    }

    public static <T> T getMetadata(UUID entityUUID, EntityFlag entityFlag, T def) {
        if(trackUUIDs.contains(entityUUID))
            Bukkit.broadcastMessage(String.format("getMetadata(%s, %s, %s)", entityUUID + "", entityFlag + "", def + ""));

        FlagsMap flagsMap = entityStorage.get(entityUUID);
        return flagsMap == null ? null : (T) entityFlag.getValueClass().cast(flagsMap.getOrDefault(entityFlag, def));
    }

    public static void removeMetadata(Entity entity, EntityFlag entityFlag) {
        removeMetadata(entity.getUniqueId(), entityFlag);
    }

    public static void removeMetadata(UUID entityUUID, EntityFlag entityFlag) {
        if(trackUUIDs.contains(entityUUID))
            Bukkit.broadcastMessage(String.format("removeMetadata(%s, %s)", entityUUID + "", entityFlag + ""));

        FlagsMap flagsMap = entityStorage.get(entityUUID);
        if(flagsMap != null)
            flagsMap.remove(entityFlag);
    }

    public static void clearMetadata(Entity entity) {
        clearMetadata(entity.getUniqueId());
    }

    public static void clearMetadata(UUID entityUUID) {
        if(trackUUIDs.contains(entityUUID))
            Bukkit.broadcastMessage(String.format("clearMetadata(%s)", entityUUID + ""));

        entityStorage.remove(entityUUID);
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
