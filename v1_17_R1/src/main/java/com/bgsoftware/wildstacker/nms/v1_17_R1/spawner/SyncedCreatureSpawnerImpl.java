package com.bgsoftware.wildstacker.nms.v1_17_R1.spawner;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.UtilColor;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;

public class SyncedCreatureSpawnerImpl extends CraftBlockEntityState<TileEntityMobSpawner> implements SyncedCreatureSpawner {

    private final World world;
    private final BlockPosition blockPosition;
    private final Location blockLocation;

    public SyncedCreatureSpawnerImpl(org.bukkit.World bukkitWorld, TileEntityMobSpawner tileEntityMobSpawner) {
        super(bukkitWorld, tileEntityMobSpawner);
        this.world = ((CraftWorld) bukkitWorld).getHandle();
        blockPosition = tileEntityMobSpawner.getPosition();
        blockLocation = new Location(bukkitWorld, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    @Override
    public EntityType getSpawnedType() {
        try {
            MinecraftKey key = getMobName();
            EntityType entityType = key == null ? EntityType.PIG : EntityType.fromName(key.getKey());
            return entityType == null ? EntityType.PIG : entityType;
        } catch (Exception ex) {
            return EntityType.PIG;
        }
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        if (entityType != null && entityType.getName() != null) {
            getSpawner().getSpawner().setMobName(EntityTypes.a(entityType.getName()).orElse(EntityTypes.an));
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
        MinecraftKey key = getMobName();
        return key == null ? "PIG" : key.getKey();
    }

    @Override
    public int getDelay() {
        return getSpawner().getSpawner().d;
    }

    @Override
    public void setDelay(int i) {
        getSpawner().getSpawner().d = i;
    }

    @Override
    public int getMinSpawnDelay() {
        return getSpawner().getSpawner().i;
    }

    @Override
    public void setMinSpawnDelay(int i) {
        getSpawner().getSpawner().i = i;
    }

    @Override
    public int getMaxSpawnDelay() {
        return getSpawner().getSpawner().j;
    }

    @Override
    public void setMaxSpawnDelay(int i) {
        getSpawner().getSpawner().j = i;
    }

    @Override
    public int getSpawnCount() {
        return getSpawner().getSpawner().k;
    }

    @Override
    public void setSpawnCount(int i) {
        getSpawner().getSpawner().k = i;
    }

    @Override
    public int getMaxNearbyEntities() {
        return getSpawner().getSpawner().m;
    }

    @Override
    public void setMaxNearbyEntities(int i) {
        getSpawner().getSpawner().m = i;
    }

    @Override
    public int getRequiredPlayerRange() {
        return getSpawner().getSpawner().n;
    }

    @Override
    public void setRequiredPlayerRange(int i) {
        getSpawner().getSpawner().n = i;
    }

    @Override
    public int getSpawnRange() {
        return getSpawner().getSpawner().o;
    }

    @Override
    public void setSpawnRange(int i) {
        getSpawner().getSpawner().o = i;
    }

    public boolean isActivated() {
        this.requirePlaced();
        return getSpawner().getSpawner().c(this.world, this.getPosition());
    }

    public void resetTimer() {
        this.requirePlaced();
        getSpawner().getSpawner().d(this.world, this.getPosition());
    }

    public void setSpawnedItem(org.bukkit.inventory.ItemStack bukkitItem) {
        Preconditions.checkArgument(bukkitItem != null && !bukkitItem.getType().isAir(), "spawners cannot spawn air");
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        NBTTagCompound entity = new NBTTagCompound();
        entity.setString("id", IRegistry.Y.getKey(EntityTypes.Q).toString());
        entity.set("Item", itemStack.save(new NBTTagCompound()));
        getSpawner().getSpawner().setSpawnData(
                this.isPlaced() ? this.world : null,
                this.getPosition(),
                new MobSpawnerData(entity)
        );
    }

    @Override
    public void updateSpawner(SpawnerUpgrade spawnerUpgrade) {
        MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
        mobSpawnerAbstract.i = spawnerUpgrade.getMinSpawnDelay();
        mobSpawnerAbstract.j = spawnerUpgrade.getMaxSpawnDelay();
        mobSpawnerAbstract.k = spawnerUpgrade.getSpawnCount();
        mobSpawnerAbstract.m = spawnerUpgrade.getMaxNearbyEntities();
        mobSpawnerAbstract.n = spawnerUpgrade.getRequiredPlayerRange();
        mobSpawnerAbstract.o = spawnerUpgrade.getSpawnRange();
    }

    @Override
    public SpawnerCachedData readData() {
        MobSpawnerAbstract mobSpawnerAbstract = getSpawner().getSpawner();
        return new SpawnerCachedData(
                mobSpawnerAbstract.i,
                mobSpawnerAbstract.j,
                mobSpawnerAbstract.k,
                mobSpawnerAbstract.m,
                mobSpawnerAbstract.n,
                mobSpawnerAbstract.o,
                mobSpawnerAbstract.d / 20,
                ""
        );
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        return blockLocation.getBlock().getState().update(force, applyPhysics);
    }

    TileEntityMobSpawner getSpawner() {
        return (TileEntityMobSpawner) world.getTileEntity(blockPosition);
    }

    private MinecraftKey getMobName() {
        String id = getSpawner().getSpawner().f.getEntity().getString("id");
        return UtilColor.b(id) ? null : new MinecraftKey(id);
    }

}

