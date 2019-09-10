package com.bgsoftware.wildstacker.utils;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.key.KeyMap;

import java.util.List;

public final class GeneralUtils {

    public static boolean contains(List<String> list, StackedEntity stackedEntity){
        return list.contains(stackedEntity.getType().name()) ||  list.contains(stackedEntity.getSpawnCause().name()) ||
                list.contains(stackedEntity.getType().name() + ":" + stackedEntity.getSpawnCause().name()) ||
                list.contains("all") || list.contains("ALL");
    }

    public static int get(KeyMap<Integer> map, StackedEntity stackedEntity, int def){
        if(map.containsKey(stackedEntity.getType().name()))
            return map.get(stackedEntity.getType().name());
        if(map.containsKey(stackedEntity.getSpawnCause().name()))
            return map.get(stackedEntity.getSpawnCause().name());
        if(map.containsKey(stackedEntity.getType().name() + ":" + stackedEntity.getSpawnCause().name()))
            return map.get(stackedEntity.getType().name() + ":" + stackedEntity.getSpawnCause().name());
        if(map.containsKey("all"))
            return map.get("all");
        return def;
    }

}
