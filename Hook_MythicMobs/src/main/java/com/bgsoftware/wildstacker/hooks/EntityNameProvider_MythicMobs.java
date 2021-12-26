package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public final class EntityNameProvider_MythicMobs implements EntityNameProvider {

    private final WildStackerPlugin plugin;

    public EntityNameProvider_MythicMobs(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    @Override
    public String getCustomName(LivingEntity livingEntity) {
        ActiveMob activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(livingEntity);

        try {
            return activeMob.getDisplayName();
        } catch (Throwable ignored) {
        }

        return plugin.getNMSAdapter().getCustomName(livingEntity);
    }

}
