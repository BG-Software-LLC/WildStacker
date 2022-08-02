package com.bgsoftware.wildstacker.nms.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Remap {

    String classPath();

    String name();

    Type type();

    String remappedName() default "";

    enum Type {

        FIELD,
        METHOD

    }


}
