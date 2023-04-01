package com.bgsoftware.wildstacker.nms.v1_7_R4.spawner;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.nms.v1_7_R4.world.BlockPosition;
import com.bgsoftware.wildstacker.stacker.entities.WStackedEntity;
import com.bgsoftware.wildstacker.stacker.spawners.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Debug;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import net.minecraft.server.v1_7_R4.AxisAlignedBB;
import net.minecraft.server.v1_7_R4.Blocks;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.EntityOcelot;
import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.MobSpawnerAbstract;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.TileEntityMobSpawner;
import net.minecraft.server.v1_7_R4.TileEntityMobSpawnerData;
import net.minecraft.server.v1_7_R4.UtilColor;
import net.minecraft.server.v1_7_R4.WeightedRandom;
import net.minecraft.server.v1_7_R4.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StackedMobSpawner extends MobSpawnerAbstract {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(
            TileEntityMobSpawner.class, MobSpawnerAbstract.class, "a").removeFinal();

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final World world;
    private final BlockPosition position;
    private final WeakReference<WStackedSpawner> stackedSpawner;
    private final List<TileEntityMobSpawnerData> mobs = new ArrayList<>();
    public int minSpawnDelay = 200;
    public int maxSpawnDelay = 800;
    public int spawnCount = 4;
    public int maxNearbyEntities = 6;
    public int requiredPlayerRange = 16;
    public int spawnRange = 4;
    public String failureReason = "";
    private String mobName;
    private TileEntityMobSpawnerData spawnData;
    private int spawnedEntities = 0;
    private WStackedEntity demoEntity = null;

    public StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackedSpawner stackedSpawner) {
        boolean isDebug = ((WStackedSpawner) stackedSpawner).isDebug();

        if (isDebug)
            Debug.debug("StackedMobSpawner", "init", "Creating new spawner");

        this.world = tileEntityMobSpawner.getWorld();
        this.position = new BlockPosition(tileEntityMobSpawner.x, tileEntityMobSpawner.y, tileEntityMobSpawner.z);
        this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

        MobSpawnerAbstract originalSpawner = tileEntityMobSpawner.getSpawner();

        if (isDebug)
            Debug.debug("StackedMobSpawner", "init", "originalSpawner=" + originalSpawner);

        MOB_SPAWNER_ABSTRACT.set(tileEntityMobSpawner, this);

        if (isDebug)
            Debug.debug("StackedMobSpawner", "init", "After set: " + tileEntityMobSpawner.getSpawner());

        NBTTagCompound tagCompound = new NBTTagCompound();
        originalSpawner.b(tagCompound);
        a(tagCompound);

        updateDemoEntity();
        updateUpgrade(((WStackedSpawner) stackedSpawner).getUpgradeId());
    }

    @Override
    public String getMobName() {
        if (spawnData == null) {
            if (this.mobName == null) {
                this.mobName = "Pig";
            } else if (this.mobName.equals("Minecart")) {
                this.mobName = "MinecartRideable";
            }

            return this.mobName;
        } else {
            return spawnData.a().getString("Type");
        }
    }

    @Override
    public void setMobName(String mobName) {
        this.mobName = mobName;
    }

    @Override
    public void g() {
        WStackedSpawner stackedSpawner = this.stackedSpawner.get();

        if (stackedSpawner == null) {
            super.g();
            return;
        }

        if (!hasNearbyPlayers()) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "g", "No nearby players in range (" + this.requiredPlayerRange + ")");
            failureReason = "There are no nearby players.";
            return;
        }

        if (this.spawnDelay == -1)
            resetSpawnDelay();

        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }

        if (this.demoEntity == null) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "g", "Demo entity is null, trying to update it.");
            updateDemoEntity();
            if (this.demoEntity == null) {
                if (stackedSpawner.isDebug())
                    Debug.debug("StackedMobSpawner", "g", "Demo entity is null again, aborting.");
                super.g();
                failureReason = "";
                return;
            }
        }

        Entity demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
        String entityType = EntityTypes.b(demoNMSEntity);

        if (entityType == null || !entityType.equals(getMobName())) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "g", "No valid entity to spawn");
            updateDemoEntity();

            if (demoEntity == null) {
                if (stackedSpawner.isDebug())
                    Debug.debug("StackedMobSpawner", "g", "Demo entity is null after trying to update it");
                super.g();
                return;
            }

            updateUpgrade(stackedSpawner.getUpgradeId());

            demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
        }

        int stackAmount = stackedSpawner.getStackAmount();

        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "g", "stackAmount=" + stackAmount);

        //noinspection unchecked
        List<? extends Entity> nearbyEntities = world.a(demoNMSEntity.getClass(), AxisAlignedBB.a(
                position.x, position.y, position.z,
                position.x + 1, position.y + 1, position.z + 1
        ).grow(this.spawnRange, this.spawnRange, this.spawnRange));

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
            Debug.debug("StackedMobSpawner", "g", "targetEntity=" + targetEntity);

        if (targetEntity == null && nearbyEntities.size() >= this.maxNearbyEntities) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "g", "There are too many nearby entities (" + nearbyEntities.size() + ">" + this.maxNearbyEntities + ")");
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
            Debug.debug("StackedMobSpawner", "g", "spawnStacked=" + spawnStacked);

        int spawnCount = !spawnStacked || !demoEntity.isCached() ? Random.nextInt(1, this.spawnCount, stackAmount) :
                stackedEntityCount;

        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "g", "spawnCount=" + spawnCount);

        int amountPerEntity = 1;
        int mobsToSpawn;

        short particlesAmount = 0;

        // Try stacking into the target entity first
        if (targetEntity != null && canStackToTarget && EventsCaller.callEntityStackEvent(targetEntity, demoEntity)) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "g", "Stacking into the target entity");

            int targetEntityStackLimit = targetEntity.getStackLimit();
            int currentStackAmount = targetEntity.getStackAmount();
            int increaseStackAmount = Math.min(spawnCount, targetEntityStackLimit - currentStackAmount);

            if (increaseStackAmount != spawnCount) {
                mobsToSpawn = spawnCount - increaseStackAmount;
            } else {
                mobsToSpawn = 0;
            }

            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "g", "increaseStackAmount=" + increaseStackAmount);

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

                world.triggerEffect(2004, position.x, position.y, position.z, 0);
                particlesAmount++;
            }
        } else {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "g", "Stacking naturally");
            mobsToSpawn = spawnCount;
        }

        if (mobsToSpawn > 0 && demoEntity.isCached() && spawnStacked) {
            amountPerEntity = Math.min(mobsToSpawn, demoEntity.getStackLimit());
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "g", "amountPerEntity=" + amountPerEntity);
            mobsToSpawn = mobsToSpawn / amountPerEntity;
        }

        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "g", "mobsToSpawn=" + mobsToSpawn);

        while (spawnedEntities < stackAmount) {
            if (!attemptMobSpawning(mobsToSpawn, amountPerEntity, spawnCount, particlesAmount, stackedSpawner))
                return;
        }

        resetSpawnDelay();
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        this.mobName = nbttagcompound.getString("EntityId");
        this.spawnDelay = nbttagcompound.getShort("Delay");
        this.mobs.clear();
        if (nbttagcompound.hasKeyOfType("SpawnPotentials", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getList("SpawnPotentials", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                this.mobs.add(new TileEntityMobSpawnerData(this, nbttaglist.get(i)));
            }
        }

        if (nbttagcompound.hasKeyOfType("SpawnData", 10)) {
            this.a(new TileEntityMobSpawnerData(this, nbttagcompound.getCompound("SpawnData"), this.mobName));
        } else {
            this.a((TileEntityMobSpawnerData) null);
        }

        if (nbttagcompound.hasKeyOfType("MinSpawnDelay", 99)) {
            this.minSpawnDelay = nbttagcompound.getShort("MinSpawnDelay");
            this.maxSpawnDelay = nbttagcompound.getShort("MaxSpawnDelay");
            this.spawnCount = nbttagcompound.getShort("SpawnCount");
        }

        if (nbttagcompound.hasKeyOfType("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = nbttagcompound.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = nbttagcompound.getShort("RequiredPlayerRange");
        }

        if (nbttagcompound.hasKeyOfType("SpawnRange", 99)) {
            this.spawnRange = nbttagcompound.getShort("SpawnRange");
        }
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        String s = this.getMobName();

        if (!UtilColor.b(s)) {
            nbttagcompound.setString("EntityId", s);
            nbttagcompound.setShort("Delay", (short) this.spawnDelay);
            nbttagcompound.setShort("MinSpawnDelay", (short) this.minSpawnDelay);
            nbttagcompound.setShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
            nbttagcompound.setShort("SpawnCount", (short) this.spawnCount);
            nbttagcompound.setShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
            nbttagcompound.setShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
            nbttagcompound.setShort("SpawnRange", (short) this.spawnRange);

            if (spawnData != null) {
                nbttagcompound.set("SpawnData", spawnData.a().getCompound("Properties").clone());
            }

            if (spawnData != null || this.mobs.size() > 0) {
                NBTTagList nbttaglist = new NBTTagList();

                if (this.mobs.size() > 0) {
                    for (TileEntityMobSpawnerData mobData : this.mobs)
                        nbttaglist.add(mobData.a());
                } else {
                    nbttaglist.add(spawnData.a());
                }

                nbttagcompound.set("SpawnPotentials", nbttaglist);
            }
        }
    }

    @Override
    public void a(TileEntityMobSpawnerData spawnData) {
        this.spawnData = spawnData;
        world.notify(position.x, position.y, position.z);
    }

    @Override
    public void a(int i) {
        world.playBlockAction(position.x, position.y, position.z, Blocks.MOB_SPAWNER, i, 0);
    }

    @Override
    public World a() {
        return world;
    }

    public int b() {
        return position.x;
    }

    public int c() {
        return position.y;
    }

    public int d() {
        return position.z;
    }

    public void updateUpgrade(int upgradeId) {
        if (demoEntity != null)
            demoEntity.setUpgradeId(upgradeId);
    }

    private boolean attemptMobSpawning(int mobsToSpawn, int amountPerEntity, int spawnCount, short particlesAmount,
                                       WStackedSpawner stackedSpawner) {
        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "attemptMobSpawning", "Attempting to spawn mob");

        boolean hasSpawnedEntity = false;

        for (int i = 0; i < mobsToSpawn; i++) {
            double x = position.x + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;
            double y = position.y + world.random.nextInt(3) - 1;
            double z = position.z + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;

            Location location = new Location(world.getWorld(), x, y, z);

            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "attemptMobSpawning", "location=" + location);

            org.bukkit.entity.Entity bukkitEntity = generateEntity(x, y, z, true);

            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "attemptMobSpawning", "bukkitEntity=" + bukkitEntity);

            if (bukkitEntity == null) {
                resetSpawnDelay();
                return false;
            }

            Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();

            boolean hasSpace;

            if (nmsEntity instanceof EntityOcelot) {
                World world = nmsEntity.world;
                hasSpace = !world.containsLiquid(nmsEntity.boundingBox) &&
                        world.getCubes(nmsEntity, nmsEntity.boundingBox).isEmpty() &&
                        world.b(nmsEntity.boundingBox);
            } else {
                hasSpace = !(nmsEntity instanceof EntityInsentient) || ((EntityInsentient) nmsEntity).canSpawn();
            }

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
        return world.findNearbyPlayer(position.x + 0.5D, position.y + 0.5D, position.z + 0.5D, this.requiredPlayerRange) != null;
    }

    private void resetSpawnDelay() {
        if (maxSpawnDelay <= minSpawnDelay) {
            spawnDelay = minSpawnDelay;
        } else {
            spawnDelay = minSpawnDelay + world.random.nextInt(maxSpawnDelay - minSpawnDelay);
        }

        if (!this.mobs.isEmpty()) {
            a((TileEntityMobSpawnerData) WeightedRandom.a(this.a().random, this.mobs));
        }

        spawnedEntities = 0;
        failureReason = "";

        a(1);
    }

    private org.bukkit.entity.Entity generateEntity(double x, double y, double z, boolean rotation) {
        Entity entity = EntityTypes.createEntityByName(this.getMobName(), world);

        if (entity == null)
            return null;

        entity.setPositionRotation(x, y, z, 0f, 0f);

        if (rotation)
            entity.yaw = world.random.nextFloat() * 360.0F;

        entity.world = world;
        entity.valid = true;
        entity.dead = false;

        return entity.getBukkitEntity();
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

            entityInsentient.prepare(null);

            if (entityInsentient.world.spigotConfig.nerfSpawnerMobs) {
                entityInsentient.fromMobSpawner = true;
            }
        }

        if (CraftEventFactory.callSpawnerSpawnEvent(entity, position.x, position.y, position.z).isCancelled()) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "handleEntitySpawn", "SpawnerStackEvent was cancelled");

            if (entity.vehicle != null)
                entity.vehicle.dead = true;

            if (entity.passenger != null)
                entity.passenger.dead = true;
        } else if (addEntity(entity)) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "handleEntitySpawn", "Successfully added entity to the world");

            if (spawnParticles)
                world.triggerEffect(2004, position.x, position.y, position.z, 0);

            if (entity instanceof EntityInsentient) {
                ((EntityInsentient) entity).s();
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
            if (entity.passenger != null)
                addEntity(entity.passenger);
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
        org.bukkit.entity.Entity demoEntityBukkit = generateEntity(position.x, position.y, position.z, false);

        if (demoEntityBukkit == null || !EntityUtils.isStackable(demoEntityBukkit)) {
            demoEntity = null;
            return;
        }

        demoEntity = (WStackedEntity) WStackedEntity.of(demoEntityBukkit);
        demoEntity.setSpawnCause(SpawnCause.SPAWNER);
        demoEntity.setDemoEntity();
    }

}

