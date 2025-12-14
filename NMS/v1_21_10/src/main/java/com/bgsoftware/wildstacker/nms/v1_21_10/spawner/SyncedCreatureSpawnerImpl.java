package com.bgsoftware.wildstacker.nms.v1_21_10.spawner;

import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.block.CraftCreatureSpawner;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SyncedCreatureSpawnerImpl extends CraftBlockEntityState<SpawnerBlockEntity> implements SyncedCreatureSpawner {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ServerLevel serverLevel;
    private final BlockPos blockPos;
    private final Location blockLocation;

    public SyncedCreatureSpawnerImpl(World bukkitWorld, SpawnerBlockEntity spawnerBlockEntity) {
        super(bukkitWorld, spawnerBlockEntity);
        this.serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        this.blockPos = spawnerBlockEntity.getBlockPos();
        this.blockLocation = new Location(bukkitWorld, this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ());
    }

    public SyncedCreatureSpawnerImpl(CraftBlockEntityState<SpawnerBlockEntity> entityState, Location location) {
        super(entityState, location);
        World bukkitWorld = location.getWorld();
        this.serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        this.blockPos = entityState.getPosition();
        this.blockLocation = new Location(bukkitWorld, this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ());
    }

    @Override
    public EntityType getSpawnedType() {
        SpawnData spawnData = getSpawner().nextSpawnData;
        if (spawnData == null)
            return null;

        try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(() -> "spawner@" + this.getLocation(), LOGGER)) {
            ValueInput valueInput = TagValueInput.create(scopedCollector, this.serverLevel.registryAccess(), spawnData.entityToSpawn());
            Optional<net.minecraft.world.entity.EntityType<?>> type = net.minecraft.world.entity.EntityType.by(valueInput);
            return type.map(CraftEntityType::minecraftToBukkit).orElse(null);
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
        CraftCreatureSpawner.setSpawnedEntity(getSpawner(), snapshot, null, null);
    }

    public void setSpawnedEntity(SpawnerEntry spawnerEntry) {
        CraftCreatureSpawner.setSpawnedEntity(getSpawner(), spawnerEntry.getSnapshot(), spawnerEntry.getSpawnRule(), spawnerEntry.getEquipment());
    }

    @Override
    public void addPotentialSpawn(@NotNull SpawnerEntry spawnerEntry) {
        this.addPotentialSpawn(spawnerEntry.getSnapshot(), spawnerEntry.getSpawnWeight(), spawnerEntry.getSpawnRule());
    }

    @Override
    public void addPotentialSpawn(@NotNull EntitySnapshot snapshot, int weight, @Nullable SpawnRule spawnRule) {
        BaseSpawner baseSpawner = getSpawner();

        CompoundTag compoundTag = ((CraftEntitySnapshot) snapshot).getData();

        WeightedList.Builder<SpawnData> builder = WeightedList.builder();
        baseSpawner.spawnPotentials.unwrap().forEach(entry -> builder.add(entry.value(), entry.weight()));
        builder.add(new SpawnData(compoundTag, Optional.ofNullable(this.toMinecraftRule(spawnRule)), Optional.empty()), weight);
        baseSpawner.spawnPotentials = builder.build();
    }

    @Override
    public void setPotentialSpawns(@NotNull Collection<SpawnerEntry> entries) {
        WeightedList.Builder<SpawnData> builder = WeightedList.builder();
        for (SpawnerEntry spawnerEntry : entries) {
            CompoundTag compoundTag = ((CraftEntitySnapshot) spawnerEntry.getSnapshot()).getData();
            builder.add(new SpawnData(compoundTag,
                            Optional.ofNullable(this.toMinecraftRule(spawnerEntry.getSpawnRule())),
                            Optional.empty()),
                    spawnerEntry.getSpawnWeight());
        }
        getSpawner().spawnPotentials = builder.build();
    }

    @Override
    public @NotNull List<SpawnerEntry> getPotentialSpawns() {
        List<SpawnerEntry> entries = new ArrayList<>();

        for (Weighted<SpawnData> entry : getSpawner().spawnPotentials.unwrap()) {
            CraftEntitySnapshot snapshot = CraftEntitySnapshot.create(entry.value().getEntityToSpawn());

            if (snapshot != null) {
                SpawnRule rule = entry.value().customSpawnRules().map(this::fromMinecraftRule).orElse(null);
                entries.add(new SpawnerEntry(snapshot, entry.weight(), rule));
            }
        }
        return entries;
    }

    @Override
    public void setCreatureTypeByName(String s) {
        EntityType entityType = CraftEntityType.stringToBukkit(s);
        if (entityType != null && entityType != EntityType.UNKNOWN)
            setSpawnedType(entityType);
    }

    @Override
    public String getCreatureTypeName() {
        SpawnData spawnData = getSpawner().nextSpawnData;
        if (spawnData == null) {
            return null;
        }

        try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(() -> "spawner@" + this.getLocation(), LOGGER)) {
            ValueInput valueInput = TagValueInput.create(scopedCollector, this.serverLevel.registryAccess(), spawnData.getEntityToSpawn());
            Optional<net.minecraft.world.entity.EntityType<?>> type = net.minecraft.world.entity.EntityType.by(valueInput);
            return type.map(CraftEntityType::minecraftToBukkit).map(CraftEntityType::bukkitToString).orElse(null);
        }
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
        return new SyncedCreatureSpawnerImpl(this.world, this.getBlockEntity());
    }

    public CraftBlockEntityState<SpawnerBlockEntity> copy(Location location) {
        return new SyncedCreatureSpawnerImpl(this, location);
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

        BaseSpawner baseSpawner = getSpawner();

        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);

        try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(baseSpawner::toString, LOGGER)) {
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, this.serverLevel.registryAccess());
            tagValueOutput.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(net.minecraft.world.entity.EntityType.ITEM).toString());
            tagValueOutput.store("Item", ItemStack.CODEC, itemStack);
            baseSpawner.setNextSpawnData(
                    this.isPlaced() ? this.world.getHandle() : null,
                    this.getPosition(),
                    new SpawnData(tagValueOutput.buildResult(), Optional.empty(),
                            Optional.ofNullable(baseSpawner.nextSpawnData).flatMap(SpawnData::equipment))
            );
        }
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        return blockLocation.getBlock().getState().update(force, applyPhysics);
    }

    BaseSpawner getSpawner() {
        return ((SpawnerBlockEntity) this.serverLevel.getBlockEntity(this.blockPos)).getSpawner();
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