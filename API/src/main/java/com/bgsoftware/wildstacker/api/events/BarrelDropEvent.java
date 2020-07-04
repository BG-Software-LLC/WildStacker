package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * BarrelDropEvent is called when a barrel is broken and dropped.
 */
public class BarrelDropEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedBarrel stackedBarrel;
    private final Player player;
    private ItemStack itemStack;

    /**
     * The constructor for the event.
     * @param stackedBarrel The barrel that was broken.
     * @param player The player that broke the spawner. May be null.
     * @param itemStack The item that will be dropped.
     */
    public BarrelDropEvent(StackedBarrel stackedBarrel, Player player, ItemStack itemStack){
        this.stackedBarrel = stackedBarrel;
        this.player = player;
        this.itemStack = itemStack;
    }

    /**
     * Get the barrel that was broken.
     */
    public StackedBarrel getBarrel() {
        return stackedBarrel;
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

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
