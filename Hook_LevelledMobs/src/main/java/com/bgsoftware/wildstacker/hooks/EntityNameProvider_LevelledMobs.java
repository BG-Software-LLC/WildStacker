package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import org.bukkit.entity.LivingEntity;

@SuppressWarnings("unused")
public final class EntityNameProvider_LevelledMobs implements EntityNameProvider {

    private final WildStackerPlugin plugin;

    public EntityNameProvider_LevelledMobs(WildStackerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCustomName(LivingEntity livingEntity) {
        return plugin.getNMSAdapter().getCustomName(livingEntity);
    }

}
