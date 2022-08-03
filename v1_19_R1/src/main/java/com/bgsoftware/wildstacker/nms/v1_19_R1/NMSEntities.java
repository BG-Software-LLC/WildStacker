package com.bgsoftware.wildstacker.nms.v1_19_R1;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.utils.entity.StackCheck;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import org.bukkit.entity.Frog;
import org.bukkit.entity.LivingEntity;

import java.util.Objects;

public final class NMSEntities implements com.bgsoftware.wildstacker.nms.NMSEntities {

    @Override
    public StackCheckResult areSimilar(EntityTypes entityType, LivingEntity en1, LivingEntity en2) {
        if (StackCheck.FROG_TOUNGE_TARGET.isEnabled() && StackCheck.FROG_TOUNGE_TARGET.isTypeAllowed(entityType)) {
            if (!Objects.equals(((Frog) en1).getTongueTarget(), ((Frog) en2).getTongueTarget()))
                return StackCheckResult.FROG_TOUNGE_TARGET;
        }

        if (StackCheck.FROG_TYPE.isEnabled() && StackCheck.FROG_TYPE.isTypeAllowed(entityType)) {
            if (((Frog) en1).getVariant() != ((Frog) en2).getVariant())
                return StackCheckResult.FROG_TYPE;
        }

        return StackCheckResult.SUCCESS;
    }

}
