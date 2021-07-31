package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * SpawnerPlaceEvent is called when a new spawner is placed in the world.
 */
public class SpawnerPlaceEvent extends PlaceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ItemStack itemInHand;

    /**
     * The constructor for the event.
     *
     * @param player  The player who placed the spawner.
     * @param spawner The spawner object of the placed block.
     * @deprecated See SpawnerPlaceEvent(Player, StackedSpawner, ItemStack)
     */
    public SpawnerPlaceEvent(Player player, StackedSpawner spawner) {
        this(player, spawner, new ItemStack(Material.AIR));
    }

    /**
     * The constructor for the event.
     *
     * @param player     The player who placed the spawner.
     * @param spawner    The spawner object of the placed block.
     * @param itemInHand The item that the player held.
     */
    public SpawnerPlaceEvent(Player player, StackedSpawner spawner, ItemStack itemInHand) {
        super(player, spawner);
        this.itemInHand = itemInHand.clone();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Get the spawner object of the event.
     */
    public StackedSpawner getSpawner() {
        return (StackedSpawner) object;
    }

    /**
     * Get the item that the player held.
     */
    public ItemStack getItemInHand() {
        return itemInHand.clone();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
