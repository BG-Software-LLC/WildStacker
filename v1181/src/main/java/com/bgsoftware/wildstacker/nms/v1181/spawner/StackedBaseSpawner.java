package com.bgsoftware.wildstacker.nms.v1181.spawner;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MCUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.phys.AABB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

public class StackedBaseSpawner extends BaseSpawner {

    private static final ReflectField<BaseSpawner> BASE_SPAWNER = new ReflectField<BaseSpawner>(SpawnerBlockEntity.class,
            SpawnerBlockEntity.class, "a").removeFinal();
    private static final ReflectField<LevelCallback<Entity>> WORLD_LEVEL_CALLBACK =
            new ReflectField<>(PersistentEntitySectionManager.class, LevelCallback.class, "c");

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final WeakReference<WStackedSpawner> stackedSpawner;
    public String failureReason = "";

    private int spawnedEntities = 0;
    private int tickDelay = 0;
    private WStackedEntity demoEntity = null;

    public StackedBaseSpawner(SpawnerBlockEntity spawnerBlockEntity, StackedSpawner stackedSpawner) {
        this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

        BaseSpawner originalSpawner = spawnerBlockEntity.getSpawner();
        BASE_SPAWNER.set(spawnerBlockEntity, this);

        this.nextSpawnData = originalSpawner.nextSpawnData;
        this.minSpawnDelay = originalSpawner.minSpawnDelay;
        this.maxSpawnDelay = originalSpawner.maxSpawnDelay;
        this.spawnCount = originalSpawner.spawnCount;
        this.maxNearbyEntities = originalSpawner.maxNearbyEntities;
        this.requiredPlayerRange = originalSpawner.requiredPlayerRange;
        this.spawnRange = originalSpawner.spawnRange;

        updateDemoEntity((ServerLevel) spawnerBlockEntity.getLevel(), spawnerBlockEntity.getBlockPos());
        updateUpgrade(((WStackedSpawner) stackedSpawner).getUpgradeId());
    }

    @Override
    public void serverTick(ServerLevel serverLevel, BlockPos blockPos) {
        try {
            // Paper only
            if (spawnDelay > 0 && --tickDelay > 0)
                return;

            tickDelay = serverLevel.paperConfig.mobSpawnerTickRate;

            if (tickDelay == -1)
                return;
        } catch (Throwable ignored) {
            // If not running Paper, we want the tickDelay to be set to 1.
            tickDelay = 1;
        }

        WStackedSpawner stackedSpawner = this.stackedSpawner.get();

        if (stackedSpawner == null) {
            super.serverTick(serverLevel, blockPos);
            return;
        }

        if (!serverLevel.hasNearbyAlivePlayer(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, this.requiredPlayerRange)) {
            failureReason = "There are no nearby players.";
            return;
        }

        if (this.spawnDelay <= -tickDelay)
            resetSpawnDelay(serverLevel, blockPos);

        if (this.spawnDelay > 0) {
            this.spawnDelay -= tickDelay;
            return;
        }

        if (this.demoEntity == null) {
            super.serverTick(serverLevel, blockPos);
            failureReason = "";
            return;
        }

        CompoundTag entityToSpawn = this.nextSpawnData.getEntityToSpawn();
        Optional<EntityType<?>> entityTypesOptional = EntityType.by(entityToSpawn);

        if (!entityTypesOptional.isPresent()) {
            resetSpawnDelay(serverLevel, blockPos);
            failureReason = "";
            return;
        }

        EntityType<?> entityToSpawnType = entityTypesOptional.get();

        Entity demoEntity = ((CraftEntity) this.demoEntity.getLivingEntity()).getHandle();

        if (demoEntity.getType() != entityToSpawnType) {
            updateDemoEntity(serverLevel, blockPos);

            if (this.demoEntity == null) {
                super.serverTick(serverLevel, blockPos);
                failureReason = "";
                return;
            }

            updateUpgrade(stackedSpawner.getUpgradeId());

            demoEntity = ((CraftEntity) this.demoEntity.getLivingEntity()).getHandle();
        }

        int stackAmount = stackedSpawner.getStackAmount();

        List<? extends Entity> nearbyEntities = serverLevel.getEntitiesOfClass(
                demoEntity.getClass(), new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                        blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1).inflate(this.spawnRange));

