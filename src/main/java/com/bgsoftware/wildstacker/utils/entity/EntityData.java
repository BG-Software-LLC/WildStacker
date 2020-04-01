package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.reflection.Fields;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("WeakerAccess")
public final class EntityData {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final Map<UUID, EntityData> cachedData = new WeakHashMap<>();
    private static final Object NULL = new Object();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private Object nbtTagCompound = null;
    private Object epicSpawners = null;
    private EntityTypes entityType = null;

    private EntityData(){
    }

    public void loadEntityData(LivingEntity livingEntity){
        nbtTagCompound = plugin.getNMSAdapter().getNBTTagCompound(livingEntity);
        if(EntityStorage.hasMetadata(livingEntity, "ES"))
            epicSpawners = EntityStorage.getMetadata(livingEntity, "ES", Object.class);
        entityType = EntityTypes.fromEntity(livingEntity);
    }

    public void applyEntityData(LivingEntity livingEntity){
        if(nbtTagCompound != null)
            plugin.getNMSAdapter().setNBTTagCompound(livingEntity, nbtTagCompound);
        if(epicSpawners != null)
            EntityStorage.setMetadata(livingEntity, "ES", epicSpawners);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof EntityData){
            EntityData other = (EntityData) obj;

            Map map = Fields.NBT_TAG_MAP.get(nbtTagCompound, Map.class);
            Map otherMap = Fields.NBT_TAG_MAP.get(other.nbtTagCompound, Map.class);

            if(StackCheck.AGE.isEnabled()){
                if(map.containsKey("Age") != otherMap.containsKey("Age"))
                    return false;
                if(map.containsKey("Age")) {
                    if((getInteger(map.get("Age")) >= 0) != (getInteger(otherMap.get("Age")) >= 0)) //isAdult
                        return false;
                }
            }

            if(StackCheck.ZOMBIE_PIGMAN_ANGRY.isEnabled() && StackCheck.ZOMBIE_PIGMAN_ANGRY.isTypeAllowed(entityType)){
                if(map.containsKey("Anger") != otherMap.containsKey("Anger"))
                    return false;
                if(map.containsKey("Anger")) {
                    if ((getInteger(map.get("Anger")) > 0) != (getInteger(otherMap.get("Anger")) > 0)) //canBreed
                        return false;
                }
            }

            if(StackCheck.ENDERMAN_CARRIED_BLOCK.isEnabled() && StackCheck.ENDERMAN_CARRIED_BLOCK.isTypeAllowed(entityType)){
                if(map.containsKey("carried") != otherMap.containsKey("carried") || map.containsKey("carriedData") != otherMap.containsKey("carriedData") ||
                        map.containsKey("carriedBlockState") != otherMap.containsKey("carriedBlockState"))
                    return false;
                if(map.containsKey("carried")){
                    if(!map.get("carried").equals(otherMap.get("carried")))
                        return false;
                }
                if(map.containsKey("carriedData")){
                    if(!map.get("carriedData").equals(otherMap.get("carriedData")))
                        return false;
                }
                if(map.containsKey("carriedBlockState")){
                    if(!map.get("carriedBlockState").equals(otherMap.get("carriedBlockState")))
                        return false;
                }
            }

            if (StackCheck.HORSE_COLOR.isEnabled() && StackCheck.HORSE_COLOR.isTypeAllowed(entityType)) {
                if (map.containsKey("Variant") != otherMap.containsKey("Variant"))
                    return false;
                if (map.containsKey("Variant")) {
                    if(getInteger(map.get("Variant")) != getInteger(otherMap.get("Variant")))
                        return false;
                }
            }

            if (StackCheck.HORSE_STYLE.isEnabled() && StackCheck.HORSE_STYLE.isTypeAllowed(entityType)) {
                if (map.containsKey("Variant") != otherMap.containsKey("Variant"))
                    return false;
                if (map.containsKey("Variant")) {
                    if ((getInteger(map.get("Variant")) >>> 8) != (getInteger(otherMap.get("Variant")) >>> 8))
                        return false;
                }
            }

            if (StackCheck.TROPICALFISH_BODY_COLOR.isEnabled() && StackCheck.TROPICALFISH_BODY_COLOR.isTypeAllowed(entityType)) {
                if (map.containsKey("Variant") != otherMap.containsKey("Variant"))
                    return false;
                if (map.containsKey("Variant")) {
                    if ((getInteger(map.get("Variant")) >> 16 & 255) != (getInteger(otherMap.get("Variant")) >> 16 & 255))
                        return false;
                }
            }

            if (StackCheck.TROPICALFISH_TYPE_COLOR.isEnabled() && StackCheck.TROPICALFISH_TYPE_COLOR.isTypeAllowed(entityType)) {
                if (map.containsKey("Variant") != otherMap.containsKey("Variant"))
                    return false;
                if (map.containsKey("Variant")) {
                    if ((getInteger(map.get("Variant")) >> 24 & 255) != (getInteger(otherMap.get("Variant")) >> 24 & 255))
                        return false;
                }
            }

            for(StackCheck stackCheck : StackCheck.values()){
                if(stackCheck.isEnabled() && stackCheck.isTypeAllowed(entityType)){
                    for(String key : stackCheck.getCompoundKeys()){
                        if(!key.isEmpty() && !get(map, key).equals(get(otherMap, key))){
                            return false;
                        }
                    }
                }
            }

            return true;
        }
        return super.equals(obj);
    }

    private Object get(Map map, String key){
        Object value = map.get(key);
        return value == null ? NULL : value;
    }

    private int getInteger(Object object){
        return object == null ? 0 : plugin.getNMSAdapter().getNBTInteger(object);
    }

    public static EntityData of(StackedEntity stackedEntity){
        return of(stackedEntity.getLivingEntity());
    }

    public static EntityData of(LivingEntity livingEntity){
        EntityData entityData = get(livingEntity.getUniqueId());

        if (entityData != null)
            return entityData;

        entityData = new EntityData();
        entityData.loadEntityData(livingEntity);

        put(livingEntity.getUniqueId(), entityData);

        return entityData;
    }

    public static void uncache(UUID uuid){
        try{
            lock.writeLock().lock();
            cachedData.remove(uuid);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static EntityData get(UUID uuid){
        try{
            lock.readLock().lock();
            return cachedData.get(uuid);
        } finally {
            lock.readLock().unlock();
        }
    }

    private static void put(UUID uuid, EntityData entityData){
        try{
            lock.writeLock().lock();
            cachedData.put(uuid, entityData);
        } finally {
            lock.writeLock().unlock();
        }
    }

}
