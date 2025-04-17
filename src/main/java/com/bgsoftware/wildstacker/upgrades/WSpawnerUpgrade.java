package com.bgsoftware.wildstacker.upgrades;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumArray;
import com.bgsoftware.wildstacker.utils.items.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WSpawnerUpgrade implements SpawnerUpgrade {

    private static final ItemStack DEFAULT_ICON = new ItemBuilder(Material.GOLD_INGOT)
            .withName("%name% &7%cost%")
            .withLore(
                    "&7Spawn Delay Range: &f%min-spawn-delay%-%max-spawn-delay%",
                    "&7Spawn Count: &f%spawn-count%",
                    "&7Max Nearby Entities: &f%max-nearby-entities%",
                    "&7Required Player Range: &f%required-player-range%",
                    "&7Spawn Range: &f%spawn-range%"
            ).build();

    private final String name;
    private final int id;

    private ItemStack icon = DEFAULT_ICON.clone();
    private SpawnerUpgrade nextUpgrade = null;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    // Default upgrade values
    private FastEnumArray<EntityType> allowedEntities;

    // Regular upgrade values
    private double cost;
    private String displayName = "";

    public WSpawnerUpgrade(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isDefault() {
        return id <= 0;
    }

    @Override
    public SpawnerUpgrade getNextUpgrade() {
        return nextUpgrade;
    }

    @Override
    public void setNextUpgrade(SpawnerUpgrade nextUpgrade) {
        this.nextUpgrade = nextUpgrade;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(icon)
                .replaceAll("%name%", name)
                .replaceAll("%min-spawn-delay%", minSpawnDelay + "")
                .replaceAll("%max-spawn-delay%", maxSpawnDelay + "")
                .replaceAll("%spawn-count%", spawnCount + "")
                .replaceAll("%max-nearby-entities%", maxNearbyEntities + "")
                .replaceAll("%required-player-range%", requiredPlayerRange + "")
                .replaceAll("%spawn-range%", spawnRange + "")
                .build();
    }

    @Override
    public void setIcon(ItemStack icon) {
        this.icon = icon == null ? DEFAULT_ICON.clone() : icon.clone();
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? "" : ChatColor.translateAlternateColorCodes('&', displayName);
    }

    @Override
    public boolean isEntityAllowed(EntityType entityType) {
        return allowedEntities == null || allowedEntities.size() == 0 || allowedEntities.contains(entityType);
    }

    @Override
    public void setAllowedEntities(List<String> allowedEntities) {
        this.allowedEntities = allowedEntities == null ? null : FastEnumArray.fromList(allowedEntities, EntityType.class);
    }

    @Override
    public int getMinSpawnDelay() {
        return minSpawnDelay;
    }

    @Override
    public void setMinSpawnDelay(int minSpawnDelay) {
        this.minSpawnDelay = minSpawnDelay;
    }

    @Override
    public int getMaxSpawnDelay() {
        return maxSpawnDelay;
    }

    @Override
    public void setMaxSpawnDelay(int maxSpawnDelay) {
        this.maxSpawnDelay = maxSpawnDelay;
    }

    @Override
    public int getSpawnCount() {
        return spawnCount;
    }

    @Override
    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    @Override
    public int getMaxNearbyEntities() {
        return maxNearbyEntities;
    }

    @Override
    public void setMaxNearbyEntities(int maxNearbyEntities) {
        this.maxNearbyEntities = maxNearbyEntities;
    }

    @Override
    public int getRequiredPlayerRange() {
        return requiredPlayerRange;
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        this.requiredPlayerRange = requiredPlayerRange;
    }

    @Override
    public int getSpawnRange() {
        return spawnRange;
    }

    @Override
    public void setSpawnRange(int spawnRange) {
        this.spawnRange = spawnRange;
    }

}
