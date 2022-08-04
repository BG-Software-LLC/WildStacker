package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

public interface NMSEntities {

    default StackCheckResult areSimilar(EntityTypes entityType, LivingEntity en1, LivingEntity en2) {
        return StackCheckResult.SUCCESS;
    }

    boolean checkEntityAttributes(LivingEntity livingEntity, Map<String, Object> attributes);

}
