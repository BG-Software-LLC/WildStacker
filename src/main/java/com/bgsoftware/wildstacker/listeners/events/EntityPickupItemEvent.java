package com.bgsoftware.wildstacker.listeners.events;

import com.bgsoftware.wildstacker.api.objects.StackedItem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("WeakerAccess")
public final class EntityPickupItemEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final StackedItem stackedItem;
    private final Inventory inventory;

    private boolean cancelled = false;

    public EntityPickupItemEvent(LivingEntity entity, StackedItem stackedItem){
        super(entity);
        this.stackedItem = stackedItem;
        this.inventory = entity instanceof InventoryHolder ? ((InventoryHolder) entity).getInventory() : null;
    }

    public StackedItem getItem() {
        return stackedItem;
    }

    @Nullable
    public Inventory getInventory() {
        return inventory;
    }

    @NotNull
    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    @Nullable
    public Player getPlayer(){
        return entity instanceof Player ? (Player) entity : null;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
