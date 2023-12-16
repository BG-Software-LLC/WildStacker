package com.bgsoftware.wildstacker.nms.v1_20_3.spawner;

import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.spawners.SpawnerCachedData;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.StringUtil;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.bukkit.Location;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntitySnapshot;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SyncedCreatureSpawnerImpl extends CraftBlockEntityState<SpawnerBlockEntity> implements SyncedCreatureSpawner {

    private final ServerLevel serverLevel;
    private final BlockPos blockPos;
    private final Location blockLocation;

    public SyncedCreatureSpawnerImpl(org.bukkit.World bukkitWorld, SpawnerBlockEntity spawnerBlockEntity) {
        super(bukkitWorld, spawnerBlockEntity);
        this.serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        this.blockPos = spawnerBlockEntity.getBlockPos();
        this.blockLocation = new Location(bukkitWorld, this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ());
    }

    @Override
    public EntityType getSpawnedType() {
        try {
            ResourceLocation mobName = getMobName();
            EntityType entityType = mobName == null ? EntityType.PIG : EntityType.fromName(mobName.getPath());
            return entityType == null ? EntityType.PIG : entityType;
        } catch (Exception ex) {
            return EntityType.PIG;
        }
    }

    @Override
    public void setSpawnedType(EntityType entityType) {
        if (entityType != null && entityType.getName() != null) {
            getSpawner().setEntityId(net.minecraft.world.entity.EntityType.byString(entityType.getName())
                            .orElse(net.minecraft.world.entity.EntityType.PIG), this.world.getHandle(),
                    this.world.getHandle().getRandom(),
                    this.getPosition());
        } else {
            throw new IllegalArgumentException("Can't spawn EntityType " + entityType + " from mobspawners!");
        }
    }

    @Override
    public EntitySnapshot getSpawnedEntity() {
        SpawnData spawnData = getSpawner().nextSpawnData;
        return spawnData == null ? null : CraftEntitySnapshot.create(spawnData.getEntityToSpawn());
    }

    @Override
    public void setSpawnedEntity(@NotNull EntitySnapshot snapshot) {
        BaseSpawner baseSpawner = getSpawner();

        CompoundTag compoundTag = ((CraftEntitySnapshot) snapshot).getData();

        baseSpawner.spawnPotentials = SimpleWeightedRandomList.empty();
        baseSpawner.nextSpawnData = new SpawnData(compoundTag, Optional.empty());
    }

    @Override
    public void addPotentialSpawn(@NotNull SpawnerEntry spawnerEntry) {
        this.addPotentialSpawn(spawnerEntry.getSnapshot(), spawnerEntry.getSpawnWeight(), spawnerEntry.getSpawnRule());
    }

    @Override
    public void addPotentialSpawn(@NotNull EntitySnapshot snapshot, int weight, @Nullable SpawnRule spawnRule) {
        BaseSpawner baseSpawner = getSpawner();

        CompoundTag compoundTag = ((CraftEntitySnapshot) snapshot).getData();

        SimpleWeightedRandomList.Builder<SpawnData> builder = SimpleWeightedRandomList.builder();
        baseSpawner.spawnPotentials.unwrap().forEach(entry -> builder.add(entry.getData(), entry.getWeight().asInt()));
        builder.add(new SpawnData(compoundTag, Optional.ofNullable(this.toMinecraftRule(spawnRule))), weight);
        baseSpawner.spawnPotentials = builder.build();
    }

    @Override
    public void setPotentialSpawns(@NotNull Collection<SpawnerEntry> entries) {
        SimpleWeightedRandomList.Builder<SpawnData> builder = SimpleWeightedRandomList.builder();
        for (SpawnerEntry spawnerEntry : entries) {
            CompoundTag compoundTag = ((CraftEntitySnapshot) spawnerEntry.getSnapshot()).getData();
            builder.add(new SpawnData(compoundTag, Optional.ofNullable(this.toMinecraftRule(spawnerEntry.getSpawnRule()))), spawnerEntry.getSpawnWeight());
        }
        getSpawner().spawnPotentials = builder.build();
    }

    @Override
    public @NotNull List<SpawnerEntry> getPotentialSpawns() {
        List<SpawnerEntry> entries = new ArrayList<>();

        for (WeightedEntry.Wrapper<SpawnData> entry : getSpawner().spawnPotentials.unwrap()) {
            CraftEntitySnapshot snapshot = CraftEntitySnapshot.create(entry.getData().getEntityToSpawn());

            if (snapshot != null) {
                SpawnRule rule = entry.getData().customSpawnRules().map(this::fromMinecraftRule).orElse(null);
                entries.add(new SpawnerEntry(snapshot, entry.getWeight().asInt(), rule));
            }
        }
        return entries;
    }

    @Override
    public void setCreatureTypeByName(String s) {
        EntityType entityType = EntityType.fromName(s);
        if (entityType != null && entityType != EntityType.UNKNOWN)
            setSpawnedType(entityType);
    }

    @Override
    public String getCreatureTypeName() {
        ResourceLocation mobName = getMobName();
        return mobName == null ? "PIG" : mobName.getPath();
    }

    @Override
    public int getDelay() {
        return getSpawner().spawnDelay;
    }


    @Override
    public void setDelay(int spawnDelay) {
        getSpawner().spawnDelay = spawnDelay;
    }

    @Override
    public int getMinSpawnDelay() {
        return getSpawner().minSpawnDelay;
    }

    @Override
    public void setMinSpawnDelay(int minSpawnDelay) {
        getSpawner().minSpawnDelay = minSpawnDelay;
    }

    @Override
    public int getMaxSpawnDelay() {
        return getSpawner().maxSpawnDelay;
    }

    @Override
    public void setMaxSpawnDelay(int maxSpawnDelay) {
        getSpawner().maxSpawnDelay = maxSpawnDelay;
    }

    @Override
    public int getSpawnCount() {
        return getSpawner().spawnCount;
    }

    @Override
    public void setSpawnCount(int spawnCount) {
        getSpawner().spawnCount = spawnCount;
    }

    @Override
    public int getMaxNearbyEntities() {
        return getSpawner().maxNearbyEntities;
    }

    @Override
    public void setMaxNearbyEntities(int maxNearbyEntities) {
        getSpawner().maxNearbyEntities = maxNearbyEntities;
    }

    @Override
    public int getRequiredPlayerRange() {
        return getSpawner().requiredPlayerRange;
    }

    @Override
    public void setRequiredPlayerRange(int requiredPlayerRange) {
        getSpawner().requiredPlayerRange = requiredPlayerRange;
    }

    @Override
    public int getSpawnRange() {
        return getSpawner().spawnRange;
    }

    @Override
    public void setSpawnRange(int spawnRange) {
        getSpawner().spawnRange = spawnRange;
    }

    public SyncedCreatureSpawnerImpl copy() {
        return new SyncedCreatureSpawnerImpl(this.world, this.getTileEntity());
    }

    public boolean isActivated() {
        this.requirePlaced();
        return getSpawner().isNearPlayer(this.world.getHandle(), this.getPosition());
    }

    public void resetTimer() {
        this.requirePlaced();
        getSpawner().delay(this.world.getHandle(), this.getPosition());
    }

    public void setSpawnedItem(org.bukkit.inventory.ItemStack bukkitItem) {
        Preconditions.checkArgument(bukkitItem != null && !bukkitItem.getType().isAir(), "spawners cannot spawn air");
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        CompoundTag entity = new CompoundTag();
        entity.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(net.minecraft.world.entity.EntityType.ITEM).toString());
        entity.put("Item", itemStack.save(new CompoundTag()));
        getSpawner().setNextSpawnData(
                this.isPlaced() ? this.world.getHandle() : null,
                this.getPosition(),
                new SpawnData(entity, Optional.empty())
        );
    }


    @Override
    public void updateSpawner(SpawnerUpgrade spawnerUpgrade) {
        BaseSpawner baseSpawner = getSpawner();
        baseSpawner.minSpawnDelay = spawnerUpgrade.getMinSpawnDelay();
        baseSpawner.maxSpawnDelay = spawnerUpgrade.getMaxSpawnDelay();
        baseSpawner.spawnCount = spawnerUpgrade.getSpawnCount();
        baseSpawner.maxNearbyEntities = spawnerUpgrade.getMaxNearbyEntities();
        baseSpawner.requiredPlayerRange = spawnerUpgrade.getRequiredPlayerRange();
        baseSpawner.spawnRange = spawnerUpgrade.getSpawnRange();
    }

    @Override
    public SpawnerCachedData readData() {
        BaseSpawner baseSpawner = getSpawner();
        return new SpawnerCachedData(
                baseSpawner.minSpawnDelay,
                baseSpawner.maxSpawnDelay,
                baseSpawner.spawnCount,
                baseSpawner.maxNearbyEntities,
                baseSpawner.requiredPlayerRange,
                baseSpawner.spawnRange,
                baseSpawner.spawnDelay / 20,
                baseSpawner instanceof StackedBaseSpawner stackedBaseSpawner ? stackedBaseSpawner.failureReason : ""
        );
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        return blockLocation.getBlock().getState().update(force, applyPhysics);
    }

    BaseSpawner getSpawner() {
        return ((SpawnerBlockEntity) this.serverLevel.getBlockEntity(this.blockPos)).getSpawner();
    }

    private ResourceLocation getMobName() {
        String id = getSpawner().nextSpawnData.getEntityToSpawn().getString("id");
        return StringUtil.isNullOrEmpty(id) ? null : new ResourceLocation(id);
    }

    private SpawnData.CustomSpawnRules toMinecraftRule(SpawnRule rule) {
        if (rule == null) {
            return null;
        }
        return new SpawnData.CustomSpawnRules(
                new InclusiveRange<>(rule.getMinBlockLight(), rule.getMaxBlockLight()),
                new InclusiveRange<>(rule.getMinSkyLight(), rule.getMaxSkyLight()));
    }

    private SpawnRule fromMinecraftRule(SpawnData.CustomSpawnRules rule) {
        InclusiveRange<Integer> blockLight = rule.blockLightLimit();
        InclusiveRange<Integer> skyLight = rule.skyLightLimit();

        return new SpawnRule(blockLight.maxInclusive(), blockLight.maxInclusive(), skyLight.minInclusive(), skyLight.maxInclusive());
    }

}
