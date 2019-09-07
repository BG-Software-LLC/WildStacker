package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;

import java.util.List;

public final class GeneralUtils {

    public static boolean contains(List<String> list, StackedEntity stackedEntity){
        return list.contains(stackedEntity.getType().name()) ||  list.contains(stackedEntity.getSpawnCause().name()) ||
                list.contains(stackedEntity.getType().name() + ":" + stackedEntity.getSpawnCause().name());
    }

}
