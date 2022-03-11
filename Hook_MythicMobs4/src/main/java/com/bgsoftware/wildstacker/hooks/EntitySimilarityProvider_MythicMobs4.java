package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;

public final class EntitySimilarityProvider_MythicMobs4 implements EntitySimilarityProvider {

    @Override
    public StackCheckResult areSimilar(Entity entity, Entity other) {
        ActiveMob activeMob1 = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        ActiveMob activeMob2 = MythicMobs.inst().getMobManager().getActiveMob(other.getUniqueId()).orElse(null);

        if ((activeMob1 == null) != (activeMob2 == null))
            return StackCheckResult.MYTHIC_MOB_TYPE;

        try {
            if(activeMob1 != null && !activeMob1.getType().getInternalName()
                    .equals(activeMob2.getType().getInternalName()))
                return StackCheckResult.MYTHIC_MOB_TYPE;
        } catch (Exception ignored){
        }

        return StackCheckResult.SUCCESS;
    }

}
