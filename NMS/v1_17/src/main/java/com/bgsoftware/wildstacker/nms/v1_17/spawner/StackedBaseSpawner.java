package com.bgsoftware.wildstacker.nms.v1_17.spawner;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Optional;
import java.util.Random;

public class StackedBaseSpawner extends com.bgsoftware.wildstacker.nms.v1_17.spawner.AbstractStackedBaseSpawner {

    public StackedBaseSpawner(SpawnerBlockEntity spawnerBlockEntity, StackedSpawner stackedSpawner) {
        super(spawnerBlockEntity, stackedSpawner);
    }

    @Override
    protected int getConfigTickDelay(ServerLevel serverLevel) {
        return serverLevel.paperConfig.mobSpawnerTickRate;
    }

    @Override
    protected Pair<Optional<EntityType<?>>, CompoundTag> getEntityDataFromSpawnData(ServerLevel serverLevel, SpawnData spawnData) {
        CompoundTag compoundTag = spawnData.getTag();
        Optional<EntityType<?>> entityType = EntityType.by(compoundTag);
        return new Pair<>(entityType, compoundTag);
    }

    @Override
    protected void readLocationFromCompoundTag(CompoundTag compoundTag, ServerLevel serverLevel, BlockPos blockPos, Location location) {
        Random random = serverLevel.getRandom();

        ListTag posList = compoundTag.getList("Pos", 6);

        location.setX(posList.size() >= 1 ? posList.getDouble(0) : blockPos.getX() + (random.nextDouble() - random.nextDouble()) * this.spawnRange + 0.5D);
        location.setY(posList.size() >= 2 ? posList.getDouble(1) : blockPos.getY() + random.nextInt(3) - 1);
        location.setZ(posList.size() >= 3 ? posList.getDouble(2) : blockPos.getZ() + (random.nextDouble() - random.nextDouble()) * this.spawnRange + 0.5D);
    }

    @Override
    protected boolean checkCollision(ServerLevel serverLevel, EntityType<?> entityType, double x, double y, double z) {
        return !serverLevel.noCollision(entityType.getAABB(x, y, z));
    }

    @Override
    protected boolean handleCustomSpawnRules(ServerLevel serverLevel, BlockPos blockPos, EntityType<?> entityType,
                                             WStackedSpawner stackedSpawner) {
        return false;
    }

    @Override
    protected void setNextSpawnDataInternal(ServerLevel serverLevel, BlockPos blockPos) {
        this.spawnPotentials.getRandom(serverLevel.getRandom()).ifPresent(weightedEntry ->
                this.setNextSpawnData(serverLevel, blockPos, weightedEntry));
    }

    @Override
    protected Entity generateEntity(ServerLevel serverLevel, double x, double y, double z, boolean rotation) {
        CompoundTag entityCompound = this.nextSpawnData.getTag();
        return EntityType.loadEntityRecursive(entityCompound, serverLevel, entity -> {
            entity.moveTo(x, y, z, rotation ? serverLevel.getRandom().nextFloat() * 360.0F : 0f, 0f);
            entity.valid = true;
            return entity;
        });
    }

    @Override
    protected boolean shouldFinalizeSpawn(CompoundTag compoundTag) {
        return compoundTag.size() == 1 && compoundTag.contains("id", 8);
    }

    @Override
    protected void finalizeSpawn(Mob mob, ServerLevel serverLevel) {
        mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()),
                MobSpawnType.SPAWNER, null, null);
    }

    @Override
    protected boolean addEntityInternal(ServerLevel serverLevel, Entity entity) {
        return serverLevel.addAllEntitiesSafely(entity, CreatureSpawnEvent.SpawnReason.SPAWNER);
    }
}