        StackedEntity targetEntity = getTargetEntity(stackedSpawner, this.demoEntity, nearbyEntities);

        if (targetEntity == null && nearbyEntities.size() >= this.maxNearbyEntities) {
            failureReason = "There are too many nearby entities.";
            return;
        }

        boolean spawnStacked = plugin.getSettings().entitiesStackingEnabled &&
                EventsCaller.callSpawnerStackedEntitySpawnEvent(stackedSpawner.getSpawner());
        failureReason = "";

        int spawnCount = !spawnStacked || !this.demoEntity.isCached() ? Random.nextInt(1, this.spawnCount, stackAmount) :
                Random.nextInt(1, this.spawnCount, stackAmount, 1.5);

        int amountPerEntity = 1;
        int mobsToSpawn;

        short particlesAmount = 0;

        // Try stacking into the target entity first
        if (targetEntity != null && EventsCaller.callEntityStackEvent(targetEntity, this.demoEntity)) {
            int targetEntityStackLimit = targetEntity.getStackLimit();
            int currentStackAmount = targetEntity.getStackAmount();
            int increaseStackAmount = Math.min(spawnCount, targetEntityStackLimit - currentStackAmount);

            if (increaseStackAmount != spawnCount) {
                mobsToSpawn = spawnCount - increaseStackAmount;
            } else {
                mobsToSpawn = 0;
            }

            if (increaseStackAmount > 0) {
                spawnedEntities += increaseStackAmount;

                targetEntity.increaseStackAmount(increaseStackAmount, true);
                this.demoEntity.spawnStackParticle(true);

                if (plugin.getSettings().linkedEntitiesEnabled && targetEntity.getLivingEntity() != stackedSpawner.getLinkedEntity())
                    stackedSpawner.setLinkedEntity(targetEntity.getLivingEntity());

                serverLevel.levelEvent(2004, blockPos, 0);
                particlesAmount++;
            }
        } else {
            mobsToSpawn = spawnCount;
        }

        if (mobsToSpawn > 0 && this.demoEntity.isCached() && spawnStacked) {
            amountPerEntity = Math.min(mobsToSpawn, this.demoEntity.getStackLimit());
            mobsToSpawn = mobsToSpawn / amountPerEntity;
        }

        MobSpawnResult spawnResult = MobSpawnResult.SUCCESS;
        try {
            stackedSpawner.setSpawnerOverridenTick(true);
            while (spawnResult == MobSpawnResult.SUCCESS && spawnedEntities < stackAmount) {
                spawnResult = attemptMobSpawning(serverLevel, blockPos, entityToSpawn, entityToSpawnType,
                        mobsToSpawn, amountPerEntity, spawnCount, particlesAmount, stackedSpawner);
            }
        } finally {
            stackedSpawner.setSpawnerOverridenTick(false);
        }

