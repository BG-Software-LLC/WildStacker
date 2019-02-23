package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.StackCheck;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class EntityData {

    private static WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private Object nbtTagCompound;
    private List<MetadataValue> epicSpawners;

    private EntityData(){
        nbtTagCompound = null;
        epicSpawners = null;
    }

    public void loadEntityData(LivingEntity livingEntity){
        nbtTagCompound = plugin.getNMSAdapter().getNBTTagCompound(livingEntity);
        if(livingEntity.hasMetadata("ES"))
            epicSpawners = livingEntity.getMetadata("ES");
    }

    public void applyEntityData(LivingEntity livingEntity){
        if(nbtTagCompound != null)
            plugin.getNMSAdapter().setNBTTagCompound(livingEntity, nbtTagCompound);
        if(epicSpawners != null)
            epicSpawners.forEach(value -> livingEntity.setMetadata("ES", value));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof EntityData){
            EntityData other = (EntityData) obj;
            Map map, otherMap;

            try {
                Class nbtTagClass = ReflectionUtil.getNMSClass("NBTTagCompound");
                map = (Map) ReflectionUtil.getField("map", nbtTagClass).get(nbtTagCompound);
                otherMap = (Map) ReflectionUtil.getField("map", nbtTagClass).get(other.nbtTagCompound);
            }catch(Exception ex){
                ex.printStackTrace();
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

    private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static boolean asIntField = version.equals("v1_13_R2");
    private static boolean dField = version.contains("v1_9") || version.contains("v1_8");

    private int getInteger(Object object){
        try{
            String fieldName = asIntField ? "asInt" : dField ? "d" : "e";
            return (int) object.getClass().getMethod(fieldName).invoke(object);
        }catch(Exception ex){
            ex.printStackTrace();
            return 0;
        }
    }

    public static EntityData of(StackedEntity stackedEntity){
        return of(stackedEntity.getLivingEntity());
    }

    public static EntityData of(LivingEntity livingEntity){
        EntityData entityData = new EntityData();
        entityData.loadEntityData(livingEntity);
        return entityData;
    }

}
