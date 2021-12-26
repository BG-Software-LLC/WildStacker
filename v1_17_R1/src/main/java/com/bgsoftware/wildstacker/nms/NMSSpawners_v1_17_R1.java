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
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.levelgen.SeededRandom;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public final class NMSSpawners_v1_17_R1 implements NMSSpawners {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(TileEntityMobSpawner.class, MobSpawnerAbstract.class, "a").removeFinal();

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static void createCondition(String id, BiPredicate<World, BlockPosition> predicate, EntityType... entityTypes) {
        SpawnCondition spawnCondition = SpawnCondition.register(new SpawnCondition(id, EntityUtils.format(id)) {
            @Override
            public boolean test(Location location) {
                return predicate.test(((CraftWorld) location.getWorld()).getHandle(),
                        new BlockPosition(location.getX(), location.getY(), location.getZ()));
            }
        });
        plugin.getSystemManager().addSpawnCondition(spawnCondition, entityTypes);
    }

    @Override
    public boolean updateStackedSpawner(StackedSpawner stackedSpawner) {
        World world = ((CraftWorld) stackedSpawner.getWorld()).getHandle();
        Location location = stackedSpawner.getLocation();

        TileEntity tileEntity = world.getTileEntity(new BlockPosition(location.getX(), location.getY(), location.getZ()));
        if (tileEntity instanceof TileEntityMobSpawner && !(((TileEntityMobSpawner) tileEntity).getSpawner() instanceof StackedMobSpawner)) {
            new StackedMobSpawner((TileEntityMobSpawner) tileEntity, stackedSpawner);
            return true;
        }

        return false;
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ANIMAL_LIGHT",
                (world, position) -> world.getLightLevel(position, 0) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.GOAT, EntityType.HORSE,
                EntityType.LLAMA, EntityType.MUSHROOM_COW, EntityType.MULE, EntityType.PARROT, EntityType.PIG,
                EntityType.RABBIT, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.TURTLE, EntityType.WOLF,
                EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.FOX, EntityType.PANDA, EntityType.TRADER_LLAMA
        );

        createCondition("ANIMAL_LIGHT_AND_COLD", (world, position) -> {
            Optional<ResourceKey<BiomeBase>> biomeBase = world.j(position);
            boolean coldBiome = Objects.equals(biomeBase, Optional.of(Biomes.k)) ||
                    Objects.equals(biomeBase, Optional.of(Biomes.Y));
            IBlockData blockData = world.getType(position.down());
            return world.getLightLevel(position, 0) > 8 && (coldBiome ? blockData.a(Blocks.i) : blockData.a(Blocks.cL));
        }, EntityType.POLAR_BEAR);

        createCondition("COMPLETE_DARKNESS",
                (world, position) -> (world.Y() ? world.c(position, 10) : world.getLightLevel(position)) == 0,
                EntityType.AXOLOTL, EntityType.GLOW_SQUID
        );


        createCondition("ABOVE_NATURAL_STONE", (world, position) -> {
            BlockPosition.MutableBlockPosition mutableBlockPosition = position.i();

            for(int i = 0; i < 5; ++i) {
                mutableBlockPosition.c(EnumDirection.a);

                IBlockData blockData = world.getType(mutableBlockPosition);

                // Check for overworld stone
                if (blockData.a(TagsBlock.aQ)) {
                    return true;
                }

                // Check for non-water blocks
                if (!blockData.a(Blocks.A)) {
                    return false;
                }
            }

            return false;
        }, EntityType.AXOLOTL, EntityType.GLOW_SQUID);

        createCondition("BELOW_SEA_LEVEL",
                (world, position) -> position.getY() < world.getSeaLevel(),
                EntityType.DOLPHIN
        );

        createCondition("DARK_BLOCK_LIGHT",
                (world, position) -> world.getBrightness(EnumSkyBlock.b, position) <= 8,
                EntityType.PILLAGER
        );

        createCondition("IN_LAVA_AND_AIR_ABOVE", (world, position) -> {
            BlockPosition.MutableBlockPosition mutableBlockPosition = position.i();

            do {
                mutableBlockPosition.c(EnumDirection.b);
            } while (world.getFluid(mutableBlockPosition).a(TagsFluid.c));

            return world.getType(mutableBlockPosition).isAir();
        }, EntityType.STRIDER);

        createCondition("IN_SEA_SURFACE",
                (world, position) -> position.getY() < world.getSeaLevel() + 4,
                EntityType.TURTLE
        );

        createCondition("IN_SLIME_CHUNK_OR_SWAMP", (world, position) -> {
            if (Objects.equals(world.j(position), Optional.of(Biomes.g)))
                return true;

            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(position);
            boolean isSlimeChunk = SeededRandom.a(chunkcoordintpair.b, chunkcoordintpair.c, ((GeneratorAccessSeed) world).getSeed(),
                    world.getMinecraftWorld().spigotConfig.slimeSeed).nextInt(10) == 0;
            return isSlimeChunk && position.getY() < 40;
        }, EntityType.SLIME);

        createCondition("IN_WATER_DEEP",
                (world, position) -> world.getFluid(position).a(TagsFluid.b) && world.getFluid(position.up()).a(TagsFluid.b),
                EntityType.COD, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.DOLPHIN
        );

        createCondition("MONSTER_LIGHT",
                (world, position) -> EntityMonster.a((WorldServer) world, position, world.getRandom()),
                EntityType.DROWNED, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.GIANT, EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX,
                EntityType.VINDICATOR
        );

        createCondition("NOT_IN_OCEAN", (world, position) -> {
            ResourceKey<BiomeBase> biomeBase = world.j(position).orElse(null);
            return biomeBase == null || (!biomeBase.equals(Biomes.a) && !biomeBase.equals(Biomes.y));
        }, EntityType.DOLPHIN);

        createCondition("IN_RIVER", (world, position) -> {
            Optional<ResourceKey<BiomeBase>> biomeBase = world.j(position);
            return Objects.equals(biomeBase, Optional.of(Biomes.h)) || Objects.equals(biomeBase, Optional.of(Biomes.l));
        }, EntityType.DOLPHIN);

        createCondition("NOT_IN_OCEAN_DEEP",
                (world, position) -> position.getY() > 45,
                EntityType.DOLPHIN
        );

        createCondition("NOT_ON_NETHER_WART_BLOCK",
                (world, position) -> !world.getType(position.down()).a(Blocks.iY),
                EntityType.ZOMBIFIED_PIGLIN
        );

        createCondition("NOT_PEACEFUL",
                (world, position) -> world.getDifficulty() != EnumDifficulty.a,
                EntityType.DROWNED, EntityType.GUARDIAN, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.GHAST, EntityType.GIANT, EntityType.HUSK,
                EntityType.MAGMA_CUBE, EntityType.PILLAGER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON,
                EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER,
                EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX, EntityType.VINDICATOR, EntityType.ELDER_GUARDIAN
        );

        createCondition("ON_GRASS",
                (world, position) -> world.getType(position.down()).a(Blocks.i),
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.GOAT, EntityType.HORSE,
                EntityType.LLAMA, EntityType.MULE, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON_HORSE,
                EntityType.WOLF, EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.FOX, EntityType.PANDA,
                EntityType.TRADER_LLAMA
        );

        createCondition("ON_GRASS_OR_SAND_OR_SNOW", (world, position) -> {
            IBlockData blockData = world.getType(position.down());
            return blockData.a(Blocks.i) || blockData.a(Blocks.cK) || blockData.a(Blocks.C);
        }, EntityType.RABBIT);

        createCondition("ON_MYCELIUM",
                (world, position) -> world.getType(position.down()).a(Blocks.ec),
                EntityType.MUSHROOM_COW
        );

        createCondition("ON_NETHER_WART_BLOCK",
                (world, position) -> world.getType(position.down()).a(Blocks.iY),
                EntityType.HOGLIN, EntityType.PIGLIN
        );

        createCondition("ON_SAND",
                (world, position) -> world.getType(position.down()).a(Blocks.C),
                EntityType.TURTLE
        );

        createCondition("ON_TREE_OR_AIR", (world, position) -> {
            IBlockData blockData = world.getType(position.down());
            return blockData.a(TagsBlock.I) || blockData.a(Blocks.i) ||
                    blockData.a(TagsBlock.s) || blockData.a(Blocks.a);
        }, EntityType.PARROT);
    }

    static class StackedMobSpawner extends MobSpawnerAbstract {

        private final WeakReference<WStackedSpawner> stackedSpawner;
        public String failureReason = "";

        private int spawnedEntities = 0;
        private WStackedEntity demoEntity = null;

        StackedMobSpawner(TileEntityMobSpawner tileEntityMobSpawner, StackedSpawner stackedSpawner) {
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

                spawnedEntities += increaseStackAmount;

                targetEntity.increaseStackAmount(increaseStackAmount, true);
                demoEntity.spawnStackParticle(true);

                if (plugin.getSettings().linkedEntitiesEnabled && targetEntity.getLivingEntity() != stackedSpawner.getLinkedEntity())
                    stackedSpawner.setLinkedEntity(targetEntity.getLivingEntity());

                world.triggerEffect(2004, position, 0);
                particlesAmount++;
            } else {
                mobsToSpawn = spawnCount;
            }

            if (mobsToSpawn > 0 && demoEntity.isCached() && spawnStacked) {
                amountPerEntity = Math.min(mobsToSpawn, demoEntity.getStackLimit());
                mobsToSpawn = mobsToSpawn / amountPerEntity;
            }

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
                    return;
                }

                int amountToSpawn = spawnedEntities + amountPerEntity > spawnCount ? spawnCount - spawnedEntities : amountPerEntity;

                if (handleEntitySpawn(world, position, bukkitEntity, stackedSpawner, amountToSpawn, particlesAmount <= this.k)) {
                    spawnedEntities += amountPerEntity;
                    particlesAmount++;
                }
            }

            if (spawnedEntities >= stackAmount)
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
                _entity.setPositionRotation(x, y, z, rotation ? world.getRandom().nextFloat() * 360.0F:  0f, 0f);

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
                    ((EntityInsentient) entity).prepare(world, world.getDamageScaler(entity.getChunkCoordinates()),
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

                if (entity instanceof EntityInsentient) {
                    ((EntityInsentient) entity).doSpawnEffect();
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

}
