package com.bgsoftware.wildstacker.api.objects;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;

public interface UpgradeableStackedObject {

    /**
     * Get the upgrade of the object.
     * If it doesn't have an upgrade, default upgrade will be returned.
     */
    SpawnerUpgrade getUpgrade();

    /**
     * Set an upgrade to this object.
     * Can be null to remove the upgrades from this object.
     * @param spawnerUpgrade The upgrade to set.
     */
    void setUpgrade(SpawnerUpgrade spawnerUpgrade);

}
