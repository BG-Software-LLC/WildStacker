package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.api.handlers.UpgradesManager;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.upgrades.WSpawnerUpgrade;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class UpgradesHandler implements UpgradesManager {

    private final Map<String, SpawnerUpgrade> spawnerUpgrades = Maps.newConcurrentMap();
    private final Map<Integer, SpawnerUpgrade> spawnerUpgradesById = Maps.newConcurrentMap();
    private final SpawnerUpgrade DEFAULT_UPGRADE = createUpgrade("Default", 0);

    @Override
    public SpawnerUpgrade createUpgrade(String name, int upgradeId) {
        String nameLower = name.toLowerCase();

        SpawnerUpgrade spawnerUpgrade = spawnerUpgrades.get(nameLower);

        if(spawnerUpgrade != null) {
            if(spawnerUpgrade.getId() != upgradeId)
                throw new IllegalArgumentException("The upgrade " + name + " doesn't have the id " + upgradeId);
            return spawnerUpgrade;
        }

        if(getUpgrade(upgradeId) != null)
            throw new IllegalArgumentException("An upgrade with the id " + upgradeId + " already exists.");

        spawnerUpgrade = new WSpawnerUpgrade(name, upgradeId);

        spawnerUpgrades.put(nameLower, spawnerUpgrade);
        spawnerUpgradesById.put(upgradeId, spawnerUpgrade);

        return spawnerUpgrade;
    }

    @Override
    public SpawnerUpgrade getUpgrade(String name) {
        return spawnerUpgrades.get(name.toLowerCase());
    }

    @Override
    public SpawnerUpgrade getUpgrade(int id) {
        return spawnerUpgradesById.get(id);
    }

    @Override
    public SpawnerUpgrade getDefaultUpgrade() {
        return DEFAULT_UPGRADE;
    }

    @Override
    public List<SpawnerUpgrade> getAllUpgrades() {
        return Collections.unmodifiableList(new ArrayList<>(spawnerUpgrades.values()));
    }

    @Override
    public void removeUpgrade(SpawnerUpgrade spawnerUpgrade) {
        if(DEFAULT_UPGRADE != spawnerUpgrade)
            spawnerUpgrades.remove(spawnerUpgrade.getName().toLowerCase());
    }

    @Override
    public void removeAllUpgrades() {
        spawnerUpgrades.clear();
        spawnerUpgrades.put("default", DEFAULT_UPGRADE);
        spawnerUpgradesById.put(0, DEFAULT_UPGRADE);
    }

}
