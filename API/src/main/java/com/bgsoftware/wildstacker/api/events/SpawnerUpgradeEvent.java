package com.bgsoftware.wildstacker.api.events;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * SpawnerUpgradeEvent is called when a spawner is upgraded.
 */
public class SpawnerUpgradeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final StackedSpawner stackedSpawner;
    private final SpawnerUpgrade spawnerUpgrade;
    private final Player player;

    /**
     * The constructor for the event.
     *
     * @param stackedSpawner The spawner that was broken.
     * @param spawnerUpgrade The new upgrade.
     */
    public SpawnerUpgradeEvent(StackedSpawner stackedSpawner, SpawnerUpgrade spawnerUpgrade) {
        this(stackedSpawner, spawnerUpgrade, null);
    }

    /**
     * The constructor for the event.
     *
     * @param stackedSpawner The spawner that was broken.
     * @param spawnerUpgrade The new upgrade.
     * @param player The player that upgraded the spawner.
     */
    public SpawnerUpgradeEvent(StackedSpawner stackedSpawner, SpawnerUpgrade spawnerUpgrade, @Nullable Player player) {
        this.stackedSpawner = stackedSpawner;
        this.spawnerUpgrade = spawnerUpgrade;
        this.player = player;
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

    /**
     * Get the player that upgraded the spawner.
     */
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
