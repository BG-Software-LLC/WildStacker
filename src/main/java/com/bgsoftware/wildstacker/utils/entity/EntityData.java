package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
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

    public StackCheckResult match(EntityData other){
        Map map = Fields.NBT_TAG_MAP.get(nbtTagCompound, Map.class);
        Map otherMap = Fields.NBT_TAG_MAP.get(other.nbtTagCompound, Map.class);

        if(StackCheck.AGE.isEnabled() && map.containsKey("Age")){
            if((getInteger(map, "Age") >= 0) != (getInteger(otherMap, "Age") >= 0)) //isAdult
                return StackCheckResult.AGE;
        }

        if(StackCheck.ZOMBIE_PIGMAN_ANGRY.isEnabled() && StackCheck.ZOMBIE_PIGMAN_ANGRY.isTypeAllowed(entityType) && map.containsKey("Anger")){
            if((getInteger(map, "Anger") >= 0) != (getInteger(otherMap, "Anger") >= 0)) //canBreed
                return StackCheckResult.ZOMBIE_PIGMAN_ANGRY;
        }

        if(StackCheck.ENDERMAN_CARRIED_BLOCK.isEnabled() && StackCheck.ENDERMAN_CARRIED_BLOCK.isTypeAllowed(entityType)){
            if(map.containsKey("carried")){
                if(!get(map, "carried").equals(get(otherMap, "carried")))
                    return StackCheckResult.ENDERMAN_CARRIED_BLOCK;
            }
            if(map.containsKey("carriedData")){
                if(!get(map, "carriedData").equals(get(otherMap, "carriedData")))
                    return StackCheckResult.ENDERMAN_CARRIED_BLOCK;
            }
            if(map.containsKey("carriedBlockState")){
                if(!get(map, "carriedBlockState").equals(get(otherMap, "carriedBlockState")))
                    return StackCheckResult.ENDERMAN_CARRIED_BLOCK;
            }
        }

        if(map.containsKey("Variant")){
            int firstVariant = getInteger(map, "Variant"), secondVariant = getInteger(otherMap, "Variant");
            if(StackCheck.HORSE_COLOR.isEnabled() && StackCheck.HORSE_COLOR.isTypeAllowed(entityType)){
                if(firstVariant != secondVariant)
                    return StackCheckResult.HORSE_COLOR;
            }
            if (StackCheck.HORSE_STYLE.isEnabled() && StackCheck.HORSE_STYLE.isTypeAllowed(entityType)){
                if((firstVariant >>> 8) != (secondVariant >>> 8))
                    return StackCheckResult.HORSE_STYLE;
            }
            if (StackCheck.TROPICALFISH_BODY_COLOR.isEnabled() && StackCheck.TROPICALFISH_BODY_COLOR.isTypeAllowed(entityType)){
                if((firstVariant >> 16 & 255) != (secondVariant >> 16 & 255))
                    return StackCheckResult.TROPICALFISH_BODY_COLOR;
            }
            if(StackCheck.TROPICALFISH_TYPE_COLOR.isEnabled() && StackCheck.TROPICALFISH_TYPE_COLOR.isTypeAllowed(entityType)){
                if((firstVariant >> 24 & 255) != (secondVariant >> 24 & 255))
                    return StackCheckResult.TROPICALFISH_BODY_COLOR;
            }
        }

        for(StackCheck stackCheck : StackCheck.values()){
            if(stackCheck.isEnabled() && stackCheck.isTypeAllowed(entityType)){
                for(String key : stackCheck.getCompoundKeys()){
                    if(!key.isEmpty() && !get(map, key).equals(get(otherMap, key))){
                        return StackCheckResult.valueOf(stackCheck.name());
                    }
                }
            }
        }

        return StackCheckResult.SUCCESS;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityData && match((EntityData) obj) == StackCheckResult.SUCCESS;
    }

    private Object get(Map map, String key){
        Object value = map.get(key);
        return value == null ? NULL : value;
    }

    private int getInteger(Map map, String key){
        Object value = get(map, key);
        return value == NULL ? 0 : plugin.getNMSAdapter().getNBTInteger(value);
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
