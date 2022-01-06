package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.objects.StackedEntity;

import javax.annotation.Nullable;

public interface EntityNameProvider {

    @Nullable
    String getCustomName(StackedEntity stackedEntity);

}
