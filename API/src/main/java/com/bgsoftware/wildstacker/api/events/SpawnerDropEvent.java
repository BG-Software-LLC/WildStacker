package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * SpawnerDropEvent is called when a spawner is broken and dropped.
 */
public class SpawnerDropEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedSpawner stackedSpawner;
    private final Player player;
    private ItemStack itemStack;

    /**
     * The constructor for the event.
     *
     * @param stackedSpawner The spawner that was broken.
     * @param player         The player that broke the spawner. May be null.
     * @param itemStack      The item that will be dropped.
     */
    public SpawnerDropEvent(StackedSpawner stackedSpawner, Player player, ItemStack itemStack) {
        this.stackedSpawner = stackedSpawner;
        this.player = player;
        this.itemStack = itemStack;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the spawner that was broken.
     */
    public StackedSpawner getSpawner() {
        return stackedSpawner;
    }

    /**
     * Get the player that broke the spawner.
     * May be null if was broken by an explosion.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the item that will be dropped.
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Set a new item that will be dropped.
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
