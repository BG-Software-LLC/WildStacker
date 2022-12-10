package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;

@SuppressWarnings("unused")
public final class EntityNameProvider_MythicMobs5 implements EntityNameProvider {

    private final WildStackerPlugin plugin;

    public EntityNameProvider_MythicMobs5(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCustomName(StackedEntity stackedEntity) {
        if (stackedEntity.getSpawnCause() != SpawnCause.MYTHIC_MOBS)
            return null;

        LivingEntity livingEntity = stackedEntity.getLivingEntity();

        ActiveMob activeMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(livingEntity);

        try {
            return activeMob.getDisplayName();
        } catch (Throwable ignored) {
        }

        return plugin.getNMSEntities().getCustomName(livingEntity);
    }

}
