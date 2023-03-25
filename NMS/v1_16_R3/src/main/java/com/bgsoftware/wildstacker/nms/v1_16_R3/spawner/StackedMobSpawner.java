package com.bgsoftware.wildstacker.nms.v1_16_R3.spawner;

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
import com.bgsoftware.wildstacker.utils.Debug;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_16_R3.WeightedRandom;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class StackedMobSpawner extends MobSpawnerAbstract {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(
            TileEntityMobSpawner.class, MobSpawnerAbstract.class, "a").removeFinal();

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final WorldServer world;
    private final BlockPosition position;
    private final WeakReference<WStackedSpawner> stackedSpawner;
    public String failureReason = "";

    private int spawnedEntities = 0;
    private WStackedEntity demoEntity = null;

    public StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackedSpawner stackedSpawner) {
        boolean isDebug = ((WStackedSpawner) stackedSpawner).isDebug();

        if (isDebug)
            Debug.debug("StackedMobSpawner", "init", "Creating new spawner");

        this.world = (WorldServer) tileEntityMobSpawner.getWorld();
        this.position = tileEntityMobSpawner.getPosition();
        this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

        MobSpawnerAbstract originalSpawner = tileEntityMobSpawner.getSpawner();

        if (isDebug)
            Debug.debug("StackedMobSpawner", "init", "originalSpawner=" + originalSpawner);

        MOB_SPAWNER_ABSTRACT.set(tileEntityMobSpawner, this);

        if (isDebug)
            Debug.debug("StackedMobSpawner", "init", "After set: " + tileEntityMobSpawner.getSpawner());

        this.spawnData = originalSpawner.spawnData;
        this.minSpawnDelay = originalSpawner.minSpawnDelay;
        this.maxSpawnDelay = originalSpawner.maxSpawnDelay;
        this.spawnCount = originalSpawner.spawnCount;
        this.maxNearbyEntities = originalSpawner.maxNearbyEntities;
        this.requiredPlayerRange = originalSpawner.requiredPlayerRange;
        this.spawnRange = originalSpawner.spawnRange;

        updateDemoEntity();
        updateUpgrade(((WStackedSpawner) stackedSpawner).getUpgradeId());
    }

    @Override
    public void c() {
        WStackedSpawner stackedSpawner = this.stackedSpawner.get();

        if (stackedSpawner == null) {
            super.c();
            return;
        }

        if (!hasNearbyPlayers()) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "No nearby players in range (" + this.requiredPlayerRange + ")");
            failureReason = "There are no nearby players.";
            return;
        }

        if (this.spawnDelay <= -1)
            resetSpawnDelay();

        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }

        if (this.demoEntity == null) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "Demo entity is null, trying to update it.");
            updateDemoEntity();
            if (this.demoEntity == null) {
                if (stackedSpawner.isDebug())
                    Debug.debug("StackedMobSpawner", "c", "Demo entity is null again, aborting.");
                super.c();
                failureReason = "";
                return;
            }
        }

        Optional<EntityTypes<?>> entityTypesOptional = EntityTypes.a(this.spawnData.getEntity());

        if (!entityTypesOptional.isPresent()) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "No valid entity to spawn");
            resetSpawnDelay();
            failureReason = "";
            return;
        }

        EntityTypes<?> entityTypes = entityTypesOptional.get();

        Entity demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();

        if (demoNMSEntity.getEntityType() != entityTypes) {
            updateDemoEntity();

            if (demoEntity == null) {
                if (stackedSpawner.isDebug())
                    Debug.debug("StackedMobSpawner", "c", "Demo entity is null after trying to update it");
                super.c();
                failureReason = "";
                return;
            }

            updateUpgrade(stackedSpawner.getUpgradeId());

            demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
        }

        int stackAmount = stackedSpawner.getStackAmount();

        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "c", "stackAmount=" + stackAmount);

        List<? extends Entity> nearbyEntities = world.a(demoNMSEntity.getClass(), new AxisAlignedBB(
                position.getX(), position.getY(), position.getZ(),
                position.getX() + 1, position.getY() + 1, position.getZ() + 1
        ).g(this.spawnRange));

        AtomicInteger nearbyAndStackableCount = new AtomicInteger(0);
        List<StackedEntity> nearbyAndStackableEntities = new LinkedList<>();

        nearbyEntities.forEach(entity -> {
            CraftEntity craftEntity = entity.getBukkitEntity();
            if (EntityUtils.isStackable(craftEntity)) {
                StackedEntity stackedEntity = WStackedEntity.of(craftEntity);
                if (this.demoEntity.runStackCheck(stackedEntity) == StackCheckResult.SUCCESS) {
                    nearbyAndStackableCount.set(nearbyAndStackableCount.get() + stackedEntity.getStackAmount());
                    nearbyAndStackableEntities.add(stackedEntity);
                }
            }
        });

        StackedEntity targetEntity = getTargetEntity(stackedSpawner, this.demoEntity, nearbyAndStackableEntities, nearbyAndStackableCount);
        int targetEntityCount = targetEntity == null ? 0 : targetEntity.getStackAmount();

        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "c", "targetEntity=" + targetEntity);

        if (targetEntity == null && nearbyEntities.size() >= this.maxNearbyEntities) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "There are too many nearby entities (" + nearbyEntities.size() + ">" + this.maxNearbyEntities + ")");
            failureReason = "There are too many nearby entities.";
            return;
        }

        int minimumEntityRequirement = GeneralUtils.get(plugin.getSettings().minimumRequiredEntities, this.demoEntity, 1);

        int stackedEntityCount = Random.nextInt(1, this.spawnCount, stackAmount, 1.5);

        boolean canStackToTarget = nearbyAndStackableCount.get() + targetEntityCount + stackedEntityCount >= minimumEntityRequirement;

        boolean spawnStacked = plugin.getSettings().entitiesStackingEnabled && canStackToTarget &&
                EventsCaller.callSpawnerStackedEntitySpawnEvent(stackedSpawner.getSpawner());
        failureReason = "";

        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "c", "spawnStacked=" + spawnStacked);

        int spawnCount = !spawnStacked || !demoEntity.isCached() ? Random.nextInt(1, this.spawnCount, stackAmount) :
                stackedEntityCount;

        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "c", "spawnCount=" + spawnCount);

        int amountPerEntity = 1;
        int mobsToSpawn;

        short particlesAmount = 0;

        // Try stacking into the target entity first
        if (targetEntity != null && canStackToTarget && EventsCaller.callEntityStackEvent(targetEntity, demoEntity)) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "Stacking into the target entity");

            int targetEntityStackLimit = targetEntity.getStackLimit();
            int currentStackAmount = targetEntity.getStackAmount();
            int increaseStackAmount = Math.min(spawnCount, targetEntityStackLimit - currentStackAmount);

            if (increaseStackAmount != spawnCount) {
                mobsToSpawn = spawnCount - increaseStackAmount;
            } else {
                mobsToSpawn = 0;
            }

            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "increaseStackAmount=" + increaseStackAmount);

            if (increaseStackAmount > 0) {
                spawnedEntities += increaseStackAmount;

                if (minimumEntityRequirement > 1) {
                    // We want to stack all nearby entities into target as well.
                    increaseStackAmount += nearbyAndStackableCount.get();
                    nearbyAndStackableEntities.forEach(nearbyEntity -> {
                        if (nearbyEntity != targetEntity) {
                            nearbyEntity.remove();
                            nearbyEntity.spawnStackParticle(true);
                        }
                    });
                }

                targetEntity.increaseStackAmount(increaseStackAmount, true);
                demoEntity.spawnStackParticle(true);

                if (plugin.getSettings().linkedEntitiesEnabled && targetEntity.getLivingEntity() != stackedSpawner.getLinkedEntity())
                    stackedSpawner.setLinkedEntity(targetEntity.getLivingEntity());

                world.triggerEffect(2004, position, 0);
                particlesAmount++;
            }
        } else {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "Stacking naturally");
            mobsToSpawn = spawnCount;
        }

        if (mobsToSpawn > 0 && demoEntity.isCached() && spawnStacked) {
            amountPerEntity = Math.min(mobsToSpawn, demoEntity.getStackLimit());
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "amountPerEntity=" + amountPerEntity);
            mobsToSpawn = mobsToSpawn / amountPerEntity;
        }

        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "c", "mobsToSpawn=" + mobsToSpawn);

        while (spawnedEntities < stackAmount) {
            if (!attemptMobSpawning(entityTypes, mobsToSpawn, amountPerEntity, spawnCount, particlesAmount, stackedSpawner))
                return;
        }

        resetSpawnDelay();
    }

    @Override
    public void a(int i) {
        world.playBlockAction(position, Blocks.SPAWNER, i, 0);
    }

    @Override
    public World a() {
        return world;
    }

    @Override
    public BlockPosition b() {
        return position;
    }

    public void updateUpgrade(int upgradeId) {
        if (demoEntity != null)
            demoEntity.setUpgradeId(upgradeId);
    }

    private boolean attemptMobSpawning(EntityTypes<?> entityTypes, int mobsToSpawn, int amountPerEntity,
                                       int spawnCount, short particlesAmount, WStackedSpawner stackedSpawner) {
        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "attemptMobSpawning", "Attempting to spawn mob");

        boolean hasSpawnedEntity = false;

        for (int i = 0; i < mobsToSpawn; i++) {
            double x = position.getX() + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;
            double y = position.getY() + world.random.nextInt(3) - 1;
            double z = position.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;

            Location location = new Location(world.getWorld(), x, y, z);

            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "attemptMobSpawning", "location=" + location);

            boolean hasSpace = world.b(entityTypes.a(x, y, z));

            if (!hasSpace) {
                if (stackedSpawner.isDebug())
                    Debug.debug("StackedMobSpawner", "attemptMobSpawning", "Not enough space to spawn the entity.");
                if (failureReason.isEmpty())
                    failureReason = "Not enough space to spawn the entity.";
                continue;
            }

            SpawnCondition failedCondition = plugin.getSystemManager().getSpawnConditions(demoEntity.getLivingEntity().getType())
                    .stream().filter(spawnCondition -> !spawnCondition.test(location)).findFirst().orElse(null);

            if (failedCondition != null) {
                if (stackedSpawner.isDebug())
                    Debug.debug("StackedMobSpawner", "attemptMobSpawning", "Cannot spawn due to " + failedCondition.getName());
                failureReason = "Cannot spawn entities due to " + failedCondition.getName() + " restriction.";
                continue;
            }

            org.bukkit.entity.Entity bukkitEntity = generateEntity(x, y, z, true);

            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "attemptMobSpawning", "bukkitEntity=" + bukkitEntity);

            if (bukkitEntity == null) {
                resetSpawnDelay();
                return false;
            }

            int amountToSpawn = spawnedEntities + amountPerEntity > spawnCount ? spawnCount - spawnedEntities : amountPerEntity;

            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "attemptMobSpawning", "amountToSpawn=" + amountToSpawn);

            if (handleEntitySpawn(bukkitEntity, stackedSpawner, amountToSpawn, particlesAmount <= this.spawnCount)) {
                spawnedEntities += amountPerEntity;
                particlesAmount++;
                hasSpawnedEntity = true;
            }
        }

        return hasSpawnedEntity;
    }

    private boolean hasNearbyPlayers() {
        return world.isPlayerNearby(position.getX() + 0.5D, position.getY() + 0.5D,
                position.getZ() + 0.5D, requiredPlayerRange);
    }

    private void resetSpawnDelay() {
        if (maxSpawnDelay <= minSpawnDelay) {
            spawnDelay = minSpawnDelay;
        } else {
            spawnDelay = minSpawnDelay + world.random.nextInt(maxSpawnDelay - minSpawnDelay);
        }

        if (!this.mobs.isEmpty()) {
            setSpawnData(WeightedRandom.a(this.a().random, this.mobs));
        }

        spawnedEntities = 0;
        failureReason = "";

        a(1);
    }

    private org.bukkit.entity.Entity generateEntity(double x, double y, double z, boolean rotation) {
        NBTTagCompound entityCompound = this.spawnData.getEntity();
        Entity entity = EntityTypes.a(entityCompound, world, _entity -> {
            _entity.setPositionRotation(x, y, z, 0f, 0f);

            if (rotation)
                _entity.yaw = world.random.nextFloat() * 360.0F;

            _entity.world = world;
            _entity.valid = true;
            _entity.dead = false;

            return _entity;
        });
        return entity == null ? null : entity.getBukkitEntity();
    }

    private boolean handleEntitySpawn(org.bukkit.entity.Entity bukkitEntity, WStackedSpawner stackedSpawner, int amountPerEntity, boolean spawnParticles) {
        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "handleEntitySpawn", "Trying to spawn entity" +
                    " (amountPerEntity=" + amountPerEntity + " spawnParticles=" + spawnParticles + ")");

        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        StackedEntity stackedEntity = null;

        EntityStorage.setMetadata(bukkitEntity, EntityFlag.SPAWN_CAUSE, SpawnCause.SPAWNER);

        if (amountPerEntity > 1 || stackedSpawner.getUpgradeId() != 0) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "handleEntitySpawn", "Setting upgrade id for the entity to " + stackedSpawner.getUpgradeId());
            stackedEntity = WStackedEntity.of(bukkitEntity);
            ((WStackedEntity) stackedEntity).setUpgradeId(stackedSpawner.getUpgradeId());
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "handleEntitySpawn", "Setting stack amount for the entity to " + amountPerEntity);
            stackedEntity.setStackAmount(amountPerEntity, true);
        }

        if (entity instanceof EntityInsentient) {
            EntityInsentient entityInsentient = (EntityInsentient) entity;

            if (this.spawnData.getEntity().e() == 1 && this.spawnData.getEntity().hasKeyOfType("id", 8)) {
                entityInsentient.prepare(world, world.getDamageScaler(entity.getChunkCoordinates()), EnumMobSpawn.SPAWNER, null, null);
            }

            if (entityInsentient.world.spigotConfig.nerfSpawnerMobs) {
                entityInsentient.aware = false;
            }
        }

        if (CraftEventFactory.callSpawnerSpawnEvent(entity, position).isCancelled()) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "handleEntitySpawn", "SpawnerStackEvent was cancelled");
            Entity vehicle = entity.getVehicle();

            if (vehicle != null) {
                vehicle.dead = true;
            }

            for (Entity passenger : entity.getAllPassengers())
                passenger.dead = true;
        } else if (addEntity(entity)) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "handleEntitySpawn", "Successfully added entity to the world");

            if (spawnParticles)
                world.triggerEffect(2004, position, 0);

            if (entity instanceof EntityInsentient) {
                ((EntityInsentient) entity).doSpawnEffect();
            }

            return true;
        }

        if (stackedEntity != null)
            plugin.getSystemManager().removeStackObject(stackedEntity);

        EntityStorage.clearMetadata(bukkitEntity);

        return false;
    }

    private boolean addEntity(Entity entity) {
        entity.valid = false;

        if (world.addEntity(entity, CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            entity.getPassengers().forEach(this::addEntity);
            return true;
        }

        return false;
    }

    private StackedEntity getTargetEntity(StackedSpawner stackedSpawner, StackedEntity demoEntity,
                                          List<StackedEntity> nearbyEntities, AtomicInteger nearbyAndStackableCount) {
        if (!plugin.getSettings().entitiesStackingEnabled)
            return null;

        LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

        boolean adjustNearbyCounts = true;
        StackedEntity targetEntity;

        if (linkedEntity != null && linkedEntity.getType() == demoEntity.getType()) {
            targetEntity = WStackedEntity.of(linkedEntity);
            adjustNearbyCounts = nearbyEntities.contains(targetEntity);
        } else {
            targetEntity = GeneralUtils.getClosest(stackedSpawner.getLocation(), nearbyEntities.stream()).orElse(null);
        }

        if (targetEntity != null && adjustNearbyCounts) {
            nearbyAndStackableCount.addAndGet(-targetEntity.getStackAmount());
        }

        return targetEntity;
    }

    private void updateDemoEntity() {
        org.bukkit.entity.Entity demoEntityBukkit = generateEntity(position.getX(), position.getY(), position.getZ(), false);

        if (demoEntityBukkit != null)
            world.unregisterEntity(((CraftEntity) demoEntityBukkit).getHandle());

        if (demoEntityBukkit == null || !EntityUtils.isStackable(demoEntityBukkit)) {
            demoEntity = null;
            return;
        }

        demoEntity = (WStackedEntity) WStackedEntity.of(demoEntityBukkit);
        demoEntity.setSpawnCause(SpawnCause.SPAWNER);
        demoEntity.setDemoEntity();
    }

}
