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
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
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
import java.util.List;
import java.util.Optional;

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
        this.world = tileEntityMobSpawner.getWorld();
        this.position = new BlockPosition(tileEntityMobSpawner.x, tileEntityMobSpawner.y, tileEntityMobSpawner.z);
        this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

        MobSpawnerAbstract originalSpawner = tileEntityMobSpawner.getSpawner();
        MOB_SPAWNER_ABSTRACT.set(tileEntityMobSpawner, this);

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
            failureReason = "There are no nearby players.";
            return;
        }

        if (this.spawnDelay == -1)
            resetSpawnDelay();

        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }

        if (demoEntity == null) {
            super.g();
            return;
        }

        Entity demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
        String entityType = EntityTypes.b(demoNMSEntity);

        if (entityType == null || !entityType.equals(getMobName())) {
            updateDemoEntity();

            if (demoEntity == null) {
                super.g();
                return;
            }

            updateUpgrade(stackedSpawner.getUpgradeId());

            demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
        }

        int stackAmount = stackedSpawner.getStackAmount();

        //noinspection unchecked
        List<? extends Entity> nearbyEntities = world.a(demoNMSEntity.getClass(), AxisAlignedBB.a(
                position.x, position.y, position.z,
                position.x + 1, position.y + 1, position.z + 1
        ).grow(this.spawnRange, this.spawnRange, this.spawnRange));

        StackedEntity targetEntity = getTargetEntity(stackedSpawner, demoEntity, nearbyEntities);

        if (targetEntity == null && nearbyEntities.size() >= this.maxNearbyEntities) {
            failureReason = "There are too many nearby entities.";
            return;
        }

        boolean spawnStacked = EventsCaller.callSpawnerStackedEntitySpawnEvent(stackedSpawner.getSpawner());
        failureReason = "";

        int spawnCount = !spawnStacked || !demoEntity.isCached() ? Random.nextInt(1, this.spawnCount, stackAmount) :
                Random.nextInt(1, this.spawnCount, stackAmount, 1.5);

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

                world.triggerEffect(2004, position.x, position.y, position.z, 0);
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
        boolean hasSpawnedEntity = false;

        for (int i = 0; i < mobsToSpawn; i++) {
            double x = position.x + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;
            double y = position.y + world.random.nextInt(3) - 1;
            double z = position.z + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;

            org.bukkit.entity.Entity bukkitEntity = generateEntity(x, y, z, true);

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
                failureReason = "Not enough space to spawn the entity.";
                continue;
            }

            Location location = new Location(world.getWorld(), x, y, z);

            SpawnCondition failedCondition = plugin.getSystemManager().getSpawnConditions(demoEntity.getLivingEntity().getType())
                    .stream().filter(spawnCondition -> !spawnCondition.test(location)).findFirst().orElse(null);

            if (failedCondition != null) {
                failureReason = "Cannot spawn entities due to " + failedCondition.getName() + " restriction.";
                continue;
            }

            int amountToSpawn = spawnedEntities + amountPerEntity > spawnCount ? spawnCount - spawnedEntities : amountPerEntity;

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
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        StackedEntity stackedEntity = null;

        EntityStorage.setMetadata(bukkitEntity, EntityFlag.SPAWN_CAUSE, SpawnCause.SPAWNER);

        if (amountPerEntity > 1 || stackedSpawner.getUpgradeId() != 0) {
            stackedEntity = WStackedEntity.of(bukkitEntity);
            ((WStackedEntity) stackedEntity).setUpgradeId(stackedSpawner.getUpgradeId());
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
            if (stackedEntity != null)
                plugin.getSystemManager().removeStackObject(stackedEntity);
            EntityStorage.clearMetadata(bukkitEntity);
        } else {
            if (!addEntity(entity)) {
                EntityStorage.clearMetadata(bukkitEntity);
                return false;
            }

            if (spawnParticles)
                world.triggerEffect(2004, position.x, position.y, position.z, 0);

            if (entity instanceof EntityInsentient) {
                ((EntityInsentient) entity).s();
            }

            return true;
        }

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

