package com.bgsoftware.wildstacker.hooks.listeners;

import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;

public interface IEntityDuplicateListener {

    @Nullable
    <T extends LivingEntity> T duplicateEntity(T entity);

}
