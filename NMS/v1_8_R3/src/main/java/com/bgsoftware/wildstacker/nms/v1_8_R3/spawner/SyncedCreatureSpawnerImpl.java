package com.bgsoftware.wildstacker.nms.v1_8_R3.spawner;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.entity.CreatureType;
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

    TileEntityMobSpawner getSpawner() {
        return (TileEntityMobSpawner) world.getTileEntity(blockPosition);
    }

}

