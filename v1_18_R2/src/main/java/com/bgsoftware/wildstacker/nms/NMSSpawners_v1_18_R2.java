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
import net.minecraft.core.Holder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
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
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

import static com.bgsoftware.wildstacker.nms.NMSMappings_v1_18_R2.*;

@SuppressWarnings("unused")
public final class NMSSpawners_v1_18_R2 implements NMSSpawners {

    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(TileEntityMobSpawner.class,
            MobSpawnerAbstract.class, "a").removeFinal();
    private static final ReflectField<LevelCallback<Entity>> WORLD_LEVEL_CALLBACK = new ReflectField<>(PersistentEntitySectionManager.class,
            LevelCallback.class, "c");


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

        TileEntity tileEntity = getTileEntity(world, new BlockPosition(location.getX(), location.getY(), location.getZ()));
        if (tileEntity instanceof TileEntityMobSpawner tileEntityMobSpawner &&
                !(getSpawner(tileEntityMobSpawner) instanceof StackedMobSpawner)) {
            new StackedMobSpawner(tileEntityMobSpawner, stackedSpawner);
            return true;
        }

        return false;
    }

    private static IBlockData getBlockBelow(World world, BlockPosition position) {
        return getType(world, position.c());
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ANIMAL_LIGHT",
                (world, position) -> getLightLevel(world, position, 0) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.GOAT, EntityType.HORSE,
                EntityType.LLAMA, EntityType.MUSHROOM_COW, EntityType.MULE, EntityType.PARROT, EntityType.PIG,
                EntityType.RABBIT, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.TURTLE, EntityType.WOLF,
                EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.FOX, EntityType.PANDA, EntityType.TRADER_LLAMA
        );

        createCondition("ANIMAL_LIGHT_AND_COLD", (world, position) -> {
            Holder<BiomeBase> biomeBaseHolder = world.v(position);
            boolean coldBiome = biomeBaseHolder.a(Biomes.U) || biomeBaseHolder.a(Biomes.V);
            IBlockData blockData = getBlockBelow(world, position);
            return getLightLevel(world, position, 0) > 8 && (coldBiome ? blockData.a(Blocks.i) : blockData.a(Blocks.cL));
        }, EntityType.POLAR_BEAR);

        createCondition("ON_AXOLOTL_SPAWNABLE",
                (world, position) -> isTagged(TagsBlock.bt, getBlock(getType(world, position.c()))),
                EntityType.AXOLOTL
        );

        createCondition("ON_FOX_SPAWNABLE",
                (world, position) -> isTagged(TagsBlock.bz, getBlock(getType(world, position.c()))),
                EntityType.FOX
        );

        createCondition("ON_GOAT_SPAWNABLE",
                (world, position) -> isTagged(TagsBlock.bu, getBlock(getType(world, position.c()))),
                EntityType.GOAT
        );

        createCondition("ON_RABBITS_SPAWNABLE",
                (world, position) -> isTagged(TagsBlock.by, getBlock(getType(world, position.c()))),
                EntityType.RABBIT
        );

        createCondition("BELOW_SEA_LEVEL",
                (world, position) -> getY(position) < getSeaLevel(world),
                EntityType.DOLPHIN
        );

        createCondition("DARK_BLOCK_LIGHT",
                (world, position) -> getBrightness(world, EnumSkyBlock.b, position) <= 8,
                EntityType.PILLAGER
        );

        createCondition("IN_LAVA_AND_AIR_ABOVE", (world, position) -> {
            BlockPosition.MutableBlockPosition mutableBlockPosition = position.i();

            do {
                mutableBlockPosition.c(EnumDirection.b);
            } while (getFluid(world, mutableBlockPosition).a(TagsFluid.b));

            return getType(world, mutableBlockPosition).g(); //isAir
        }, EntityType.STRIDER);

        createCondition("IN_SEA_SURFACE",
                (world, position) -> getY(position) < getSeaLevel(world) + 4,
                EntityType.TURTLE
        );

        createCondition("IN_SLIME_CHUNK_OR_SWAMP", (world, position) -> {
            Holder<BiomeBase> biomeBaseHolder = world.v(position);

            if (biomeBaseHolder.a(Biomes.g))
                return true;

            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(position);
            boolean isSlimeChunk = SeededRandom.a(chunkcoordintpair.c, chunkcoordintpair.d, ((GeneratorAccessSeed) world).D(),
                    world.getMinecraftWorld().spigotConfig.slimeSeed).nextInt(10) == 0;
            return isSlimeChunk && getY(position) < 40;
        }, EntityType.SLIME);

        createCondition("IN_FISH_WATER",
                (world, position) -> getFluid(world, position.c()).a(TagsFluid.b) && getType(world, position.b()).a(Blocks.A) &&
                        position.v() >= getSeaLevel(world) - 13 && position.v() <= getSeaLevel(world),
                EntityType.COD, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.DOLPHIN
        );

        createCondition("IN_FISH_WATER_OR_LUSH_CAVES",
                (world, position) -> getFluid(world, position.c()).a(TagsFluid.b) &&
                        (world.v(position).a(Biomes.Y) ||
                                (getType(world, position.b()).a(Blocks.A) && position.v() >= getSeaLevel(world) - 13 && position.v() <= getSeaLevel(world))),
                EntityType.TROPICAL_FISH
        );

        createCondition("COMPLETE_DARKNESS",
                (world, position) -> (world.Y() ? world.c(position, 10) : getLightLevel(world, position)) == 0,
                EntityType.AXOLOTL, EntityType.DROWNED, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.GIANT, EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX,
                EntityType.VINDICATOR
        );

        createCondition("NOT_IN_OCEAN", (world, position) -> {
            Holder<BiomeBase> biomeBaseHolder = world.v(position);
            return !biomeBaseHolder.a(Biomes.Q) && !biomeBaseHolder.a(Biomes.R);
        }, EntityType.DOLPHIN);

        createCondition("IN_RIVER", (world, position) -> {
            Holder<BiomeBase> biomeBaseHolder = world.v(position);
            return biomeBaseHolder.a(Biomes.I) || biomeBaseHolder.a(Biomes.J);
        }, EntityType.DOLPHIN);

        createCondition("NOT_IN_OCEAN_DEEP",
                (world, position) -> getY(position) > 45,
                EntityType.DOLPHIN
        );

        createCondition("IN_OCEAN_DEEP",
                (world, position) -> getType(world, position).a(Blocks.A) && getY(position) <= 30,
                EntityType.GLOW_SQUID
        );

        createCondition("NOT_ON_NETHER_WART_BLOCK",
                (world, position) -> !getBlockBelow(world, position).a(Blocks.iY),
                EntityType.ZOMBIFIED_PIGLIN
        );

        createCondition("NOT_PEACEFUL",
                (world, position) -> getDifficulty(world) != EnumDifficulty.a,
                EntityType.DROWNED, EntityType.GUARDIAN, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.GHAST, EntityType.GIANT, EntityType.HUSK,
                EntityType.MAGMA_CUBE, EntityType.PILLAGER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON,
                EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER,
                EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX, EntityType.VINDICATOR, EntityType.ELDER_GUARDIAN
        );

        createCondition("ON_GRASS",
                (world, position) -> getBlockBelow(world, position).a(Blocks.i),
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.GOAT, EntityType.HORSE,
                EntityType.LLAMA, EntityType.MULE, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON_HORSE,
                EntityType.WOLF, EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.PANDA, EntityType.TRADER_LLAMA
        );

        createCondition("ON_MYCELIUM",
                (world, position) -> getBlockBelow(world, position).a(Blocks.ec),
                EntityType.MUSHROOM_COW
        );

        createCondition("ON_NETHER_WART_BLOCK",
                (world, position) -> getBlockBelow(world, position).a(Blocks.iY),
                EntityType.HOGLIN, EntityType.PIGLIN
        );

        createCondition("ON_SAND",
                (world, position) -> getBlockBelow(world, position).a(Blocks.C),
                EntityType.TURTLE
        );

        createCondition("ON_TREE_OR_AIR", (world, position) -> {
            IBlockData blockData = getBlockBelow(world, position);
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

            MobSpawnerAbstract originalSpawner = getSpawner(tileEntityMobSpawner);
            MOB_SPAWNER_ABSTRACT.set(tileEntityMobSpawner, this);

            this.e = originalSpawner.e;
            this.h = originalSpawner.h;
            this.i = originalSpawner.i;
            this.j = originalSpawner.j;
            this.l = originalSpawner.l;
            this.m = originalSpawner.m;
            this.n = originalSpawner.n;

            updateDemoEntity((WorldServer) getWorld(tileEntityMobSpawner), getPosition(tileEntityMobSpawner));
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

            if (this.c <= -1)
                resetSpawnDelay(world, position);

            if (this.c > 0) {
                --this.c;
                return;
            }

            Optional<EntityTypes<?>> entityTypesOptional = EntityTypes.a(getEntity(this.e));

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

            if (getEntityType(demoNMSEntity) != entityTypes) {
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
                    getX(position), getY(position), getZ(position),
                    getX(position) + 1, getY(position) + 1, getZ(position) + 1
            ).g(this.n));

            StackedEntity targetEntity = getTargetEntity(stackedSpawner, demoEntity, nearbyEntities);

            if (targetEntity == null && nearbyEntities.size() >= this.l) {
                failureReason = "There are too many nearby entities.";
                return;
            }

            boolean spawnStacked = EventsCaller.callSpawnerStackedEntitySpawnEvent(stackedSpawner.getSpawner());
            failureReason = "";

            int spawnCount = !spawnStacked || !demoEntity.isCached() ? Random.nextInt(1, this.j, stackAmount) :
                    Random.nextInt(1, this.j, stackAmount, 1.5);

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

                    triggerEffect(world, 2004, position, 0);
                    particlesAmount++;
                }
            } else {
                mobsToSpawn = spawnCount;
            }

            if (mobsToSpawn > 0 && demoEntity.isCached() && spawnStacked) {
                amountPerEntity = Math.min(mobsToSpawn, demoEntity.getStackLimit());
                mobsToSpawn = mobsToSpawn / amountPerEntity;
            }

            for (int i = 0; i < mobsToSpawn; i++) {
                double x = getX(position) + (getRandom(world).nextDouble() - getRandom(world).nextDouble()) * this.n + 0.5D;
                double y = getY(position) + getRandom(world).nextInt(3) - 1;
                double z = getZ(position) + (getRandom(world).nextDouble() - getRandom(world).nextDouble()) * this.n + 0.5D;

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

                if (handleEntitySpawn(world, position, bukkitEntity, stackedSpawner, amountToSpawn, particlesAmount <= this.j)) {
                    spawnedEntities += amountPerEntity;
                    particlesAmount++;
                }
            }

            if (spawnedEntities >= stackAmount)
                resetSpawnDelay(world, position);
        }

        @Override
        public void a(World world, BlockPosition position, int i) {
            world.a(position, Blocks.bV, i, 0);
        }

        public void updateUpgrade(int upgradeId) {
            if (demoEntity != null)
                demoEntity.setUpgradeId(upgradeId);
        }

        private boolean hasNearbyPlayers(World world, BlockPosition position) {
            return world.a(getX(position) + 0.5D, getY(position) + 0.5D, getZ(position) + 0.5D, this.m);
        }

        private void resetSpawnDelay(World world, BlockPosition position) {
            if (this.i <= this.h) {
                this.c = this.h;
            } else {
                this.c = this.h + getRandom(world).nextInt(this.i - this.h);
            }

            // Set mob spawn data
            this.d.b(getRandom(world)).ifPresent(weightedEntry -> this.a(world, position, weightedEntry.b()));

            spawnedEntities = 0;
            failureReason = "";

            a(world, position, 1);
        }

        private org.bukkit.entity.Entity generateEntity(World world, double x, double y, double z, boolean rotation) {
            NBTTagCompound entityCompound = getEntity(this.e);
            Entity entity = EntityTypes.a(entityCompound, world, _entity -> {
                setPositionRotation(_entity, x, y, z, rotation ? getRandom(world).nextFloat() * 360.0F : 0f, 0f);

                _entity.s = world;
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
                if (getEntity(this.e).e() == 1 && hasKeyOfType(getEntity(this.e), "id", 8)) {
                    prepare(entityInsentient, world, getDamageScaler(world, getChunkCoordinates(entity)),
                            EnumMobSpawn.c, null, null);
                }

                if (getWorld(entityInsentient).spigotConfig.nerfSpawnerMobs) {
                    entityInsentient.aware = false;
                }
            }

            if (CraftEventFactory.callSpawnerSpawnEvent(entity, position).isCancelled()) {
                Entity vehicle = getVehicle(entity);
                if (vehicle != null) {
                    die(vehicle);
                }

                for (Entity passenger : getAllPassengers(entity))
                    die(passenger);

                if (stackedEntity != null)
                    plugin.getSystemManager().removeStackObject(stackedEntity);
                EntityStorage.clearMetadata(bukkitEntity);
            } else {
                if (!addEntity(world, entity)) {
                    EntityStorage.clearMetadata(bukkitEntity);
                    return false;
                }

                if (spawnParticles)
                    triggerEffect(world, 2004, position, 0);

                if (entity instanceof EntityInsentient entityInsentient) {
                    doSpawnEffect(entityInsentient);
                }

                return true;
            }

            return false;
        }

        private boolean addEntity(World world, Entity entity) {
            entity.valid = false;

            if (NMSMappings_v1_18_R2.addEntity(world, entity, CreatureSpawnEvent.SpawnReason.SPAWNER)) {
                getPassengers(entity).forEach(passenger -> addEntity(world, passenger));
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

        private void updateDemoEntity(WorldServer world, BlockPosition position) {
            org.bukkit.entity.Entity demoEntityBukkit = generateEntity(world, getX(position), getY(position),
                    getZ(position), false);

            if (demoEntityBukkit != null)
                WORLD_LEVEL_CALLBACK.get(world.O).a(((CraftEntity) demoEntityBukkit).getHandle());

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
