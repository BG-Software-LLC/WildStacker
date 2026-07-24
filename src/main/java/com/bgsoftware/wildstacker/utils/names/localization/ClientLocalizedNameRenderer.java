package com.bgsoftware.wildstacker.utils.names.localization;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public interface ClientLocalizedNameRenderer {

    boolean isSupported();

    LocalizedNameApplyResult applyItemName(
            Entity itemEntity,
            ItemStack itemStack,
            LocalizedNameTemplate template,
            int amount,
            @Nullable LocalizedItemDescriptor descriptor
    );

    LocalizedNameApplyResult applyEntityName(
            Entity entity,
            EntityType entityType,
            LocalizedNameTemplate template,
            int amount,
            @Nullable String upgradeDisplayName
    );
}
