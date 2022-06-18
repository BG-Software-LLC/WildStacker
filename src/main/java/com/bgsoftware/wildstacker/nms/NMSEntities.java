package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import org.bukkit.entity.LivingEntity;

public interface NMSEntities {

    StackCheckResult areSimilar(EntityTypes entityType, LivingEntity en1, LivingEntity en2);

}
