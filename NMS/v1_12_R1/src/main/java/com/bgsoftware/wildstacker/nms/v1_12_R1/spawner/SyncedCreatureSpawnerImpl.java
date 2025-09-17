package com.bgsoftware.wildstacker.nms.v1_12_R1.spawner;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockState;
import org.bukkit.entity.EntityType;

public class SyncedCreatureSpawnerImpl extends CraftBlockState implements SyncedCreatureSpawner {

    private final World world;
    private final BlockPosition blockPosition;

    public SyncedCreatureSpawnerImpl(Block block) {
        super(block);
        world = ((CraftWorld) block.getWorld()).getHandle();
        blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
    }

    @Override
    public EntityType getSpawnedType() {
        try {
            MinecraftKey key = getSpawner().getSpawner().getMobName();
            return key == null ? EntityType.PIG : EntityType.fromName(key.getKey());
        } catch (Exception ex) {
            return EntityType.PIG;
        }
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        if (entityType != null && entityType.getName() != null) {
            getSpawner().getSpawner().setMobName(new MinecraftKey(entityType.getName()));
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

    TileEntityMobSpawner getSpawner() {
        return (TileEntityMobSpawner) world.getTileEntity(blockPosition);
    }

}

