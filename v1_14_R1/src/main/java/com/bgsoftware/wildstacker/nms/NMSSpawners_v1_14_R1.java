package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.WildStackerPlugin;
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
import com.bgsoftware.wildstacker.utils.reflection.Fields;
import net.minecraft.server.v1_14_R1.AxisAlignedBB;
import net.minecraft.server.v1_14_R1.BiomeBase;
import net.minecraft.server.v1_14_R1.Biomes;
import net.minecraft.server.v1_14_R1.Block;
import net.minecraft.server.v1_14_R1.BlockLogAbstract;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityInsentient;
import net.minecraft.server.v1_14_R1.EntityMonster;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumDifficulty;
import net.minecraft.server.v1_14_R1.EnumMobSpawn;
import net.minecraft.server.v1_14_R1.EnumSkyBlock;
import net.minecraft.server.v1_14_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.SeededRandom;
import net.minecraft.server.v1_14_R1.TagsBlock;
import net.minecraft.server.v1_14_R1.TagsFluid;
import net.minecraft.server.v1_14_R1.TileEntity;
import net.minecraft.server.v1_14_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_14_R1.WeightedRandom;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public final class NMSSpawners_v1_14_R1 implements NMSSpawners {

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    @Override
    public void updateStackedSpawner(StackedSpawner stackedSpawner) {
        World world = ((CraftWorld) stackedSpawner.getWorld()).getHandle();
        Location location = stackedSpawner.getLocation();

        TileEntity tileEntity = world.getTileEntity(new BlockPosition(location.getX(), location.getY(), location.getZ()));
        if(tileEntity instanceof TileEntityMobSpawner)
            new StackedMobSpawner((TileEntityMobSpawner) tileEntity, stackedSpawner);
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ANIMAL_LIGHT",
                (world, position) -> world.getLightLevel(position, 0) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA,
                EntityType.MUSHROOM_COW, EntityType.MULE, EntityType.PARROT, EntityType.PIG, EntityType.RABBIT,
                EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.TURTLE, EntityType.WOLF, EntityType.ZOMBIE_HORSE,
                EntityType.CAT, EntityType.FOX, EntityType.PANDA, EntityType.TRADER_LLAMA
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

        createCondition("DARK_BLOCK_LIGHT",
                (world, position) -> world.getBrightness(EnumSkyBlock.BLOCK, position) <= 8,
                EntityType.PILLAGER
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
                EntityType.COD, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.TROPICAL_FISH
        );

        createCondition("MONSTER_LIGHT",
                (world, position) -> EntityMonster.a(world, position, world.random),
                EntityType.DROWNED, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.GIANT, EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX,
                EntityType.VINDICATOR
        );

        createCondition("NOT_IN_OCEAN", (world, position) -> {
            BiomeBase biomeBase = world.getBiome(position);
            return biomeBase != Biomes.OCEAN && biomeBase != Biomes.DEEP_OCEAN;
        }, EntityType.DOLPHIN);

        createCondition("NOT_PEACEFUL",
                (world, position) -> world.getDifficulty() != EnumDifficulty.PEACEFUL,
                EntityType.DROWNED, EntityType.GUARDIAN, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.GHAST, EntityType.GIANT, EntityType.HUSK,
                EntityType.MAGMA_CUBE, EntityType.PILLAGER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON,
                EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER,
                EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX, EntityType.VINDICATOR, EntityType.ELDER_GUARDIAN
        );

        createCondition("ON_GRASS",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.GRASS_BLOCK,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA,
                EntityType.MULE, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.WOLF,
                EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.FOX, EntityType.PANDA, EntityType.TRADER_LLAMA
        );

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

    @SuppressWarnings("ConstantConditions")
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
        public String failureReason = "";

        private int spawnedEntities = 0;

        StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackedSpawner stackedSpawner){
            this.world = tileEntityMobSpawner.getWorld();
            this.position = tileEntityMobSpawner.getPosition();
            this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

            if(!(tileEntityMobSpawner.getSpawner() instanceof StackedMobSpawner)) {
                MobSpawnerAbstract originalSpawner = tileEntityMobSpawner.getSpawner();
                Fields.TILE_ENTITY_SPAWNER_ABSTRACT_SPAWNER.set(tileEntityMobSpawner, this);

                this.spawnData = originalSpawner.spawnData;
                this.minSpawnDelay = originalSpawner.minSpawnDelay;
                this.maxSpawnDelay = originalSpawner.maxSpawnDelay;
                this.spawnCount = originalSpawner.spawnCount;
                this.maxNearbyEntities = originalSpawner.maxNearbyEntities;
                this.requiredPlayerRange = originalSpawner.requiredPlayerRange;
                this.spawnRange = originalSpawner.spawnRange;
            }
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

            Optional<EntityTypes<?>> entityTypesOptional = EntityTypes.a(this.spawnData.getEntity());

            if(!entityTypesOptional.isPresent()){
                resetSpawnDelay();
                return;
            }

            EntityTypes<?> entityTypes = entityTypesOptional.get();

            org.bukkit.entity.Entity demoEntityBukkit = generateEntity(position.getX(), position.getY(), position.getZ(), false);

            if(demoEntityBukkit == null){
                resetSpawnDelay();
                return;
            }

            if(!EntityUtils.isStackable(demoEntityBukkit)){
                super.c();
                return;
            }

            StackedEntity demoEntity = WStackedEntity.of(demoEntityBukkit);
            demoEntity.setSpawnCause(SpawnCause.SPAWNER);
            ((WStackedEntity) demoEntity).setUpgradeId(stackedSpawner.getUpgradeId());
            ((WStackedEntity) demoEntity).setDemoEntity();

            Entity demoNMSEntity = ((CraftEntity) demoEntityBukkit).getHandle();
            ((WorldServer) world).unregisterEntity(demoNMSEntity);

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

                Location location = new Location(world.getWorld(), x, y, z);

                boolean hasSpace = world.c(entityTypes.a(x, y, z));

                if(!hasSpace){
                    failureReason = "Not enough space to spawn the entity.";
                    continue;
                }

                SpawnCondition failedCondition = plugin.getSystemManager().getSpawnConditions(demoEntityBukkit.getType())
                        .stream().filter(spawnCondition -> !spawnCondition.test(location)).findFirst().orElse(null);

                if(failedCondition != null) {
                    failureReason = "Cannot spawn entities due to " + failedCondition.getName() + " restriction.";
                    continue;
                }

                org.bukkit.entity.Entity bukkitEntity = generateEntity(x, y, z, true);

                if (bukkitEntity == null) {
                    resetSpawnDelay();
                    return;
                }

                if(handleEntitySpawn(bukkitEntity, stackedSpawner, amountPerEntity, particlesAmount <= this.spawnCount)) {
                    spawnedEntities += amountPerEntity;
                    particlesAmount++;
                }
            }

            if(spawnedEntities >= stackAmount)
                resetSpawnDelay();
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
                setSpawnData(WeightedRandom.a(this.a().random, this.mobs));
            }

            spawnedEntities = 0;

            a(1);
        }

        private org.bukkit.entity.Entity generateEntity(double x, double y, double z, boolean rotation){
            NBTTagCompound entityCompound = this.spawnData.getEntity();
            Entity entity = EntityTypes.a(entityCompound, world, _entity -> {
                _entity.setPositionRotation(x, y, z, 0f, 0f);

                if(rotation)
                    _entity.yaw = world.random.nextFloat() * 360.0F;

                _entity.world = world;
                _entity.valid = true;
                _entity.dead = false;

                return _entity;
            });
            return entity == null ? null : entity.getBukkitEntity();
        }

        private boolean handleEntitySpawn(org.bukkit.entity.Entity bukkitEntity, WStackedSpawner stackedSpawner, int amountPerEntity, boolean spawnParticles){
            Entity entity = ((CraftEntity) bukkitEntity).getHandle();
            StackedEntity stackedEntity = null;

            EntityStorage.setMetadata(bukkitEntity, "spawn-cause", SpawnCause.SPAWNER);

            if(amountPerEntity > 1) {
                stackedEntity = WStackedEntity.of(bukkitEntity);
                ((WStackedEntity) stackedEntity).setUpgradeId(stackedSpawner.getUpgradeId());
                stackedEntity.setStackAmount(amountPerEntity, true);
            }

            if (entity instanceof EntityInsentient) {
                EntityInsentient entityinsentient = (EntityInsentient)entity;

                if (this.spawnData.getEntity().d() == 1 && this.spawnData.getEntity().hasKeyOfType("id", 8)) {
                    ((EntityInsentient)entity).prepare(world, world.getDamageScaler(new BlockPosition(entity)), EnumMobSpawn.SPAWNER, null, null);
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
            }

            else {
                addEntity(entity);

                if(spawnParticles)
                    world.triggerEffect(2004, position, 0);

                if (entity instanceof EntityInsentient) {
                    ((EntityInsentient)entity).doSpawnEffect();
                }

                return true;
            }

            return false;
        }

        private void addEntity(Entity entity) {
            ((WorldServer) world).unregisterEntity(entity);
            if (world.addEntity(entity, CreatureSpawnEvent.SpawnReason.SPAWNER))
                entity.getPassengers().forEach(this::addEntity);
        }

        private StackedEntity getTargetEntity(StackedSpawner stackedSpawner, StackedEntity demoEntity,
                                              List<? extends Entity> nearbyEntities){
            LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

            if(linkedEntity != null)
                return WStackedEntity.of(linkedEntity);

            Optional<CraftEntity> closestEntity = GeneralUtils.getClosestBukkit(stackedSpawner.getLocation(),
                    nearbyEntities.stream().map(Entity::getBukkitEntity).filter(entity ->
                            EntityUtils.isStackable(entity) &&
                                    demoEntity.runStackCheck(WStackedEntity.of(entity)) == StackCheckResult.SUCCESS));

            return closestEntity.map(WStackedEntity::of).orElse(null);
        }

    }

}
