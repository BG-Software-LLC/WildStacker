package com.bgsoftware.wildstacker.nms.v1_21_10.spawner;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Debug;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.slf4j.Logger;

import java.util.Optional;

public class StackedBaseSpawner extends com.bgsoftware.wildstacker.nms.v1_21_10.spawner.AbstractStackedBaseSpawner {

    private static final Logger LOGGER = LogUtils.getLogger();

    public StackedBaseSpawner(SpawnerBlockEntity spawnerBlockEntity, StackedSpawner stackedSpawner) {
        super(spawnerBlockEntity, stackedSpawner);
    }

    @Override
    protected int getConfigTickDelay(ServerLevel serverLevel) {
        return serverLevel.paperConfig().tickRates.mobSpawner;
    }

    @Override
    protected Pair<Optional<EntityType<?>>, CompoundTag> getEntityDataFromSpawnData(ServerLevel serverLevel, SpawnData spawnData) {
        CompoundTag compoundTag = spawnData.getEntityToSpawn();
        Optional<EntityType<?>> entityType;
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this::toString, LOGGER)) {
            ValueInput valueInput = TagValueInput.create(scopedCollector, serverLevel.registryAccess(), compoundTag);
            entityType = EntityType.by(valueInput);
        }
        return new Pair<>(entityType, compoundTag);
    }

    @Override
    protected void readLocationFromCompoundTag(CompoundTag compoundTag, ServerLevel serverLevel, BlockPos blockPos, Location location) {
        RandomSource random = serverLevel.getRandom();

        Vec3 position = compoundTag.read("Pos", Vec3.CODEC).orElseGet(() -> new Vec3(
                blockPos.getX() + (random.nextDouble() - random.nextDouble()) * this.spawnRange + 0.5D,
                blockPos.getY() + random.nextInt(3) - 1,
                blockPos.getZ() + (random.nextDouble() - random.nextDouble()) * this.spawnRange + 0.5D
        ));

        location.setX(position.x);
        location.setY(position.y);
        location.setZ(position.z);
    }

    @Override
    protected boolean checkCollision(ServerLevel serverLevel, EntityType<?> entityType, double x, double y, double z) {
        return !serverLevel.noCollision(entityType.getSpawnAABB(x, y, z));
    }

    @Override
    protected boolean handleCustomSpawnRules(ServerLevel serverLevel, BlockPos blockPos, EntityType<?> entityType,
                                             WStackedSpawner stackedSpawner) {
        if (this.nextSpawnData.getCustomSpawnRules().isEmpty())
            return false;

        if (!entityType.getCategory().isFriendly() && serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedBaseSpawner", "attemptMobSpawning", "Cannot spawn as PEACEFUL");
            this.failureReason = "Cannot spawn entities due to PEACEFUL_WORLD restriction.";
            return true;
        }

        SpawnData.CustomSpawnRules customSpawnRules = this.nextSpawnData.getCustomSpawnRules().get();
        if (!customSpawnRules.blockLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.BLOCK, blockPos)) ||
                !customSpawnRules.skyLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.SKY, blockPos))) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedBaseSpawner", "attemptMobSpawning", "Cannot spawn due to a custom spawn rule");
            this.failureReason = "Cannot spawn entities due to CustomSpawnRules restriction.";
            return true;
        }

        return false;
    }

    @Override
    protected void setNextSpawnDataInternal(ServerLevel serverLevel, BlockPos blockPos) {
        this.spawnPotentials.getRandom(serverLevel.getRandom()).ifPresent(weightedEntry ->
                this.setNextSpawnData(serverLevel, blockPos, weightedEntry));
    }

    @Override
    protected Entity generateEntity(ServerLevel serverLevel, double x, double y, double z, boolean rotation) {
        CompoundTag entityCompound = getOrCreateNextSpawnData(serverLevel.getRandom()).getEntityToSpawn();
        return EntityType.loadEntityRecursive(entityCompound, serverLevel, EntitySpawnReason.SPAWNER, entity -> {
            entity.snapTo(x, y, z, rotation ? serverLevel.getRandom().nextFloat() * 360.0F : 0f, 0f);
            entity.valid = true;
            return entity;
        });
    }

    private SpawnData getOrCreateNextSpawnData(RandomSource random) {
        if (this.nextSpawnData == null)
            this.nextSpawnData = this.spawnPotentials.getRandom(random).orElseGet(SpawnData::new);

        return this.nextSpawnData;
    }

    @Override
    protected boolean shouldFinalizeSpawn(CompoundTag compoundTag) {
        return compoundTag.size() == 1 && compoundTag.getString("id").isPresent();
    }

    @Override
    protected void finalizeSpawn(Mob mob, ServerLevel serverLevel) {
        mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()),
                EntitySpawnReason.SPAWNER, null);
    }

    @Override
    protected boolean addEntityInternal(ServerLevel serverLevel, Entity entity) {
        return serverLevel.tryAddFreshEntityWithPassengers(entity, CreatureSpawnEvent.SpawnReason.SPAWNER);
    }

}
