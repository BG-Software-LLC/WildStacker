package com.bgsoftware.wildstacker.nms.v1_16_R3.spawner;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockEntityState;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class SyncedCreatureSpawnerImpl extends CraftBlockEntityState<TileEntityMobSpawner> implements SyncedCreatureSpawner {

    private final World world;
    private final BlockPosition blockPosition;
    private final Location blockLocation;

    public SyncedCreatureSpawnerImpl(Block block) {
        super(block, TileEntityMobSpawner.class);
        world = ((CraftWorld) block.getWorld()).getHandle();
        blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        blockLocation = block.getLocation();
    }

    @Override
    public EntityType getSpawnedType() {
        try {
            MinecraftKey key = getSpawner().getSpawner().getMobName();
            EntityType entityType = key == null ? EntityType.PIG : EntityType.fromName(key.getKey());
            return entityType == null ? EntityType.PIG : entityType;
        } catch (Exception ex) {
            return EntityType.PIG;
        }
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        if (entityType != null && entityType.getName() != null) {
            getSpawner().getSpawner().setMobName(EntityTypes.a(entityType.getName()).orElse(EntityTypes.PIG));
        } else {
            throw new IllegalArgumentException("Can't spawn EntityType " + entityType + " from mobspawners!");
        }
    }

    @Override
    public void setCreatureTypeByName(String s) {
        EntityType entityType = EntityType.fromName(s);
        if (entityType != null && entityType != EntityType.UNKNOWN)
            setSpawnedType(entityType);
    }

    @Override
    public String getCreatureTypeName() {
        MinecraftKey key = getSpawner().getSpawner().getMobName();
        return key == null ? "PIG" : key.getKey();
    }

    @Override
    public int getDelay() {
        return getSpawner().getSpawner().spawnDelay;
    }

    @Override
    public void setDelay(int i) {
        getSpawner().getSpawner().spawnDelay = i;
    }

    @Override
    public int getMinSpawnDelay() {
        return getSpawner().getSpawner().minSpawnDelay;
    }

    @Override
    public void setMinSpawnDelay(int i) {
        getSpawner().getSpawner().minSpawnDelay = i;
    }

    @Override
    public int getMaxSpawnDelay() {
        return getSpawner().getSpawner().maxSpawnDelay;
    }

    @Override
    public void setMaxSpawnDelay(int i) {
        getSpawner().getSpawner().maxSpawnDelay = i;
    }

    @Override
    public int getSpawnCount() {
        return getSpawner().getSpawner().spawnCount;
    }

    @Override
    public void setSpawnCount(int i) {
        getSpawner().getSpawner().spawnCount = i;
    }

    @Override
    public int getMaxNearbyEntities() {
        return getSpawner().getSpawner().maxNearbyEntities;
    }

    @Override
    public void setMaxNearbyEntities(int i) {
        getSpawner().getSpawner().maxNearbyEntities = i;
    }

    @Override
    public int getRequiredPlayerRange() {
        return getSpawner().getSpawner().requiredPlayerRange;
    }

    @Override
    public void setRequiredPlayerRange(int i) {
        getSpawner().getSpawner().requiredPlayerRange = i;
    }

    @Override
    public int getSpawnRange() {
        return getSpawner().getSpawner().spawnRange;
    }

    @Override
    public void setSpawnRange(int i) {
        getSpawner().getSpawner().spawnRange = i;
    }

    public boolean isActivated() {
        try {
            return getSpawner().getSpawner().isActivated();
        } catch (Throwable ex) {
            return false;
        }
    }

    public void resetTimer() {
        try {
            getSpawner().getSpawner().resetTimer();
        } catch (Throwable ignored) {
        }
    }

    public void setSpawnedItem(org.bukkit.inventory.@NotNull ItemStack itemStack) {

    }

    @Override
    public void updateSpawner(SpawnerUpgrade spawnerUpgrade) {
        MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
        mobSpawnerAbstract.minSpawnDelay = spawnerUpgrade.getMinSpawnDelay();
        mobSpawnerAbstract.maxSpawnDelay = spawnerUpgrade.getMaxSpawnDelay();
        mobSpawnerAbstract.spawnCount = spawnerUpgrade.getSpawnCount();
        mobSpawnerAbstract.maxNearbyEntities = spawnerUpgrade.getMaxNearbyEntities();
        mobSpawnerAbstract.requiredPlayerRange = spawnerUpgrade.getRequiredPlayerRange();
        mobSpawnerAbstract.spawnRange = spawnerUpgrade.getSpawnRange();
        if (mobSpawnerAbstract instanceof StackedMobSpawner)
            ((StackedMobSpawner) mobSpawnerAbstract).updateUpgrade(spawnerUpgrade.getId());
    }

    @Override
    public SpawnerCachedData readData() {
        MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
        return new SpawnerCachedData(
                mobSpawnerAbstract.minSpawnDelay,
                mobSpawnerAbstract.maxSpawnDelay,
                mobSpawnerAbstract.spawnCount,
                mobSpawnerAbstract.maxNearbyEntities,
                mobSpawnerAbstract.requiredPlayerRange,
                mobSpawnerAbstract.spawnRange,
                mobSpawnerAbstract.spawnDelay / 20,
                mobSpawnerAbstract instanceof StackedMobSpawner ?
                        ((StackedMobSpawner) mobSpawnerAbstract).failureReason : ""
        );
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        return blockLocation.getBlock().getState().update(force, applyPhysics);
    }

    TileEntityMobSpawner getSpawner() {
        return (TileEntityMobSpawner) world.getTileEntity(blockPosition);
    }

}

