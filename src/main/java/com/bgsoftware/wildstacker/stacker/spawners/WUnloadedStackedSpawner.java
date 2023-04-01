package com.bgsoftware.wildstacker.stacker.spawners;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.stacker.WUnloadedStackedObject;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import org.bukkit.Location;

import java.util.Map;

public final class WUnloadedStackedSpawner extends WUnloadedStackedObject implements UnloadedStackedSpawner {

    private int spawnerUpgradeId;

    public WUnloadedStackedSpawner(StackedSpawner stackedSpawner) {
        this(stackedSpawner.getLocation(), stackedSpawner.getStackAmount(), ((WStackedSpawner) stackedSpawner).getUpgradeId());
    }

    public WUnloadedStackedSpawner(Location location, int stackAmount, int spawnerUpgradeId) {
        super(location, stackAmount);
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
        Location location = getLocation();
        Map<Location, UnloadedStackedSpawner> cachedSpawners = plugin.getDataHandler().CACHED_SPAWNERS_RAW.get(new ChunkPosition(location));
        if (cachedSpawners != null)
            cachedSpawners.remove(location);

        Query.SPAWNER_DELETE.getStatementHolder()
                .setLocation(getLocation())
                .execute(true);
    }

}
