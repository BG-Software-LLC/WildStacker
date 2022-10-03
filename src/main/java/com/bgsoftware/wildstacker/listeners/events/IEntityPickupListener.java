package com.bgsoftware.wildstacker.listeners.events;

import com.bgsoftware.wildstacker.api.objects.StackedItem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nullable;

public interface IEntityPickupListener {

    boolean apply(StackedItem stackedItem, LivingEntity livingEntity, @Nullable Inventory inventory);

}
