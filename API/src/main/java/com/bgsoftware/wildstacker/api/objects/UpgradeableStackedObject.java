package com.bgsoftware.wildstacker.api.objects;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public interface UpgradeableStackedObject {

    /**
     * Get the upgrade of the object.
     * If it doesn't have an upgrade, default upgrade will be returned.
     */
    SpawnerUpgrade getUpgrade();

    /**
     * Set an upgrade to this object.
     * Can be null to remove the upgrades from this object.
     *
     * @param spawnerUpgrade The upgrade to set.
     */
    void setUpgrade(SpawnerUpgrade spawnerUpgrade);

    /**
     * Set an upgrade to this object.
     * Can be null to remove the upgrades from this object.
     *
     * @param spawnerUpgrade The upgrade to set.
     * @param player         The player that upgraded the object.
     */
    void setUpgrade(SpawnerUpgrade spawnerUpgrade, @Nullable Player player);

    /**
     * Check whether this object has a the default upgrade.
     */
    boolean isDefaultUpgrade();

}
