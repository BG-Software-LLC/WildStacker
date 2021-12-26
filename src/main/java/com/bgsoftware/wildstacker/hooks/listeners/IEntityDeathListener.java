package com.bgsoftware.wildstacker.hooks.listeners;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;

public interface IEntityDeathListener {

    void handleDeath(StackedEntity stackedEntity, Type type);

    enum Type {

        BEFORE_DEATH_EVENT,
        AFTER_DEATH_EVENT

    }

}
