package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public final class EntityNameProvider_MythicMobs4 implements EntityNameProvider {

    private final WildStackerPlugin plugin;

    public EntityNameProvider_MythicMobs4(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public String getCustomName(StackedEntity stackedEntity) {
        if(stackedEntity.getSpawnCause() != SpawnCause.MYTHIC_MOBS)
            return null;

        LivingEntity livingEntity = stackedEntity.getLivingEntity();

        ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);

        try {
            return activeMob.getDisplayName();
        } catch (Throwable ignored) {
        }

        return plugin.getNMSAdapter().getCustomName(livingEntity);
    }

}
