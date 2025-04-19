package com.bgsoftware.wildstacker.nms.v1_8_R3.spawner;

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
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityOcelot;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.MobSpawnerAbstract;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R3.UtilColor;
import net.minecraft.server.v1_8_R3.WeightedRandom;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class StackedMobSpawner extends MobSpawnerAbstract {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(
            TileEntityMobSpawner.class, MobSpawnerAbstract.class, "a").removeFinal();

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private final World world;
    private final BlockPosition position;
    private final WeakReference<WStackedSpawner> stackedSpawner;
    private final MobSpawnerAbstract originalMobSpawnerAbstract;
    private final List<a> mobs = new ArrayList<>();
    public int minSpawnDelay = 200;
    public int maxSpawnDelay = 800;
    public int spawnCount = 4;
    public int maxNearbyEntities = 6;
    public int requiredPlayerRange = 16;
    public int spawnRange = 4;
    public String failureReason = "";
    private String mobName;
    private a spawnData;
    private int spawnedEntities = 0;
    private WStackedEntity demoEntity = null;

    public StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackedSpawner stackedSpawner) {
        boolean isDebug = ((WStackedSpawner) stackedSpawner).isDebug();

        if (isDebug)
            Debug.debug("StackedMobSpawner", "init", "Creating new spawner");

        this.world = tileEntityMobSpawner.getWorld();
        this.position = tileEntityMobSpawner.getPosition();
        this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

        MobSpawnerAbstract originalSpawner = tileEntityMobSpawner.getSpawner();

        if (isDebug)
            Debug.debug("StackedMobSpawner", "init", "originalSpawner=" + originalSpawner);

        this.originalMobSpawnerAbstract = originalSpawner;
        MOB_SPAWNER_ABSTRACT.set(tileEntityMobSpawner, this);

        if (isDebug)
            Debug.debug("StackedMobSpawner", "init", "After set: " + tileEntityMobSpawner.getSpawner());

        NBTTagCompound tagCompound = new NBTTagCompound();
        originalSpawner.b(tagCompound);
        a(tagCompound);
        this.mobs.clear();

        this.spawnDelay = originalSpawner.spawnDelay;

        updateDemoEntity();
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
    public void c() {
        WStackedSpawner stackedSpawner = this.stackedSpawner.get();

        if (stackedSpawner == null) {
            // We want to remove this StackedBaseSpawner, so a new one will regenerate.
            MOB_SPAWNER_ABSTRACT.set(this.world.getTileEntity(this.position), this.originalMobSpawnerAbstract);
            super.c();
            return;
        }

        if (!hasNearbyPlayers()) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "No nearby players in range (" + this.requiredPlayerRange + ")");
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

        Entity demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
        String entityType = EntityTypes.b(demoNMSEntity);

        if (entityType == null || !entityType.equals(getMobName())) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "c", "No valid entity to spawn");
            updateDemoEntity();

            if (demoEntity == null) {
                if (stackedSpawner.isDebug())
                    Debug.debug("StackedMobSpawner", "c", "Demo entity is null after trying to update it");
                super.c();
                return;
            }

            demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
        }

        int stackAmount = stackedSpawner.getStackAmount();

        if (stackedSpawner.isDebug())
            Debug.debug("StackedMobSpawner", "c", "stackAmount=" + stackAmount);

        // Update the upgrade id of the demo entity
        updateUpgrade(stackedSpawner.getUpgradeId());

        List<? extends Entity> nearbyEntities = world.a(demoNMSEntity.getClass(), new AxisAlignedBB(
                position.getX(), position.getY(), position.getZ(),
                position.getX() + 1, position.getY() + 1, position.getZ() + 1
        ).grow(this.spawnRange, this.spawnRange, this.spawnRange));

        AtomicLong nearbyAndStackableCount = new AtomicLong(0);
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

        int minimumEntityRequirement = GeneralUtils.get(plugin.getSettings().getEntities().getMinimumRequiredEntities(), this.demoEntity, 1);

        int stackedEntityCount = Random.nextInt(1, this.spawnCount, stackAmount, 1.5);

        boolean canStackToTarget = nearbyAndStackableCount.get() + targetEntityCount + stackedEntityCount >= minimumEntityRequirement;

        boolean spawnStacked = plugin.getSettings().getEntities().isEnabled() && canStackToTarget &&
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

                if (plugin.getSettings().getEntities().isLinkedEntitiesEnabled() && targetEntity.getLivingEntity() != stackedSpawner.getLinkedEntity())
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
                this.mobs.add(new a(nbttaglist.get(i)));
            }
        }

        if (nbttagcompound.hasKeyOfType("SpawnData", 10)) {
            this.a(new a(nbttagcompound.getCompound("SpawnData"), this.mobName));
        } else {
            this.a((a) null);
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
                    for (a mobData : this.mobs)
                        nbttaglist.add(mobData.a());
                } else {
                    nbttaglist.add(spawnData.a());
                }

                nbttagcompound.set("SpawnPotentials", nbttaglist);
            }
        }
    }

    @Override
    public void a(a spawnData) {
        this.spawnData = spawnData;
    }

    @Override
    public void a(int i) {
        world.playBlockAction(position, Blocks.MOB_SPAWNER, i, 0);
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

    public boolean isValid() {
        return this.stackedSpawner.get() != null;
    }

    private boolean attemptMobSpawning(int mobsToSpawn, int amountPerEntity, int spawnCount, short particlesAmount,
                                       WStackedSpawner stackedSpawner) {
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
                hasSpace = !world.containsLiquid(nmsEntity.getBoundingBox()) &&
                        world.getCubes(nmsEntity, nmsEntity.getBoundingBox()).isEmpty() &&
                        world.a(nmsEntity.getBoundingBox(), nmsEntity);
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
            a(WeightedRandom.a(this.a().random, this.mobs));
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

            entityInsentient.prepare(entity.world.E(new BlockPosition(entity)), null);

            if (entityInsentient.world.spigotConfig.nerfSpawnerMobs) {
                entityInsentient.fromMobSpawner = true;
            }
        }

        if (CraftEventFactory.callSpawnerSpawnEvent(entity, position.getX(), position.getY(), position.getZ()).isCancelled()) {
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
                world.triggerEffect(2004, position, 0);

            if (entity instanceof EntityInsentient) {
                ((EntityInsentient) entity).y();
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

        // Making sure the entity is not tracked, as sometimes entities already tracked.
        ((WorldServer) world).getTracker().untrackEntity(entity);

        try {
            if (world.addEntity(entity, CreatureSpawnEvent.SpawnReason.SPAWNER)) {
                if (entity.passenger != null)
                    addEntity(entity.passenger);
                return true;
            }
        } catch (Exception ignored) {
        }

        // Failed to add the entity - making sure to remove it.
        world.removeEntity(entity);

        return false;
    }

    private StackedEntity getTargetEntity(StackedSpawner stackedSpawner, StackedEntity demoEntity,
                                          List<StackedEntity> nearbyEntities, AtomicLong nearbyAndStackableCount) {
        if (!plugin.getSettings().getEntities().isEnabled())
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

        if (demoEntityBukkit == null || !EntityUtils.isStackable(demoEntityBukkit)) {
            demoEntity = null;
            return;
        }

        demoEntity = (WStackedEntity) WStackedEntity.of(demoEntityBukkit);
        demoEntity.setSpawnCause(SpawnCause.SPAWNER);
        demoEntity.setDemoEntity();
    }

}
