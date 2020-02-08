package com.bgsoftware.wildstacker.utils.reflection;

public enum Methods {

    ENTITY_SOUND_DEATH,
    ENTITY_SOUND_VOLUME,
    ENTITY_SOUND_PITCH,
    ENTITY_ALWAYS_GIVES_EXP,
    ENTITY_IS_DROP_EXPERIENCE;

    public Object invoke(Object object, Object... args) {
        try {
            return object == null ? null : ReflectionUtils.methodMap.get(this).invoke(object, args);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
