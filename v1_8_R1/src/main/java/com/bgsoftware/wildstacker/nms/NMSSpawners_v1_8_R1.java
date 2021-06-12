package com.bgsoftware.wildstacker.nms;

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
import net.minecraft.server.v1_8_R1.AxisAlignedBB;
import net.minecraft.server.v1_8_R1.BiomeBase;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.Entity;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.EntityOcelot;
import net.minecraft.server.v1_8_R1.EntityTypes;
import net.minecraft.server.v1_8_R1.EnumDifficulty;
import net.minecraft.server.v1_8_R1.EnumSkyBlock;
import net.minecraft.server.v1_8_R1.Material;
import net.minecraft.server.v1_8_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagList;
import net.minecraft.server.v1_8_R1.TileEntity;
import net.minecraft.server.v1_8_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R1.TileEntityMobSpawnerData;
import net.minecraft.server.v1_8_R1.UtilColor;
import net.minecraft.server.v1_8_R1.WeightedRandom;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R1.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public final class NMSSpawners_v1_8_R1 implements NMSSpawners {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(TileEntityMobSpawner.class, MobSpawnerAbstract.class, "a").removeFinal();

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    @Override
    public boolean updateStackedSpawner(StackedSpawner stackedSpawner) {
        World world = ((CraftWorld) stackedSpawner.getWorld()).getHandle();
        Location location = stackedSpawner.getLocation();

        TileEntity tileEntity = world.getTileEntity(new BlockPosition(location.getX(), location.getY(), location.getZ()));
        if(tileEntity instanceof TileEntityMobSpawner && !(((TileEntityMobSpawner) tileEntity).getSpawner() instanceof StackedMobSpawner)) {
            new StackedMobSpawner((TileEntityMobSpawner) tileEntity, stackedSpawner);
            return true;
        }

        return false;
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ABOVE_SEA_LEVEL",
                (world, position) -> position.getY() >= 63,
                EntityType.OCELOT
        );

        createCondition("ANIMAL_LIGHT",
                (world, position) -> world.k(position) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.HORSE, EntityType.MUSHROOM_COW, EntityType.PIG,
                EntityType.RABBIT, EntityType.SHEEP, EntityType.WOLF
        );

        createCondition("IN_SLIME_CHUNK_OR_SWAMP",
                (world, position) -> world.getBiome(position) == BiomeBase.SWAMPLAND || world.getChunkAtWorldCoords(position)
                        .a(987234911L).nextInt(10) == 0 && position.getY() < 40,
                EntityType.SLIME
        );

        createCondition("MONSTER_LIGHT", (world, position) -> {
            if (world.b(EnumSkyBlock.SKY, position) > world.random.nextInt(32)) {
                return false;
            } else {
                int lightLevel = world.getLightLevel(position);

                if (world.R()) {
                    int j = world.ab();
                    world.c(10);
                    lightLevel = world.getLightLevel(position);
                    world.c(j);
                }

                return lightLevel <= world.random.nextInt(8);
            }
        }, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.GIANT, EntityType.SKELETON,
                EntityType.SPIDER, EntityType.WITCH, EntityType.WITHER, EntityType.ZOMBIE
        );

        createCondition("NOT_PEACEFUL",
                (world, position) -> world.getDifficulty() != EnumDifficulty.PEACEFUL,
                EntityType.GUARDIAN, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.GHAST, EntityType.GIANT,
                EntityType.MAGMA_CUBE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.WITCH, EntityType.WITHER, EntityType.ZOMBIE, EntityType.PIG_ZOMBIE
        );

        createCondition("ON_GRASS",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.GRASS,
                EntityType.CHICKEN, EntityType.COW, EntityType.HORSE, EntityType.PIG, EntityType.SHEEP, EntityType.WOLF
        );

        createCondition("ON_GRASS_OR_LEAVES", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block == Blocks.GRASS || block.getMaterial() == Material.LEAVES;
        }, EntityType.OCELOT);

        createCondition("ON_GRASS_OR_SAND_OR_SNOW", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block == Blocks.GRASS || block == Blocks.SAND || block == Blocks.SNOW;
        }, EntityType.RABBIT);

        createCondition("ON_MYCELIUM",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.MYCELIUM,
                EntityType.MUSHROOM_COW
        );
    }

    private static void createCondition(String id, BiPredicate<World, BlockPosition> predicate, EntityType... entityTypes){
        SpawnCondition spawnCondition = SpawnCondition.register(new SpawnCondition(id, EntityUtils.format(id)) {
            @Override
            public boolean test(Location location) {
                return predicate.test(((CraftWorld) location.getWorld()).getHandle(),
                        new BlockPosition(location.getX(), location.getY(), location.getZ()));
            }
        });
        plugin.getSystemManager().addSpawnCondition(spawnCondition, entityTypes);
    }

    static class StackedMobSpawner extends MobSpawnerAbstract {

        private final World world;
        private final BlockPosition position;
        private final WeakReference<WStackedSpawner> stackedSpawner;
        private final List<TileEntityMobSpawnerData> mobs = new ArrayList<>();

        private String mobName;
        private TileEntityMobSpawnerData spawnData;
        public int minSpawnDelay = 200;
        public int maxSpawnDelay = 800;
        public int spawnCount = 4;
        public int maxNearbyEntities = 6;
        public int requiredPlayerRange = 16;
        public int spawnRange = 4;
        public String failureReason = "";

        private int spawnedEntities = 0;
        private WStackedEntity demoEntity = null;

        StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackedSpawner stackedSpawner){
            this.world = tileEntityMobSpawner.getWorld();
            this.position = tileEntityMobSpawner.getPosition();
            this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

            MobSpawnerAbstract originalSpawner = tileEntityMobSpawner.getSpawner();
            MOB_SPAWNER_ABSTRACT.set(tileEntityMobSpawner, this);

            NBTTagCompound tagCompound = new NBTTagCompound();
            originalSpawner.b(tagCompound);
            a(tagCompound);
            this.mobs.clear();

            updateDemoEntity();
            updateUpgrade(((WStackedSpawner) stackedSpawner).getUpgradeId());
        }

        @Override
        public String getMobName() {
            if (spawnData == null) {
                if (this.mobName == null) {
                    this.mobName = "Pig";
                }

                else if (this.mobName.equals("Minecart")) {
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
        public void a(NBTTagCompound nbttagcompound) {
            this.mobName = nbttagcompound.getString("EntityId");
            this.spawnDelay = nbttagcompound.getShort("Delay");
            this.mobs.clear();
            if (nbttagcompound.hasKeyOfType("SpawnPotentials", 9)) {
                NBTTagList nbttaglist = nbttagcompound.getList("SpawnPotentials", 10);

                for(int i = 0; i < nbttaglist.size(); ++i) {
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
                nbttagcompound.setShort("Delay", (short)this.spawnDelay);
                nbttagcompound.setShort("MinSpawnDelay", (short)this.minSpawnDelay);
                nbttagcompound.setShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
                nbttagcompound.setShort("SpawnCount", (short)this.spawnCount);
                nbttagcompound.setShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
                nbttagcompound.setShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
                nbttagcompound.setShort("SpawnRange", (short)this.spawnRange);

                if (spawnData != null) {
                    nbttagcompound.set("SpawnData", spawnData.a().getCompound("Properties").clone());
                }

                if (spawnData != null || this.mobs.size() > 0) {
                    NBTTagList nbttaglist = new NBTTagList();

                    if (this.mobs.size() > 0) {
                        for(TileEntityMobSpawnerData mobData : this.mobs)
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
            world.notify(position);
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

        @Override
        public void c() {
            WStackedSpawner stackedSpawner = this.stackedSpawner.get();

            if(stackedSpawner == null){
                super.c();
                return;
            }

            if(!hasNearbyPlayers()) {
                failureReason = "There are no nearby players.";
                return;
            }

            if (this.spawnDelay == -1)
                resetSpawnDelay();

            if (this.spawnDelay > 0) {
                --this.spawnDelay;
                return;
            }

            if(demoEntity == null){
                super.c();
                return;
            }

            Entity demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
            String entityType = EntityTypes.b(demoNMSEntity);

            if(entityType == null || !entityType.equals(getMobName())){
                updateDemoEntity();

                if(demoEntity == null){
                    super.c();
                    return;
                }

                updateUpgrade(stackedSpawner.getUpgradeId());

                demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
            }

            int stackAmount = stackedSpawner.getStackAmount();

            //noinspection unchecked
            List<? extends Entity> nearbyEntities = world.a(demoNMSEntity.getClass(), new AxisAlignedBB(
                    position.getX(), position.getY(), position.getZ(),
                    position.getX() + 1, position.getY() + 1, position.getZ() + 1
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
            if(targetEntity != null && EventsCaller.callEntityStackEvent(targetEntity, demoEntity)){
                int limit = targetEntity.getStackLimit();
                int newStackAmount = targetEntity.getStackAmount() + spawnCount;

                if(newStackAmount > limit) {
                    mobsToSpawn = newStackAmount - limit;
                    newStackAmount = limit;
                    spawnedEntities += limit - targetEntity.getStackAmount();
                }
                else{
                    mobsToSpawn = 0;
                    spawnedEntities += spawnCount;
                }

                targetEntity.setStackAmount(newStackAmount, true);
                demoEntity.spawnStackParticle(true);

                if(plugin.getSettings().linkedEntitiesEnabled && targetEntity.getLivingEntity() != stackedSpawner.getLinkedEntity())
                    stackedSpawner.setLinkedEntity(targetEntity.getLivingEntity());

                world.triggerEffect(2004, position, 0);
                particlesAmount++;
            }
            else{
                mobsToSpawn = spawnCount;
            }

            if(mobsToSpawn > 0 && demoEntity.isCached() && spawnStacked){
                amountPerEntity = Math.min(mobsToSpawn, demoEntity.getStackLimit());
                mobsToSpawn = mobsToSpawn / amountPerEntity;
            }

            for(int i = 0; i < mobsToSpawn; i++) {
                double x = position.getX() + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;
                double y = position.getY() + world.random.nextInt(3) - 1;
                double z = position.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * spawnRange + 0.5D;

                org.bukkit.entity.Entity bukkitEntity = generateEntity(x, y, z, true);

                if (bukkitEntity == null) {
                    resetSpawnDelay();
                    return;
                }

                Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();

                boolean hasSpace;

                if (nmsEntity instanceof EntityOcelot) {
                    World world = nmsEntity.world;
                    hasSpace = !world.containsLiquid(nmsEntity.getBoundingBox()) &&
                            world.getCubes(nmsEntity, nmsEntity.getBoundingBox()).isEmpty() &&
                            world.a(nmsEntity.getBoundingBox(), nmsEntity);
                }

                else {
                    hasSpace = !(nmsEntity instanceof EntityInsentient) || ((EntityInsentient) nmsEntity).canSpawn();
                }

                if(!hasSpace){
                    failureReason = "Not enough space to spawn the entity.";
                    continue;
                }

                Location location = new Location(world.getWorld(), x, y, z);

                SpawnCondition failedCondition = plugin.getSystemManager().getSpawnConditions(demoEntity.getLivingEntity().getType())
                        .stream().filter(spawnCondition -> !spawnCondition.test(location)).findFirst().orElse(null);

                if(failedCondition != null) {
                    failureReason = "Cannot spawn entities due to " + failedCondition.getName() + " restriction.";
                    continue;
                }

                int amountToSpawn = spawnedEntities + amountPerEntity > spawnCount ? spawnCount - spawnedEntities : amountPerEntity;

                if(handleEntitySpawn(bukkitEntity, stackedSpawner, amountToSpawn, particlesAmount <= this.spawnCount)) {
                    spawnedEntities += amountPerEntity;
                    particlesAmount++;
                }
            }

            if(spawnedEntities >= stackAmount)
                resetSpawnDelay();
        }

        public void updateUpgrade(int upgradeId){
            if(demoEntity != null)
                demoEntity.setUpgradeId(upgradeId);
        }

        private boolean hasNearbyPlayers(){
            return world.isPlayerNearby(position.getX() + 0.5D, position.getY() + 0.5D,
                    position.getZ() + 0.5D, requiredPlayerRange);
        }

        private void resetSpawnDelay(){
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

        private org.bukkit.entity.Entity generateEntity(double x, double y, double z, boolean rotation){
            Entity entity = EntityTypes.createEntityByName(this.getMobName(), world);

            if(entity == null)
                return null;

            entity.setPositionRotation(x, y, z, 0f, 0f);

            if(rotation)
                entity.yaw = world.random.nextFloat() * 360.0F;

            entity.world = world;
            entity.valid = true;
            entity.dead = false;

            return entity.getBukkitEntity();
        }

        private boolean handleEntitySpawn(org.bukkit.entity.Entity bukkitEntity, WStackedSpawner stackedSpawner, int amountPerEntity, boolean spawnParticles){
            Entity entity = ((CraftEntity) bukkitEntity).getHandle();
            StackedEntity stackedEntity = null;

            EntityStorage.setMetadata(bukkitEntity, EntityFlag.SPAWN_CAUSE, SpawnCause.SPAWNER);

            if(amountPerEntity > 1 || stackedSpawner.getUpgradeId() != 0) {
                stackedEntity = WStackedEntity.of(bukkitEntity);
                ((WStackedEntity) stackedEntity).setUpgradeId(stackedSpawner.getUpgradeId());
                stackedEntity.setStackAmount(amountPerEntity, true);
            }

            if (entity instanceof EntityInsentient) {
                EntityInsentient entityinsentient = (EntityInsentient)entity;

                ((EntityInsentient)entity).prepare(entity.world.E(new BlockPosition(entity)), null);

                if (entityinsentient.world.spigotConfig.nerfSpawnerMobs) {
                    entityinsentient.fromMobSpawner = true;
                }
            }

            if (CraftEventFactory.callSpawnerSpawnEvent(entity, position.getX(), position.getY(), position.getZ()).isCancelled()) {
                if(stackedEntity != null)
                    plugin.getSystemManager().removeStackObject(stackedEntity);
                EntityStorage.clearMetadata(bukkitEntity);
            }

            else {
                if(!addEntity(entity)){
                    EntityStorage.clearMetadata(bukkitEntity);
                    return false;
                }

                if(spawnParticles)
                    world.triggerEffect(2004, position, 0);

                if (entity instanceof EntityInsentient) {
                    ((EntityInsentient)entity).y();
                }

                return true;
            }

            return false;
        }

        private boolean addEntity(Entity entity) {
            entity.valid = false;

            if (world.addEntity(entity, CreatureSpawnEvent.SpawnReason.SPAWNER)) {
                if(entity.passenger != null)
                    addEntity(entity.passenger);
                return true;
            }

            return false;
        }

        private StackedEntity getTargetEntity(StackedSpawner stackedSpawner, StackedEntity demoEntity,
                                              List<? extends Entity> nearbyEntities){
            LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

            if(linkedEntity != null && linkedEntity.getType() == demoEntity.getType())
                return WStackedEntity.of(linkedEntity);

            Optional<CraftEntity> closestEntity = GeneralUtils.getClosestBukkit(stackedSpawner.getLocation(),
                    nearbyEntities.stream().map(Entity::getBukkitEntity).filter(entity ->
                            EntityUtils.isStackable(entity) &&
                                    demoEntity.runStackCheck(WStackedEntity.of(entity)) == StackCheckResult.SUCCESS));

            return closestEntity.map(WStackedEntity::of).orElse(null);
        }

        private void updateDemoEntity(){
            org.bukkit.entity.Entity demoEntityBukkit = generateEntity(position.getX(), position.getY(), position.getZ(), false);

            if(demoEntityBukkit == null || !EntityUtils.isStackable(demoEntityBukkit)){
                demoEntity = null;
                return;
            }

            demoEntity = (WStackedEntity) WStackedEntity.of(demoEntityBukkit);
            demoEntity.setSpawnCause(SpawnCause.SPAWNER);
            demoEntity.setDemoEntity();
        }

    }

}
