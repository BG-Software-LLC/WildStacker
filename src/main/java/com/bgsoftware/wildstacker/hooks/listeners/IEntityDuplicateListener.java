package com.bgsoftware.wildstacker.hooks.listeners;

import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;

public interface IEntityDuplicateListener {

    @Nullable
    LivingEntity duplicateEntity(LivingEntity entity);

}
