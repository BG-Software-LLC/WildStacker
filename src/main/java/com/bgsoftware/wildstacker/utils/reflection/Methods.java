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
    ENTITY_SOUND_PITCH,
    ENTITY_ALWAYS_GIVES_EXP,
    ENTITY_IS_DROP_EXPERIENCE,
    ENTITY_GET_ITEM_IN_MAIN_HAND_DROP_CHANCE,
    ENTITY_SET_ITEM_IN_MAIN_HAND,
    ENTITY_GET_ITEM_IN_OFF_HAND_DROP_CHANCE,
    ENTITY_SET_ITEM_IN_OFF_HAND;



    public boolean exists(){
        return ReflectionUtils.methodMap.get(this) != null;
    }

    public Object invoke(Object object, Object... args) {
        try {
            return object == null ? null : ReflectionUtils.methodMap.get(this).invoke(object, args);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
