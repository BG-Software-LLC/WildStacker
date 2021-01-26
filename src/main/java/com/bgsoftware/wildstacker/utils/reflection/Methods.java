package com.bgsoftware.wildstacker.utils.reflection;

public enum Methods {

    ENTITY_SOUND_DEATH,
    ENTITY_SOUND_VOLUME,
    ENTITY_SOUND_PITCH,
    ENTITY_ALWAYS_GIVES_EXP,
    ENTITY_IS_DROP_EXPERIENCE,

    WORLD_GET_CHUNK_IF_LOADED_PAPER,

    HUMAN_GET_SHOULDER_ENTITY_RIGHT,
    HUMAN_SET_SHOULDER_ENTITY_RIGHT,
    HUMAN_GET_SHOULDER_ENTITY_LEFT,
    HUMAN_SET_SHOULDER_ENTITY_LEFT;

    public Object invoke(Object object, Object... args) {
        try {
            return object == null ? null : ReflectionUtils.methodMap.get(this).invoke(object, args);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public boolean isValid(){
        return ReflectionUtils.methodMap.get(this) != null;
    }

}
