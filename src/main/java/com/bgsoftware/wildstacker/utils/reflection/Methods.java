package com.bgsoftware.wildstacker.utils.reflection;

public enum Methods {

    BLOCK_GET_BY_COMBINED_ID,

    MAGIC_GET_BLOCK,

    BLOCK_DATA_FROM_DATA,

    WORLD_CREATE_ENTITY,
    WORLD_ADD_ENTITY,

    ENTITY_GET_BUKKIT_ENTITY,
    ENTITY_GET_HANDLE,
    ENTITY_SOUND_DEATH,
    ENTITY_SOUND_VOLUME,
    ENTITY_SOUND_PITCH;



    public boolean exists(){
        return ReflectionUtil.methodMap.get(this) != null;
    }

    public Object invoke(Object object, Object... args) {
        try {
            return object == null ? null : ReflectionUtil.methodMap.get(this).invoke(object, args);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
