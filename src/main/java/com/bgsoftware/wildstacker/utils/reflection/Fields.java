package com.bgsoftware.wildstacker.utils.reflection;

import java.lang.reflect.Field;

public enum Fields {

    ENTITY_LAST_DAMAGE_BY_PLAYER_TIME,
    ENTITY_EXP,
    ENTITY_SPAWNED_VIA_MOB_SPAWNER,
    ENTITY_FROM_MOB_SPAWNER,

    STRIDER_SADDLE_STORAGE,

    CHUNK_ENTITY_SLICES;

    public <T> T get(Object object, Class<T> clazz){
        try {
            return clazz.cast(getField().get(object));
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void set(Object object, Object value){
        try {
            getField().set(object, value);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public boolean exists(){
        return getField() != null;
    }

    private Field getField(){
        return ReflectionUtils.fieldMap.get(this);
    }

}
