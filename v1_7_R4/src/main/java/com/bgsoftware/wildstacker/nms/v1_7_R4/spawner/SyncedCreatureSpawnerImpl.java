package com.bgsoftware.wildstacker.nms.v1_7_R4.spawner;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_7_R4.MobSpawnerAbstract;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.TileEntityMobSpawner;
import net.minecraft.server.v1_7_R4.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.block.CraftBlockState;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.EntityType;

public class SyncedCreatureSpawnerImpl extends CraftBlockState implements SyncedCreatureSpawner {

    private final World world;
    private final int locX, locY, locZ;

    public SyncedCreatureSpawnerImpl(Block block) {
        super(block);
        world = ((CraftWorld) block.getWorld()).getHandle();
        locX = block.getX();
        locY = block.getY();
        locZ = block.getZ();
    }

    @Override
    public CreatureType getCreatureType() {
        return CreatureType.fromEntityType(getSpawnedType());
    }

    @Override
    public void setCreatureType(CreatureType creatureType) {
        setSpawnedType(creatureType.toEntityType());
    }

    @Override
    public String getCreatureTypeId() {
        return getCreatureTypeName();
    }

    @Override
    public void setCreatureTypeId(String s) {
        setCreatureTypeByName(s);
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
    public void setCreatureTypeByName(String s) {
        EntityType entityType = EntityType.fromName(s);
        if (entityType != null && entityType != EntityType.UNKNOWN)
            setSpawnedType(entityType);
    }

    @Override
    public String getCreatureTypeName() {
        return getSpawner().getSpawner().getMobName();
    }

    @Override
    public EntityType getSpawnedType() {
        try {
            return EntityType.fromName(getSpawner().getSpawner().getMobName());
        } catch (Exception ex) {
            return EntityType.PIG;
        }
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        if (entityType != null && entityType.getName() != null) {
            getSpawner().getSpawner().setMobName(entityType.getName());
        } else {
            throw new IllegalArgumentException("Can't spawn EntityType " + entityType + " from mobspawners!");
        }
    }

    @Override
    public void updateSpawner(SpawnerUpgrade spawnerUpgrade) {
        MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
        if (mobSpawnerAbstract instanceof StackedMobSpawner) {
            ((StackedMobSpawner) mobSpawnerAbstract).minSpawnDelay = spawnerUpgrade.getMinSpawnDelay();
            ((StackedMobSpawner) mobSpawnerAbstract).maxSpawnDelay = spawnerUpgrade.getMaxSpawnDelay();
            ((StackedMobSpawner) mobSpawnerAbstract).spawnCount = spawnerUpgrade.getSpawnCount();
            ((StackedMobSpawner) mobSpawnerAbstract).maxNearbyEntities = spawnerUpgrade.getMaxNearbyEntities();
            ((StackedMobSpawner) mobSpawnerAbstract).requiredPlayerRange = spawnerUpgrade.getRequiredPlayerRange();
            ((StackedMobSpawner) mobSpawnerAbstract).spawnRange = spawnerUpgrade.getSpawnRange();
            ((StackedMobSpawner) mobSpawnerAbstract).updateUpgrade(spawnerUpgrade.getId());
        } else {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            mobSpawnerAbstract.b(nbtTagCompound);

            nbtTagCompound.setShort("MinSpawnDelay", (short) spawnerUpgrade.getMinSpawnDelay());
            nbtTagCompound.setShort("MaxSpawnDelay", (short) spawnerUpgrade.getMaxSpawnDelay());
            nbtTagCompound.setShort("SpawnCount", (short) spawnerUpgrade.getSpawnCount());
            nbtTagCompound.setShort("MaxNearbyEntities", (short) spawnerUpgrade.getMaxNearbyEntities());
            nbtTagCompound.setShort("RequiredPlayerRange", (short) spawnerUpgrade.getRequiredPlayerRange());
            nbtTagCompound.setShort("SpawnRange", (short) spawnerUpgrade.getSpawnRange());

            mobSpawnerAbstract.a(nbtTagCompound);
        }
    }

    @Override
    public SpawnerCachedData readData() {
        MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
        if (mobSpawnerAbstract instanceof StackedMobSpawner) {
            StackedMobSpawner stackedMobSpawner = (StackedMobSpawner) mobSpawnerAbstract;
            return new SpawnerCachedData(
                    stackedMobSpawner.minSpawnDelay,
                    stackedMobSpawner.maxSpawnDelay,
                    stackedMobSpawner.spawnCount,
                    stackedMobSpawner.maxNearbyEntities,
                    stackedMobSpawner.requiredPlayerRange,
                    stackedMobSpawner.spawnRange,
                    stackedMobSpawner.spawnDelay / 20,
                    stackedMobSpawner.failureReason
            );
        } else {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            mobSpawnerAbstract.b(nbtTagCompound);
            return new SpawnerCachedData(
                    nbtTagCompound.getShort("MinSpawnDelay"),
                    nbtTagCompound.getShort("MaxSpawnDelay"),
                    nbtTagCompound.getShort("SpawnCount"),
                    nbtTagCompound.getShort("MaxNearbyEntities"),
                    nbtTagCompound.getShort("RequiredPlayerRange"),
                    nbtTagCompound.getShort("SpawnRange"),
                    nbtTagCompound.getShort("Delay") / 20
            );
        }
    }

    TileEntityMobSpawner getSpawner() {
        return (TileEntityMobSpawner) world.getTileEntity(locX, locY, locZ);
    }

}

