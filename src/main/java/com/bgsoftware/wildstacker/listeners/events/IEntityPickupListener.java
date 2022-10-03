package com.bgsoftware.wildstacker.listeners.events;

import com.bgsoftware.wildstacker.api.objects.StackedItem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;

public interface IEntityPickupListener {

    boolean apply(Cancellable event, StackedItem stackedItem, LivingEntity livingEntity, int remaining);

}
