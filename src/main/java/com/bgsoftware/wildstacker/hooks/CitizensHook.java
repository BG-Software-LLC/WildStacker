package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import org.bukkit.entity.Entity;

public final class CitizensHook {

    public static boolean isNPC(Entity entity){
        StackedEntity stackedEntity = WStackedEntity.of(entity);
        return stackedEntity.getSpawnCause() == SpawnCause.CITIZENS ||
                stackedEntity.getLivingEntity().hasMetadata("NPC");
    }

}
