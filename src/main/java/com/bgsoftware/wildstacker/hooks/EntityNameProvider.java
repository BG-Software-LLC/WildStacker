package com.bgsoftware.wildstacker.hooks;

import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;

public interface EntityNameProvider {

    @Nullable
    String getCustomName(LivingEntity livingEntity);

}
