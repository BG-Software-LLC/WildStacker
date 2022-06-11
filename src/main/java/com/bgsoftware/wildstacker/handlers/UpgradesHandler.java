package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.api.handlers.UpgradesManager;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.upgrades.WSpawnerUpgrade;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UpgradesHandler implements UpgradesManager {

    private final Map<String, SpawnerUpgrade> spawnerUpgrades = Maps.newConcurrentMap();
    private final Map<Integer, SpawnerUpgrade> spawnerUpgradesById = Maps.newConcurrentMap();

    private final Set<SpawnerUpgrade> rootUpgrades = Sets.newConcurrentHashSet();
    private SpawnerUpgrade GLOBAL_UPGRADE = new WSpawnerUpgrade("Default", -1);

    @Override
    public SpawnerUpgrade createUpgrade(String name, int upgradeId) {
        String nameLower = name.toLowerCase();

        SpawnerUpgrade spawnerUpgrade = spawnerUpgrades.get(nameLower);

        if (spawnerUpgrade != null) {
            if (spawnerUpgrade.getId() != upgradeId)
                throw new IllegalArgumentException("The upgrade " + name + " doesn't have the id " + upgradeId);
            return spawnerUpgrade;
        }

        if (getUpgrade(upgradeId) != null)
            throw new IllegalArgumentException("An upgrade with the id " + upgradeId + " already exists.");

        spawnerUpgrade = new WSpawnerUpgrade(name, upgradeId);

        spawnerUpgrades.put(nameLower, spawnerUpgrade);
        spawnerUpgradesById.put(upgradeId, spawnerUpgrade);

        return spawnerUpgrade;
    }

    @Override
    public SpawnerUpgrade createDefault(List<String> allowedEntities) {
        return this.createDefault("Default", -1, allowedEntities);
    }

    @Override
    public SpawnerUpgrade createDefault(String name, int id, List<String> allowedEntities) {
        SpawnerUpgrade spawnerUpgrade = new WSpawnerUpgrade(name, id);

        if (allowedEntities.isEmpty()) {
            GLOBAL_UPGRADE = spawnerUpgrade;
        } else {
            spawnerUpgrade.setAllowedEntities(allowedEntities);
            rootUpgrades.add(spawnerUpgrade);
        }

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
    public SpawnerUpgrade getDefaultUpgrade(EntityType entityType) {
        return entityType == null ? GLOBAL_UPGRADE : rootUpgrades.stream().filter(_spawnerUpgrade ->
                _spawnerUpgrade.isEntityAllowed(entityType)).findFirst().orElse(GLOBAL_UPGRADE);
    }

    @Override
    public List<SpawnerUpgrade> getAllUpgrades() {
        return Collections.unmodifiableList(new ArrayList<>(spawnerUpgrades.values()));
    }

    @Override
    public void removeUpgrade(SpawnerUpgrade spawnerUpgrade) {
        if (!spawnerUpgrade.isDefault())
            spawnerUpgrades.remove(spawnerUpgrade.getName().toLowerCase());
    }

    @Override
    public void removeAllUpgrades() {
        spawnerUpgrades.clear();
        spawnerUpgradesById.clear();
        rootUpgrades.clear();
        GLOBAL_UPGRADE = new WSpawnerUpgrade("Default", -1);
    }

}
