package com.bgsoftware.wildstacker.nms.v1_17_R1.spawner;

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
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

public class StackedMobSpawner extends MobSpawnerAbstract {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(
            TileEntityMobSpawner.class, MobSpawnerAbstract.class, "a").removeFinal();

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final WeakReference<WStackedSpawner> stackedSpawner;
    public String failureReason = "";

    private int spawnedEntities = 0;
    private WStackedEntity demoEntity = null;

    public StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackedSpawner stackedSpawner) {
        this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

        MobSpawnerAbstract originalSpawner = tileEntityMobSpawner.getSpawner();
        MOB_SPAWNER_ABSTRACT.set(tileEntityMobSpawner, this);

        this.f = originalSpawner.f;
        this.i = originalSpawner.i;
        this.j = originalSpawner.j;
        this.k = originalSpawner.k;
        this.m = originalSpawner.m;
        this.n = originalSpawner.n;
        this.o = originalSpawner.o;

        updateDemoEntity((WorldServer) tileEntityMobSpawner.getWorld(), tileEntityMobSpawner.getPosition());
        updateUpgrade(((WStackedSpawner) stackedSpawner).getUpgradeId());
    }

    @Override
    public void a(WorldServer world, BlockPosition position) {
        WStackedSpawner stackedSpawner = this.stackedSpawner.get();

        if (stackedSpawner == null) {
            super.a(world, position);
            return;
        }

        if (!hasNearbyPlayers(world, position)) {
            failureReason = "There are no nearby players.";
            return;
        }

        if (this.d <= -1)
            resetSpawnDelay(world, position);

        if (this.d > 0) {
            --this.d;
            return;
        }

        Optional<EntityTypes<?>> entityTypesOptional = EntityTypes.a(this.f.getEntity());

        if (!entityTypesOptional.isPresent()) {
            resetSpawnDelay(world, position);
            failureReason = "";
            return;
        }

        EntityTypes<?> entityTypes = entityTypesOptional.get();

        if (demoEntity == null) {
            super.a(world, position);
            failureReason = "";
            return;
        }

        Entity demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();

        if (demoNMSEntity.getEntityType() != entityTypes) {
            updateDemoEntity(world, position);

            if (demoEntity == null) {
                super.a(world, position);
                failureReason = "";
                return;
            }

            updateUpgrade(stackedSpawner.getUpgradeId());

            demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
        }

        int stackAmount = stackedSpawner.getStackAmount();

        List<? extends Entity> nearbyEntities = world.a(demoNMSEntity.getClass(), new AxisAlignedBB(
                position.getX(), position.getY(), position.getZ(),
                position.getX() + 1, position.getY() + 1, position.getZ() + 1
        ).g(this.o));

        StackedEntity targetEntity = getTargetEntity(stackedSpawner, demoEntity, nearbyEntities);

        if (targetEntity == null && nearbyEntities.size() >= this.m) {
            failureReason = "There are too many nearby entities.";
            return;
        }

        boolean spawnStacked = EventsCaller.callSpawnerStackedEntitySpawnEvent(stackedSpawner.getSpawner());
        failureReason = "";

        int spawnCount = !spawnStacked || !demoEntity.isCached() ? Random.nextInt(1, this.k, stackAmount) :
                Random.nextInt(1, this.k, stackAmount, 1.5);

        int amountPerEntity = 1;
        int mobsToSpawn;

        short particlesAmount = 0;

        // Try stacking into the target entity first
        if (targetEntity != null && EventsCaller.callEntityStackEvent(targetEntity, demoEntity)) {
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
                demoEntity.spawnStackParticle(true);

                if (plugin.getSettings().linkedEntitiesEnabled && targetEntity.getLivingEntity() != stackedSpawner.getLinkedEntity())
                    stackedSpawner.setLinkedEntity(targetEntity.getLivingEntity());

                world.triggerEffect(2004, position, 0);
                particlesAmount++;
            }
        } else {
            mobsToSpawn = spawnCount;
        }

        if (mobsToSpawn > 0 && demoEntity.isCached() && spawnStacked) {
            amountPerEntity = Math.min(mobsToSpawn, demoEntity.getStackLimit());
            mobsToSpawn = mobsToSpawn / amountPerEntity;
        }

        while (spawnedEntities < stackAmount) {
            if (!attemptMobSpawning(world, position, entityTypes, mobsToSpawn, amountPerEntity, spawnCount, particlesAmount, stackedSpawner))
                return;
        }

        resetSpawnDelay(world, position);
    }

    @Override
    public void a(World world, BlockPosition position, int i) {
        world.playBlockAction(position, Blocks.bV, i, 0);
    }

    public void updateUpgrade(int upgradeId) {
        if (demoEntity != null)
            demoEntity.setUpgradeId(upgradeId);
    }

    private boolean attemptMobSpawning(WorldServer world, BlockPosition position, EntityTypes<?> entityTypes,
                                       int mobsToSpawn, int amountPerEntity, int spawnCount, short particlesAmount,
                                       WStackedSpawner stackedSpawner) {
        boolean hasSpawnedEntity = false;

        for (int i = 0; i < mobsToSpawn; i++) {
            double x = position.getX() + (world.getRandom().nextDouble() - world.getRandom().nextDouble()) * this.o + 0.5D;
            double y = position.getY() + world.getRandom().nextInt(3) - 1;
            double z = position.getZ() + (world.getRandom().nextDouble() - world.getRandom().nextDouble()) * this.o + 0.5D;

            Location location = new Location(world.getWorld(), x, y, z);

            boolean hasSpace = world.b(entityTypes.a(x, y, z));

            if (!hasSpace) {
                failureReason = "Not enough space to spawn the entity.";
                continue;
            }

            SpawnCondition failedCondition = plugin.getSystemManager().getSpawnConditions(demoEntity.getLivingEntity().getType())
                    .stream().filter(spawnCondition -> !spawnCondition.test(location)).findFirst().orElse(null);

            if (failedCondition != null) {
                failureReason = "Cannot spawn entities due to " + failedCondition.getName() + " restriction.";
                continue;
            }

            org.bukkit.entity.Entity bukkitEntity = generateEntity(world, x, y, z, true);

            if (bukkitEntity == null) {
                resetSpawnDelay(world, position);
                return false;
            }

            int amountToSpawn = spawnedEntities + amountPerEntity > spawnCount ? spawnCount - spawnedEntities : amountPerEntity;

            if (handleEntitySpawn(world, position, bukkitEntity, stackedSpawner, amountToSpawn, particlesAmount <= this.k)) {
                spawnedEntities += amountPerEntity;
                particlesAmount++;
                hasSpawnedEntity = true;
            }
        }

        return hasSpawnedEntity;
    }

    private boolean hasNearbyPlayers(World world, BlockPosition position) {
        return world.isPlayerNearby(position.getX() + 0.5D, position.getY() + 0.5D,
                position.getZ() + 0.5D, this.n);
    }

    private void resetSpawnDelay(World world, BlockPosition position) {
        if (this.j <= this.i) {
            this.d = this.i;
        } else {
            this.d = this.i + world.getRandom().nextInt(this.j - this.i);
        }

        this.e.b(world.getRandom()).ifPresent(mobSpawnerData -> this.setSpawnData(world, position, mobSpawnerData));

        spawnedEntities = 0;
        failureReason = "";

        a(world, position, 1);
    }

    private org.bukkit.entity.Entity generateEntity(World world, double x, double y, double z, boolean rotation) {
        NBTTagCompound entityCompound = this.f.getEntity();
        Entity entity = EntityTypes.a(entityCompound, world, _entity -> {
            _entity.setPositionRotation(x, y, z, rotation ? world.getRandom().nextFloat() * 360.0F : 0f, 0f);

            _entity.t = world;
            _entity.valid = true;

            return _entity;
        });
        return entity == null ? null : entity.getBukkitEntity();
    }

    private boolean handleEntitySpawn(WorldServer world, BlockPosition position,
                                      org.bukkit.entity.Entity bukkitEntity, WStackedSpawner stackedSpawner,
                                      int amountPerEntity, boolean spawnParticles) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        StackedEntity stackedEntity = null;

        EntityStorage.setMetadata(bukkitEntity, EntityFlag.SPAWN_CAUSE, SpawnCause.SPAWNER);

        if (amountPerEntity > 1 || stackedSpawner.getUpgradeId() != 0) {
            stackedEntity = WStackedEntity.of(bukkitEntity);
            ((WStackedEntity) stackedEntity).setUpgradeId(stackedSpawner.getUpgradeId());
            stackedEntity.setStackAmount(amountPerEntity, true);
        }

        if (entity instanceof EntityInsentient entityInsentient) {
            if (this.f.getEntity().e() == 1 && this.f.getEntity().hasKeyOfType("id", 8)) {
                entityInsentient.prepare(world, world.getDamageScaler(entity.getChunkCoordinates()),
                        EnumMobSpawn.c, null, null);
            }

            if (entityInsentient.getWorld().spigotConfig.nerfSpawnerMobs) {
                entityInsentient.aware = false;
            }
        }

        if (CraftEventFactory.callSpawnerSpawnEvent(entity, position).isCancelled()) {
            Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                vehicle.die();
            }

            for (Entity passenger : entity.getAllPassengers())
                passenger.die();

            if (stackedEntity != null)
                plugin.getSystemManager().removeStackObject(stackedEntity);
            EntityStorage.clearMetadata(bukkitEntity);
        } else {
            if (!addEntity(world, entity)) {
                EntityStorage.clearMetadata(bukkitEntity);
                return false;
            }

            if (spawnParticles)
                world.triggerEffect(2004, position, 0);

            if (entity instanceof EntityInsentient entityInsentient) {
                entityInsentient.doSpawnEffect();
            }

            return true;
        }

        return false;
    }

    private boolean addEntity(World world, Entity entity) {
        entity.valid = false;

        if (world.addEntity(entity, CreatureSpawnEvent.SpawnReason.SPAWNER)) {
            entity.getPassengers().forEach(passenger -> addEntity(world, passenger));
            return true;
        }

        return false;
    }

    private StackedEntity getTargetEntity(StackedSpawner stackedSpawner, StackedEntity demoEntity,
                                          List<? extends Entity> nearbyEntities) {
        LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

        if (linkedEntity != null && linkedEntity.getType() == demoEntity.getType())
            return WStackedEntity.of(linkedEntity);

        Optional<CraftEntity> closestEntity = GeneralUtils.getClosestBukkit(stackedSpawner.getLocation(),
                nearbyEntities.stream().map(Entity::getBukkitEntity).filter(entity ->
                        EntityUtils.isStackable(entity) &&
                                demoEntity.runStackCheck(WStackedEntity.of(entity)) == StackCheckResult.SUCCESS));

        return closestEntity.map(WStackedEntity::of).orElse(null);
    }

    private static final ReflectField<LevelCallback<Entity>> WORLD_LEVEL_CALLBACK = new ReflectField<>(PersistentEntitySectionManager.class, LevelCallback.class, "c");

    private void updateDemoEntity(WorldServer world, BlockPosition position) {
        org.bukkit.entity.Entity demoEntityBukkit = generateEntity(world, position.getX(), position.getY(),
                position.getZ(), false);

        if (demoEntityBukkit != null)
            WORLD_LEVEL_CALLBACK.get(world.G).a(((CraftEntity) demoEntityBukkit).getHandle());

        if (!EntityUtils.isStackable(demoEntityBukkit)) {
            demoEntity = null;
            return;
        }

        demoEntity = (WStackedEntity) WStackedEntity.of(demoEntityBukkit);
        demoEntity.setSpawnCause(SpawnCause.SPAWNER);
        demoEntity.setDemoEntity();
    }

}
