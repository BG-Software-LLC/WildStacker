package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * SpawnerUpgradeEvent is called when a spawner is upgraded.
 */
public class SpawnerUpgradeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedSpawner stackedSpawner;
    private final SpawnerUpgrade spawnerUpgrade;

    /**
     * The constructor for the event.
     *
     * @param stackedSpawner The spawner that was broken.
     * @param spawnerUpgrade The new upgrade.
     */
    public SpawnerUpgradeEvent(StackedSpawner stackedSpawner, SpawnerUpgrade spawnerUpgrade) {
        this.stackedSpawner = stackedSpawner;
        this.spawnerUpgrade = spawnerUpgrade;
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
     * Get the new upgrade of the spawner.
     */
    public SpawnerUpgrade getSpawnerUpgrade() {
        return spawnerUpgrade;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
