package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import org.bukkit.Location;

public final class WUnloadedStackedSpawner extends WUnloadedStackedObject implements UnloadedStackedSpawner {

    private int spawnerUpgradeId;

    public WUnloadedStackedSpawner(StackedSpawner stackedSpawner) {
        this(stackedSpawner.getLocation(), stackedSpawner.getStackAmount(), ((WStackedSpawner) stackedSpawner).getUpgradeId());
    }

    public WUnloadedStackedSpawner(Location location, int stackAmount, int spawnerUpgradeId) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                stackAmount, spawnerUpgradeId);
    }

    public WUnloadedStackedSpawner(String worldName, int locX, int locY, int locZ, int stackAmount, int spawnerUpgradeId) {
        super(worldName, locX, locY, locZ, stackAmount);
        this.spawnerUpgradeId = spawnerUpgradeId;
    }

    @Override
    public SpawnerUpgrade getUpgrade() {
        SpawnerUpgrade currentUpgrade = plugin.getUpgradesManager().getUpgrade(spawnerUpgradeId);
        return currentUpgrade == null ? plugin.getUpgradesManager().getDefaultUpgrade(null) : currentUpgrade;
    }

    @Override
    public void setUpgrade(SpawnerUpgrade spawnerUpgrade) {
        this.spawnerUpgradeId = spawnerUpgrade == null ? 0 : spawnerUpgrade.getId();
    }

    public int getUpgradeId() {
        return spawnerUpgradeId;
    }

    @Override
    public void remove() {
        plugin.getDataHandler().stackedSpawnerStore.removeUnloaded(this);
        plugin.getDataHandler().deleteSpawner(this);
    }

}
