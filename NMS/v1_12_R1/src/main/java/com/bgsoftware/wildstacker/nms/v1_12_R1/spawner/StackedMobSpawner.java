package com.bgsoftware.wildstacker.nms.v1_12_R1.spawner;

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
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.ChunkRegionLoader;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityOcelot;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_12_R1.MobSpawnerData;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_12_R1.UtilColor;
import net.minecraft.server.v1_12_R1.WeightedRandom;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
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
    private final List<MobSpawnerData> mobs = new ArrayList<>();
    public int minSpawnDelay = 200;
    public int maxSpawnDelay = 800;
    public int spawnCount = 4;
    public int maxNearbyEntities = 6;
    public int requiredPlayerRange = 16;
    public int spawnRange = 4;
    public String failureReason = "";
    private MobSpawnerData spawnData = new MobSpawnerData();
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

        a(originalSpawner.b(new NBTTagCompound()));
        this.mobs.clear();

        this.spawnDelay = originalSpawner.spawnDelay;

        updateDemoEntity();
    }

    @Override
    public MinecraftKey getMobName() {
        String s = this.spawnData.b().getString("id");
        return UtilColor.b(s) ? null : new MinecraftKey(s);
    }

    @Override
    public void setMobName(@Nullable MinecraftKey minecraftkey) {
        if (minecraftkey != null) {
            this.spawnData.b().setString("id", minecraftkey.toString());
        }
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
        MinecraftKey entityType = EntityTypes.a(demoNMSEntity);

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
        ).g(this.spawnRange));

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
        this.spawnDelay = nbttagcompound.getShort("Delay");
        this.mobs.clear();
        if (nbttagcompound.hasKeyOfType("SpawnPotentials", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getList("SpawnPotentials", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                this.mobs.add(new MobSpawnerData(nbttaglist.get(i)));
            }
        }

        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("SpawnData");
        if (!nbttagcompound1.hasKeyOfType("id", 8)) {
            nbttagcompound1.setString("id", "Pig");
        }

        this.a(new MobSpawnerData(1, nbttagcompound1));
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
    public NBTTagCompound b(NBTTagCompound nbttagcompound) {
        MinecraftKey minecraftkey = this.getMobName();

        if (minecraftkey != null) {
            nbttagcompound.setShort("Delay", (short) this.spawnDelay);
            nbttagcompound.setShort("MinSpawnDelay", (short) this.minSpawnDelay);
            nbttagcompound.setShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
            nbttagcompound.setShort("SpawnCount", (short) this.spawnCount);
            nbttagcompound.setShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
            nbttagcompound.setShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
            nbttagcompound.setShort("SpawnRange", (short) this.spawnRange);
            nbttagcompound.set("SpawnData", this.spawnData.b().clone());
            NBTTagList nbttaglist = new NBTTagList();
            if (!this.mobs.isEmpty()) {
                for (MobSpawnerData mobSpawnerData : this.mobs)
                    nbttaglist.add(mobSpawnerData.a());
            } else {
                nbttaglist.add(this.spawnData.a());
            }

            nbttagcompound.set("SpawnPotentials", nbttaglist);
        }

        return nbttagcompound;
    }

    @Override
    public void a(MobSpawnerData mobspawnerdata) {
        this.spawnData = mobspawnerdata;
        IBlockData blockData = world.getType(position);
        world.notify(position, blockData, blockData, 4);
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

        boolean hasSpawnedEntities = false;

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
                hasSpawnedEntities = true;
            }
        }

        return hasSpawnedEntities;
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
        NBTTagCompound entityCompound = this.spawnData.b();
        Entity entity = ChunkRegionLoader.a(entityCompound, world, x, y, z, false);
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

            if (this.spawnData.b().d() == 1 && this.spawnData.b().hasKeyOfType("id", 8)) {
                entityInsentient.prepare(world.D(new BlockPosition(entity)), null);
            }

            if (entityInsentient.world.spigotConfig.nerfSpawnerMobs) {
                entityInsentient.fromMobSpawner = true;
            }
        }

        if (CraftEventFactory.callSpawnerSpawnEvent(entity, position).isCancelled()) {
            if (stackedSpawner.isDebug())
                Debug.debug("StackedMobSpawner", "handleEntitySpawn", "SpawnerStackEvent was cancelled");

            Entity vehicle = entity.getVehicle();

            if (vehicle != null) {
                vehicle.dead = true;
            }

            for (Entity passenger : entity.passengers)
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
            entity.bF().forEach(this::addEntity);
            return true;
        }

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

