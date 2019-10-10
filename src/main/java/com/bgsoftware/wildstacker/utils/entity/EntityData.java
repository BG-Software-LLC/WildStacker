package com.bgsoftware.wildstacker.utils.entity;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.utils.reflection.Fields;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@SuppressWarnings("WeakerAccess")
public final class EntityData {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static final Map<UUID, EntityData> cachedData = new WeakHashMap<>();

    private Object nbtTagCompound = null;
    private Object epicSpawners = null;

    private EntityData(){
    }

    public void loadEntityData(LivingEntity livingEntity){
        nbtTagCompound = plugin.getNMSAdapter().getNBTTagCompound(livingEntity);
        if(EntityStorage.hasMetadata(livingEntity, "ES"))
            epicSpawners = EntityStorage.getMetadata(livingEntity, "ES", Object.class);
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

            if(StackCheck.NAME_TAG.isEnabled()){
                if(map.containsKey("NameTag") || otherMap.containsKey("NameTag"))
                    return false;
            }

            if(StackCheck.AGE.isEnabled()){
                if(map.containsKey("Age") != otherMap.containsKey("Age"))
                    return false;
                if(map.containsKey("Age")) {
                    if((getInteger(map.get("Age")) >= 0) != (getInteger(otherMap.get("Age")) >= 0)) //isAdult
                        return false;
                }
            }

            if(StackCheck.CAN_BREED.isEnabled()){
                if(map.containsKey("Age") != otherMap.containsKey("Age"))
                    return false;
                if(map.containsKey("Age")) {
                    if ((getInteger(map.get("Age")) == 0) != (getInteger(otherMap.get("Age")) == 0)) //canBreed
                        return false;
                }
            }

            if(StackCheck.ZOMBIE_PIGMAN_ANGRY.isEnabled()){
                if(map.containsKey("Anger") != otherMap.containsKey("Anger"))
                    return false;
                if(map.containsKey("Anger")) {
                    if ((getInteger(map.get("Anger")) > 0) != (getInteger(otherMap.get("Anger")) > 0)) //canBreed
                        return false;
                }
            }

            if(StackCheck.ENDERMAN_CARRIED_BLOCK.isEnabled()){
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

            if(StackCheck.HORSE_COLOR.isEnabled()){
                if(map.containsKey("Variant") != otherMap.containsKey("Variant"))
                    return false;
                if(map.containsKey("Variant")){
                    if(((getInteger(map.get("Age")) & 255) != (getInteger(otherMap.get("Age")) & 255)))
                        return false;
                }
            }

            if(StackCheck.HORSE_STYLE.isEnabled()){
                if(map.containsKey("Variant") != otherMap.containsKey("Variant"))
                    return false;
                if(map.containsKey("Variant")){
                    if((getInteger(map.get("Age")) >>> 8) != (getInteger(otherMap.get("Age")) >>> 8))
                        return false;
                }
            }

            if(StackCheck.TROPICALFISH_BODY_COLOR.isEnabled()){
                if(map.containsKey("Variant") != otherMap.containsKey("Variant"))
                    return false;
                if(map.containsKey("Variant")){
                    if((getInteger(map.get("Age")) >> 16 & 255) != (getInteger(otherMap.get("Age")) >> 16 & 255))
                        return false;
                }
            }

            if(StackCheck.TROPICALFISH_TYPE_COLOR.isEnabled()){
                if(map.containsKey("Variant") != otherMap.containsKey("Variant"))
                    return false;
                if(map.containsKey("Variant")){
                    if((getInteger(map.get("Age")) >> 24 & 255) != (getInteger(otherMap.get("Age")) >> 24 & 255))
                        return false;
                }
            }

            for(StackCheck stackCheck : StackCheck.values()){
                if(stackCheck.isEnabled()){
                    for(String key : stackCheck.getCompoundKeys()){
                        if(!key.isEmpty()){
                            if(map.containsKey(key) != otherMap.containsKey(key))
                                return false;
                            if(map.containsKey(key) && !map.get(key).equals(otherMap.get(key)))
                                return false;
                        }
                    }
                }
            }

            return true;
        }
        return super.equals(obj);
    }

    private int getInteger(Object object){
        return object == null ? 0 : plugin.getNMSAdapter().getNBTInteger(object);
    }

    public static EntityData of(StackedEntity stackedEntity){
        return of(stackedEntity.getLivingEntity());
    }

    public static EntityData of(LivingEntity livingEntity){
        synchronized (cachedData){
            if(cachedData.containsKey(livingEntity.getUniqueId()))
                return cachedData.get(livingEntity.getUniqueId());
        }

        EntityData entityData = new EntityData();
        entityData.loadEntityData(livingEntity);

        synchronized (cachedData){
            cachedData.put(livingEntity.getUniqueId(), entityData);
        }

        return entityData;
    }

}
