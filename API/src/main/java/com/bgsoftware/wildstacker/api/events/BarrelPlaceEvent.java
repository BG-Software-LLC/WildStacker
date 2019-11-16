package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * BarrelPlaceEvent is called when a new barrel is placed in the world.
 */
public class BarrelPlaceEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ItemStack itemInHand;

    /**
     * The constructor for the event.
     * @param player The player who placed the barrel.
     * @param barrel The barrel object of the placed block.
     *
     * @deprecated See BarrelPlaceEvent(Player, StackedBarrel, ItemStack)
     */
    @Deprecated
    public BarrelPlaceEvent(Player player, StackedBarrel barrel){
        this(player, barrel, new ItemStack(Material.AIR));
    }

    /**
     * The constructor for the event.
     * @param player The player who placed the barrel.
     * @param barrel The barrel object of the placed block.
     * @param itemInHand The item that the player held.
     */
    public BarrelPlaceEvent(Player player, StackedBarrel barrel, ItemStack itemInHand){
        super(player, barrel);
        this.itemInHand = itemInHand.clone();
    }

    /**
     * Get the barrel object of the event.
     */
    public StackedBarrel getBarrel() {
        return (StackedBarrel) object;
    }

    /**
     * Get the item that the player held.
     */
    public ItemStack getItemInHand() {
        return itemInHand;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
