package com.bgsoftware.wildstacker.utils.reflection;

import java.lang.reflect.Field;

public enum Fields {

    ENTITY_LAST_DAMAGE_BY_PLAYER_TIME,
    ENTITY_EXP,
    ENTITY_KILLER,
    ENTITY_DEAD,

    NBT_TAG_MAP;

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

    private Field getField(){
        return ReflectionUtil.fieldMap.get(this);
    }

}
