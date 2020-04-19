package com.bgsoftware.wildstacker.utils.entity;

import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class EntityStorage {

    private static WeakHashMap<UUID, Map<String, Object>> entityStorage = new WeakHashMap<>();

    public static void setMetadata(Entity entity, String key, Object value){
        UUID uuid = entity.getUniqueId();
        if(!entityStorage.containsKey(uuid))
            entityStorage.put(uuid, new HashMap<>());

        entityStorage.get(uuid).put(key, value);
    }

    public static boolean hasMetadata(Entity entity, String key){
        UUID uuid = entity.getUniqueId();
        return entityStorage.containsKey(uuid) && entityStorage.get(uuid).containsKey(key);
    }

    public static <T> T getMetadata(Entity entity, String key, Class<T> type){
        UUID uuid = entity.getUniqueId();
        return hasMetadata(entity, key) ? type.cast(entityStorage.get(uuid).get(key)) : null;
    }

    public static void removeMetadata(Entity entity, String key){
        UUID uuid = entity.getUniqueId();
        if(entityStorage.containsKey(uuid))
            entityStorage.get(uuid).remove(key);
    }

    public static void clearMetadata(Entity entity){
        Map<?, ?> map = entityStorage.remove(entity.getUniqueId());
        map.clear();
    }

    public static void clearCache(){
        for(Map map : entityStorage.values())
            map.clear();
        entityStorage.clear();
    }

}
