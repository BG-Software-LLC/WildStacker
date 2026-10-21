package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("unchecked")
public final class EntityStorage {

    private static final Map<UUID, FlagsMap> entityStorage = new ConcurrentHashMap<>();

    public static void setMetadata(Entity entity, EntityFlag entityFlag, Object value) {
        setMetadata(entity.getUniqueId(), entityFlag, value);
    }

    public static void setMetadata(UUID entityUUID, EntityFlag entityFlag, Object value) {
        entityStorage.computeIfAbsent(entityUUID, s -> new FlagsMap()).put(entityFlag, value);
    }

    public static boolean hasMetadata(Entity entity, EntityFlag entityFlag) {
        return hasMetadata(entity.getUniqueId(), entityFlag);
    }

    public static boolean hasMetadata(UUID entityUUID, EntityFlag entityFlag) {
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
        FlagsMap flagsMap = entityStorage.get(entityUUID);
        return flagsMap == null ? null : (T) entityFlag.getValueClass().cast(flagsMap.getOrDefault(entityFlag, def));
    }

    public static <T> T removeMetadata(Entity entity, EntityFlag entityFlag) {
        return removeMetadata(entity.getUniqueId(), entityFlag);
    }

    public static <T> T removeMetadata(UUID entityUUID, EntityFlag entityFlag) {
        return removeMetadata(entityUUID, entityFlag, null);
    }

    public static <T> T removeMetadata(Entity entity, EntityFlag entityFlag, T def) {
        return removeMetadata(entity.getUniqueId(), entityFlag, def);
    }

    public static <T> T removeMetadata(UUID entityUUID, EntityFlag entityFlag, T def) {
        FlagsMap flagsMap = entityStorage.get(entityUUID);

        if (flagsMap != null) {
            Object retVal = flagsMap.remove(entityFlag);
            if(retVal != null)
                return (T) entityFlag.getValueClass().cast(retVal);
        }

        return def;
    }

    public static void clearMetadata(Entity entity) {
        clearMetadata(entity.getUniqueId());
    }

    public static void clearMetadata(UUID entityUUID) {
        entityStorage.computeIfPresent(entityUUID, (uuid, flagsMap) -> {
            flagsMap.cancelMetadataRemoval();
            return null;
        });
    }

    public static void cancelMetadataRemoval(Entity entity) {
        entityStorage.computeIfPresent(entity.getUniqueId(), (uuid, flagsMap) -> {
            flagsMap.cancelMetadataRemoval();
            return flagsMap;
        });
    }

    public static void clearMetadata(Entity entity, long delay) {
        UUID entityUUID = entity.getUniqueId();
        entityStorage.computeIfPresent(entityUUID, (uuid, flagsMap) -> {
            flagsMap.cancelMetadataRemoval();
            MetadataRemovalTask removalTask = new MetadataRemovalTask(entityUUID);
            removalTask.task = Executor.syncTask(removalTask, delay);
            flagsMap.removalTask = removalTask.task;
            return flagsMap;
        });
    }

    public static void clearCache() {
        entityStorage.keySet().forEach(EntityStorage::clearMetadata);
    }

    private static class MetadataRemovalTask implements Runnable {

        private final UUID entityUUID;
        private BukkitTask task;

        private MetadataRemovalTask(UUID entityUUID) {
            this.entityUUID = entityUUID;
        }

        @Override
        public void run() {
            entityStorage.computeIfPresent(entityUUID, (uuid, flagsMap) ->
                    flagsMap.removalTask == task ? null : flagsMap);
        }

    }

    private static class FlagsMap {

        private BukkitTask removalTask;
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final Object[] values = new Object[EntityFlag.values().length];

        private void cancelMetadataRemoval() {
            if (removalTask != null) {
                removalTask.cancel();
                removalTask = null;
            }
        }

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

        public Object remove(EntityFlag entityFlag) {
            return put(entityFlag, null);
        }

    }

}