        if (spawnResult == MobSpawnResult.SUCCESS || spawnResult == MobSpawnResult.ABORT_AND_RESET_DELAY)
            resetSpawnDelay(serverLevel, blockPos);
    }

    @Override
    public void broadcastEvent(Level level, BlockPos blockPos, int i) {
        level.blockEvent(blockPos, Blocks.SPAWNER, i, 0);
    }

    public void updateUpgrade(int upgradeId) {
        if (demoEntity != null)
            demoEntity.setUpgradeId(upgradeId);
    }

    private MobSpawnResult attemptMobSpawning(ServerLevel serverLevel, BlockPos blockPos, CompoundTag entityToSpawn,
                                              EntityType<?> entityToSpawnType, int mobsToSpawn, int amountPerEntity,
                                              int spawnCount, short particlesAmount, WStackedSpawner stackedSpawner) {
        boolean hasSpawnedEntity = false;

        for (int i = 0; i < mobsToSpawn; i++) {
            java.util.Random random = serverLevel.getRandom();

            ListTag posList = entityToSpawn.getList("Pos", 6);

            double x = posList.size() >= 1 ? posList.getDouble(0) : blockPos.getX() + (random.nextDouble() - random.nextDouble()) * this.spawnRange + 0.5D;
            double y = posList.size() >= 2 ? posList.getDouble(1) : blockPos.getY() + random.nextInt(3) - 1;
            double z = posList.size() >= 3 ? posList.getDouble(2) : blockPos.getZ() + (random.nextDouble() - random.nextDouble()) * this.spawnRange + 0.5D;

            Location location = new Location(serverLevel.getWorld(), x, y, z);

            if (!serverLevel.noCollision(entityToSpawnType.getAABB(x, y, z))) {
                if(failureReason.isEmpty())
                    failureReason = "Not enough space to spawn the entity.";
                continue;
            }

            if (this.nextSpawnData.getCustomSpawnRules().isPresent()) {
                if (!entityToSpawnType.getCategory().isFriendly() && serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
                    failureReason = "Cannot spawn entities due to PEACEFUL_WORLD restriction.";
                    continue;
                }

                SpawnData.CustomSpawnRules customSpawnRules = this.nextSpawnData.getCustomSpawnRules().get();
                if (!customSpawnRules.blockLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.BLOCK, blockPos)) ||
                        !customSpawnRules.skyLightLimit().isValueInRange(serverLevel.getBrightness(LightLayer.SKY, blockPos))) {
                    failureReason = "Cannot spawn entities due to CustomSpawnRules restriction.";
                    continue;
                }
            } else {
                SpawnCondition failedCondition = plugin.getSystemManager().getSpawnConditions(demoEntity.getLivingEntity().getType())
                        .stream().filter(spawnCondition -> !spawnCondition.test(location)).findFirst().orElse(null);

                if (failedCondition != null) {
                    failureReason = "Cannot spawn entities due to " + failedCondition.getName() + " restriction.";
                    continue;
                }
            }

            try {
                // Paper only
                String key = EntityType.getKey(entityToSpawnType).getPath();
                org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.fromName(key);

                if (type != null) {
                    PreSpawnerSpawnEvent event = new PreSpawnerSpawnEvent(MCUtil.toLocation(serverLevel, x, y, z),
                            type, MCUtil.toLocation(serverLevel, blockPos));
                    if (!event.callEvent()) {
                        if (event.shouldAbortSpawn())
                            return MobSpawnResult.ABORT_AND_RESET_DELAY;

                        continue;
                    }
                }
            } catch (Throwable ignored) {
            }

            Entity bukkitEntity = generateEntity(serverLevel, x, y, z, true);

            if (bukkitEntity == null)
                return MobSpawnResult.ABORT_AND_RESET_DELAY;

            int amountToSpawn = spawnedEntities + amountPerEntity > spawnCount ? spawnCount - spawnedEntities : amountPerEntity;

            if (handleEntitySpawn(serverLevel, blockPos, bukkitEntity, stackedSpawner, amountToSpawn, particlesAmount <= this.spawnCount)) {
                spawnedEntities += amountPerEntity;
                particlesAmount++;
                hasSpawnedEntity = true;
            }
        }

        return hasSpawnedEntity ? MobSpawnResult.SUCCESS : MobSpawnResult.ABORT;
    }

    private void resetSpawnDelay(ServerLevel serverLevel, BlockPos blockPos) {
        if (this.maxSpawnDelay <= this.minSpawnDelay) {
            this.spawnDelay = this.minSpawnDelay;
        } else {
            this.spawnDelay = this.minSpawnDelay + serverLevel.getRandom().nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        }

        // Set mob spawn data
        this.spawnPotentials.getRandom(serverLevel.getRandom()).ifPresent(weightedEntry ->
                this.setNextSpawnData(serverLevel, blockPos, weightedEntry.getData()));

        spawnedEntities = 0;
        failureReason = "";

        broadcastEvent(serverLevel, blockPos, 1);
    }

    private Entity generateEntity(ServerLevel serverLevel, double x, double y, double z, boolean rotation) {
        CompoundTag entityCompound = this.nextSpawnData.getEntityToSpawn();
        return EntityType.loadEntityRecursive(entityCompound, serverLevel, entity -> {
            entity.moveTo(x, y, z, rotation ? serverLevel.getRandom().nextFloat() * 360.0F : 0f, 0f);
            entity.level = serverLevel;
            entity.valid = true;
            return entity;
        });
    }

    private boolean handleEntitySpawn(ServerLevel serverLevel, BlockPos blockPos,
                                      Entity entity, WStackedSpawner stackedSpawner,
                                      int amountPerEntity, boolean spawnParticles) {
        StackedEntity stackedEntity = null;

        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();

        EntityStorage.setMetadata(bukkitEntity, EntityFlag.SPAWN_CAUSE, SpawnCause.SPAWNER);

        if (amountPerEntity > 1 || !stackedSpawner.isDefaultUpgrade()) {
            stackedEntity = WStackedEntity.of(bukkitEntity);
            ((WStackedEntity) stackedEntity).setUpgradeId(stackedSpawner.getUpgradeId());
            stackedEntity.setStackAmount(amountPerEntity, true);
        }

        if (entity instanceof Mob mob) {
            CompoundTag entityToSpawn = this.nextSpawnData.getEntityToSpawn();

            if (entityToSpawn.size() == 1 && entityToSpawn.contains("id", 8)) {
                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()),
                        MobSpawnType.SPAWNER, null, null);
            }

            if (serverLevel.spigotConfig.nerfSpawnerMobs) {
                mob.aware = false;
            }

            try {
                // Paper only
                entity.spawnedViaMobSpawner = true;
                entity.spawnReason = CreatureSpawnEvent.SpawnReason.SPAWNER;
            } catch (Throwable ignored) {
            }
        }

        if (CraftEventFactory.callSpawnerSpawnEvent(entity, blockPos).isCancelled()) {
            Entity vehicle = entity.getVehicle();
            if (vehicle != null)
                vehicle.discard();

            for (Entity passenger : entity.getIndirectPassengers())
                passenger.discard();
        } else if (addEntity(serverLevel, entity)) {
            if (spawnParticles)
                serverLevel.levelEvent(2004, blockPos, 0);

            if (entity instanceof Mob mob)
                mob.spawnAnim();

            return true;
        }

        if (stackedEntity != null)
            plugin.getSystemManager().removeStackObject(stackedEntity);

        EntityStorage.clearMetadata(bukkitEntity);

        return false;
    }

    private boolean addEntity(ServerLevel serverLevel, Entity entity) {
        entity.valid = false;

        if (serverLevel.tryAddFreshEntityWithPassengers(entity, CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            entity.getPassengers().forEach(passenger -> addEntity(serverLevel, passenger));
            return true;
        }

        return false;
    }

    @Nullable
    private StackedEntity getTargetEntity(StackedSpawner stackedSpawner, StackedEntity demoEntity,
                                          List<? extends Entity> nearbyEntities) {
        if (!plugin.getSettings().entitiesStackingEnabled)
            return null;

        LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

        if (linkedEntity != null && linkedEntity.getType() == demoEntity.getType())
            return WStackedEntity.of(linkedEntity);

        Optional<CraftEntity> closestEntity = GeneralUtils.getClosestBukkit(stackedSpawner.getLocation(),
                nearbyEntities.stream().map(Entity::getBukkitEntity).filter(entity ->
                        EntityUtils.isStackable(entity) &&
                                demoEntity.runStackCheck(WStackedEntity.of(entity)) == StackCheckResult.SUCCESS));

        return closestEntity.map(WStackedEntity::of).orElse(null);
    }

    private void updateDemoEntity(ServerLevel serverLevel, BlockPos blockPos) {
        Entity demoEntity = generateEntity(serverLevel, blockPos.getX(), blockPos.getY(), blockPos.getZ(), false);

        if (demoEntity == null)
            return;

        WORLD_LEVEL_CALLBACK.get(serverLevel.entityManager).onTrackingEnd(demoEntity);

        if (!EntityUtils.isStackable(demoEntity.getBukkitEntity())) {
            this.demoEntity = null;
            return;
        }

        this.demoEntity = (WStackedEntity) WStackedEntity.of(demoEntity.getBukkitEntity());
        this.demoEntity.setSpawnCause(SpawnCause.SPAWNER);
        this.demoEntity.setDemoEntity();
    }

    enum MobSpawnResult {

        SUCCESS,

        ABORT,

        ABORT_AND_RESET_DELAY

    }

}
