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
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.BiomeBase;
import net.minecraft.server.v1_13_R2.Biomes;
import net.minecraft.server.v1_13_R2.Block;
import net.minecraft.server.v1_13_R2.BlockLogAbstract;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.Blocks;
import net.minecraft.server.v1_13_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_13_R2.ChunkRegionLoader;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.EnumDifficulty;
import net.minecraft.server.v1_13_R2.EnumSkyBlock;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.MobSpawnerAbstract;
import net.minecraft.server.v1_13_R2.MobSpawnerData;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.SeededRandom;
import net.minecraft.server.v1_13_R2.TagsBlock;
import net.minecraft.server.v1_13_R2.TagsFluid;
import net.minecraft.server.v1_13_R2.TileEntity;
import net.minecraft.server.v1_13_R2.TileEntityMobSpawner;
import net.minecraft.server.v1_13_R2.WeightedRandom;
import net.minecraft.server.v1_13_R2.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public final class NMSSpawners_v1_13_R2 implements NMSSpawners {

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
                (world, position) -> position.getY() >= world.getSeaLevel(),
                EntityType.OCELOT
        );

        createCondition("ANIMAL_LIGHT",
                (world, position) -> world.getLightLevel(position, 0) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA,
                EntityType.MUSHROOM_COW, EntityType.MULE, EntityType.PARROT, EntityType.PIG, EntityType.RABBIT,
                EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.TURTLE, EntityType.WOLF, EntityType.ZOMBIE_HORSE
        );

        createCondition("ANIMAL_LIGHT_AND_COLD", (world, position) -> {
            BiomeBase biomeBase = world.getBiome(position);
            boolean coldBiome = biomeBase == Biomes.FROZEN_OCEAN || biomeBase == Biomes.DEEP_FROZEN_OCEAN;
            Block block = world.getType(position.down()).getBlock();
            return world.getLightLevel(position, 0) > 8 && block == (coldBiome ? Blocks.GRASS_BLOCK : Blocks.ICE);
        }, EntityType.POLAR_BEAR);

        createCondition("BELOW_SEA_LEVEL",
                (world, position) -> position.getY() < world.getSeaLevel(),
                EntityType.DOLPHIN
        );

        createCondition("IN_SEA_SURFACE",
                (world, position) -> position.getY() < world.getSeaLevel() + 4,
                EntityType.TURTLE
        );

        createCondition("IN_SLIME_CHUNK_OR_SWAMP", (world, position) -> {
            if(world.getBiome(position) == Biomes.SWAMP)
                return true;

            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(position);
            boolean isSlimeChunk = SeededRandom.a(chunkcoordintpair.x, chunkcoordintpair.z, world.getSeed(),
                    world.getMinecraftWorld().spigotConfig.slimeSeed).nextInt(10) == 0;
            return isSlimeChunk && position.getY() < 40;
        }, EntityType.SLIME);

        createCondition("IN_WATER_DEEP",
                (world, position) -> world.getFluid(position).a(TagsFluid.WATER) && world.getFluid(position.up()).a(TagsFluid.WATER),
                EntityType.COD, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.DOLPHIN
        );

        createCondition("MONSTER_LIGHT", (world, position) -> {
            if (world.getBrightness(EnumSkyBlock.SKY, position) > world.random.nextInt(32)) {
                return false;
            } else {
                int lightLevel = world.Y() ? world.d(position, 10) : world.getLightLevel(position);
                return lightLevel <= world.random.nextInt(8);
            }
        }, EntityType.DROWNED, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.GIANT,
                EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.VEX, EntityType.VINDICATOR
        );

        createCondition("NOT_IN_OCEAN", (world, position) -> {
            BiomeBase biomeBase = world.getBiome(position);
            return biomeBase != Biomes.OCEAN && biomeBase != Biomes.DEEP_OCEAN;
        }, EntityType.DOLPHIN);

        createCondition("NOT_PEACEFUL",
                (world, position) -> world.getDifficulty() != EnumDifficulty.PEACEFUL,
                EntityType.DROWNED, EntityType.GUARDIAN, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.GHAST, EntityType.GIANT, EntityType.HUSK,
                EntityType.MAGMA_CUBE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON,
                EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER,
                EntityType.ILLUSIONER, EntityType.VEX, EntityType.VINDICATOR, EntityType.ELDER_GUARDIAN
        );

        createCondition("ON_GRASS",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.GRASS_BLOCK,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA,
                EntityType.MULE, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.WOLF,
                EntityType.ZOMBIE_HORSE
        );

        createCondition("ON_GRASS_OR_LEAVES", (world, position) -> {
            IBlockData blockData = world.getType(position.down());
            return blockData.getBlock() == Blocks.GRASS_BLOCK || blockData.a(TagsBlock.LEAVES);
        }, EntityType.OCELOT);

        createCondition("ON_GRASS_OR_SAND_OR_SNOW", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block == Blocks.GRASS_BLOCK || block == Blocks.SAND || block == Blocks.SNOW;
        }, EntityType.RABBIT);

        createCondition("ON_MYCELIUM",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.MYCELIUM,
                EntityType.MUSHROOM_COW
        );

        createCondition("ON_SAND",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.SAND,
                EntityType.TURTLE
        );

        createCondition("ON_TREE_OR_AIR", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block.a(TagsBlock.LEAVES) || block == Blocks.GRASS_BLOCK ||
                    block instanceof BlockLogAbstract || block == Blocks.AIR;
        }, EntityType.PARROT);
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
        private final List<MobSpawnerData> mobs = new ArrayList<>();
        private MobSpawnerData spawnData = new MobSpawnerData();
        public String failureReason = "";

        private int spawnedEntities = 0;
        private WStackedEntity demoEntity = null;

        StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackedSpawner stackedSpawner){
            this.world = tileEntityMobSpawner.getWorld();
            this.position = tileEntityMobSpawner.getPosition();
            this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

            MobSpawnerAbstract originalSpawner = tileEntityMobSpawner.getSpawner();
            MOB_SPAWNER_ABSTRACT.set(tileEntityMobSpawner, this);

            NBTTagCompound tagCompound = originalSpawner.b(new NBTTagCompound());

            if (tagCompound.hasKeyOfType("SpawnData", 10))
                this.a(new MobSpawnerData(1, tagCompound.getCompound("SpawnData")));

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

        @Override
        public void a(MobSpawnerData spawnData) {
            super.a(spawnData);
            this.spawnData = spawnData;
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

            if(demoNMSEntity.P() != EntityTypes.a(this.spawnData.b().getString("id"))){
                updateDemoEntity();

                if(demoEntity == null){
                    super.c();
                    return;
                }

                updateUpgrade(stackedSpawner.getUpgradeId());

                demoNMSEntity = ((CraftEntity) demoEntity.getLivingEntity()).getHandle();
            }

            int stackAmount = stackedSpawner.getStackAmount();

            List<? extends Entity> nearbyEntities = world.a(demoNMSEntity.getClass(), new AxisAlignedBB(
                    position.getX(), position.getY(), position.getZ(),
                    position.getX() + 1, position.getY() + 1, position.getZ() + 1
            ).g(this.spawnRange));

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

                switch (bukkitEntity.getType()){
                    case OCELOT:
                    case IRON_GOLEM: {
                        World world = nmsEntity.world;
                        hasSpace = !world.containsLiquid(nmsEntity.getBoundingBox()) &&
                                world.getCubes(nmsEntity, nmsEntity.getBoundingBox()) &&
                                world.a_(nmsEntity, nmsEntity.getBoundingBox());
                        break;
                    }
                    default:
                        hasSpace = !(nmsEntity instanceof EntityInsentient) || ((EntityInsentient) nmsEntity).canSpawn();
                        break;
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
                a(WeightedRandom.a(this.a().random, this.mobs));
            }

            spawnedEntities = 0;
            failureReason = "";

            a(1);
        }

        private org.bukkit.entity.Entity generateEntity(double x, double y, double z, boolean rotation){
            NBTTagCompound entityCompound = this.spawnData.b();
            Entity entity = ChunkRegionLoader.a(entityCompound, world, x, y, z, false);
            return entity == null ? null : entity.getBukkitEntity();
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

                if (this.spawnData.b().d() == 1 && this.spawnData.b().hasKeyOfType("id", 8)) {
                    ((EntityInsentient)entity).prepare(world.getDamageScaler(new BlockPosition(entity)), null, null);
                }

                if (entityinsentient.world.spigotConfig.nerfSpawnerMobs) {
                    entityinsentient.fromMobSpawner = true;
                }
            }

            if (CraftEventFactory.callSpawnerSpawnEvent(entity, position).isCancelled()) {
                Entity vehicle = entity.getVehicle();
                if (vehicle != null) {
                    vehicle.dead = true;
                }
                for(Entity passenger : entity.getAllPassengers())
                    passenger.dead = true;
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
                    ((EntityInsentient)entity).doSpawnEffect();
                }

                return true;
            }

            return false;
        }

        private boolean addEntity(Entity entity) {
            entity.valid = false;

            if (world.addEntity(entity, CreatureSpawnEvent.SpawnReason.SPAWNER)) {
                entity.bP().forEach(this::addEntity);
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
