package com.bgsoftware.wildstacker.nms.v1_18_R2;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.core.BlockPosition;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.server.level.MobSpawnerData;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.entity.Entity;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.level.ChunkCoordIntPair;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.level.World;
import com.bgsoftware.wildstacker.nms.v1_18_R2.mappings.net.minecraft.world.level.block.entity.TileEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.Random;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

@SuppressWarnings("unused")
public final class NMSSpawners implements com.bgsoftware.wildstacker.nms.NMSSpawners {

    @Remap(classPath = "net.minecraft.world.level.block.entity.SpawnerBlockEntity", name = "spawner", type = Remap.Type.FIELD)
    private static final ReflectField<MobSpawnerAbstract> MOB_SPAWNER_ABSTRACT = new ReflectField<MobSpawnerAbstract>(TileEntityMobSpawner.class,
            MobSpawnerAbstract.class, "a").removeFinal();
    @Remap(classPath = "net.minecraft.world.level.entity.PersistentEntitySectionManager", name = "callbacks", type = Remap.Type.FIELD)
    private static final ReflectField<LevelCallback<net.minecraft.world.entity.Entity>> WORLD_LEVEL_CALLBACK =
            new ReflectField<>(PersistentEntitySectionManager.class, LevelCallback.class, "c");

    @Remap(classPath = "net.minecraft.world.level.biome.Biomes", name = "FROZEN_OCEAN", type = Remap.Type.FIELD, remappedName = "U")
    private static final ResourceKey<BiomeBase> FROZEN_OCEAN_BIOME = Biomes.U;
    @Remap(classPath = "net.minecraft.world.level.biome.Biomes", name = "SWAMP", type = Remap.Type.FIELD, remappedName = "g")
    private static final ResourceKey<BiomeBase> SWAMP_BIOME = Biomes.g;
    @Remap(classPath = "net.minecraft.world.level.biome.Biomes", name = "DEEP_FROZEN_OCEAN", type = Remap.Type.FIELD, remappedName = "V")
    private static final ResourceKey<BiomeBase> DEEP_FROZEN_OCEAN_BIOME = Biomes.V;
    @Remap(classPath = "net.minecraft.world.level.biome.Biomes", name = "LUSH_CAVES", type = Remap.Type.FIELD, remappedName = "Y")
    private static final ResourceKey<BiomeBase> LUSH_CAVES_BIOME = Biomes.Y;
    @Remap(classPath = "net.minecraft.world.level.biome.Biomes", name = "OCEAN", type = Remap.Type.FIELD, remappedName = "Q")
    private static final ResourceKey<BiomeBase> OCEAN_BIOME = Biomes.Q;
    @Remap(classPath = "net.minecraft.world.level.biome.Biomes", name = "DEEP_OCEAN", type = Remap.Type.FIELD, remappedName = "R")
    private static final ResourceKey<BiomeBase> DEEP_OCEAN_BIOME = Biomes.R;
    @Remap(classPath = "net.minecraft.world.level.biome.Biomes", name = "RIVER", type = Remap.Type.FIELD, remappedName = "I")
    private static final ResourceKey<BiomeBase> RIVER_BIOME = Biomes.I;
    @Remap(classPath = "net.minecraft.world.level.biome.Biomes", name = "FROZEN_RIVER", type = Remap.Type.FIELD, remappedName = "J")
    private static final ResourceKey<BiomeBase> FROZEN_RIVER_BIOME = Biomes.J;
    @Remap(classPath = "net.minecraft.world.level.block.Blocks", name = "GRASS_BLOCK", type = Remap.Type.FIELD, remappedName = "i")
    private static final Block GRASS_BLOCK_BLOCK = Blocks.i;
    @Remap(classPath = "net.minecraft.world.level.block.Blocks", name = "ICE", type = Remap.Type.FIELD, remappedName = "cL")
    private static final Block ICE_BLOCK = Blocks.cL;
    @Remap(classPath = "net.minecraft.world.level.block.Blocks", name = "WATER", type = Remap.Type.FIELD, remappedName = "A")
    private static final Block WATER_BLOCK = Blocks.A;
    @Remap(classPath = "net.minecraft.world.level.block.Blocks", name = "NETHER_WART_BLOCK", type = Remap.Type.FIELD, remappedName = "iY")
    private static final Block NETHER_WART_BLOCK_BLOCK = Blocks.iY;
    @Remap(classPath = "net.minecraft.world.level.block.Blocks", name = "MYCELIUM", type = Remap.Type.FIELD, remappedName = "ec")
    private static final Block MYCELIUM_BLOCK = Blocks.ec;
    @Remap(classPath = "net.minecraft.world.level.block.Blocks", name = "SAND", type = Remap.Type.FIELD, remappedName = "C")
    private static final Block SAND_BLOCK = Blocks.C;
    @Remap(classPath = "net.minecraft.world.level.block.Blocks", name = "SPAWNER", type = Remap.Type.FIELD, remappedName = "bV")
    private static final Block SPAWNER_BLOCK = Blocks.bV;
    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "AXOLOTLS_SPAWNABLE_ON", type = Remap.Type.FIELD, remappedName = "bt")
    private static final TagKey<Block> AXOLOTLS_SPAWNABLE_ON_TAG = TagsBlock.bt;
    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "FOXES_SPAWNABLE_ON", type = Remap.Type.FIELD, remappedName = "bz")
    private static final TagKey<Block> FOXES_SPAWNABLE_ON_TAG = TagsBlock.bz;
    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "GOATS_SPAWNABLE_ON", type = Remap.Type.FIELD, remappedName = "bu")
    private static final TagKey<Block> GOATS_SPAWNABLE_ON_TAG = TagsBlock.bu;
    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "RABBITS_SPAWNABLE_ON", type = Remap.Type.FIELD, remappedName = "by")
    private static final TagKey<Block> RABBITS_SPAWNABLE_ON_TAG = TagsBlock.by;
    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "LEAVES", type = Remap.Type.FIELD, remappedName = "H")
    private static final TagKey<Block> LEAVES_TAG = TagsBlock.H;
    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "LOGS", type = Remap.Type.FIELD, remappedName = "r")
    private static final TagKey<Block> LOGS_TAG = TagsBlock.r;
    @Remap(classPath = "net.minecraft.world.entity.MobSpawnType", name = "SPAWNER", type = Remap.Type.FIELD, remappedName = "c")
    private static final EnumMobSpawn SPAWNER_MOB_SPAWN = EnumMobSpawn.c;

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static void createCondition(String id, BiPredicate<World, BlockPosition> predicate, EntityType... entityTypes) {
        SpawnCondition spawnCondition = SpawnCondition.register(new SpawnCondition(id, EntityUtils.format(id)) {
            @Override
            public boolean test(Location location) {
                return predicate.test(new World(((CraftWorld) location.getWorld()).getHandle()),
                        new BlockPosition(location.getX(), location.getY(), location.getZ()));
            }
        });
        plugin.getSystemManager().addSpawnCondition(spawnCondition, entityTypes);
    }

    @Override
    public boolean updateStackedSpawner(StackedSpawner stackedSpawner) {
        World world = new World(((CraftWorld) stackedSpawner.getWorld()).getHandle());
        Location location = stackedSpawner.getLocation();

        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntity tileEntity = world.getBlockEntity(blockPosition.getHandle());
        if (tileEntity.getHandle() instanceof TileEntityMobSpawner &&
                !(tileEntity.getSpawner().getHandle() instanceof StackedMobSpawner)) {
            new StackedMobSpawner(tileEntity, stackedSpawner);
            return true;
        }

        return false;
    }

    private static IBlockData getBlockBelow(World world, BlockPosition position) {
        return world.getBlockStateNoMappings(position.below());
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ANIMAL_LIGHT",
                (world, position) -> world.getRawBrightness(position.getHandle(), 0) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.GOAT, EntityType.HORSE,
                EntityType.LLAMA, EntityType.MUSHROOM_COW, EntityType.MULE, EntityType.PARROT, EntityType.PIG,
                EntityType.RABBIT, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.TURTLE, EntityType.WOLF,
                EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.FOX, EntityType.PANDA, EntityType.TRADER_LLAMA
        );

        createCondition("ANIMAL_LIGHT_AND_COLD", (world, position) -> {
            Holder<BiomeBase> biomeBaseHolder = world.getBiome(position.getHandle());
            boolean coldBiome = biomeBaseHolder.a(FROZEN_OCEAN_BIOME) || biomeBaseHolder.a(DEEP_FROZEN_OCEAN_BIOME);
            IBlockData blockData = getBlockBelow(world, position);
            return world.getRawBrightness(position.getHandle(), 0) > 8 &&
                    (coldBiome ? blockData.a(GRASS_BLOCK_BLOCK) : blockData.a(ICE_BLOCK));
        }, EntityType.POLAR_BEAR);

        createCondition("ON_AXOLOTL_SPAWNABLE",
                (world, position) -> world.getBlockState(position.below()).is(AXOLOTLS_SPAWNABLE_ON_TAG),
                EntityType.AXOLOTL
        );

        createCondition("ON_FOX_SPAWNABLE",
                (world, position) -> world.getBlockState(position.below()).is(FOXES_SPAWNABLE_ON_TAG),
                EntityType.FOX
        );

        createCondition("ON_GOAT_SPAWNABLE",
                (world, position) -> world.getBlockState(position.below()).is(GOATS_SPAWNABLE_ON_TAG),
                EntityType.GOAT
        );

        createCondition("ON_RABBITS_SPAWNABLE",
                (world, position) -> world.getBlockState(position.below()).is(RABBITS_SPAWNABLE_ON_TAG),
                EntityType.RABBIT
        );

        createCondition("BELOW_SEA_LEVEL",
                (world, position) -> position.getY() < world.getSeaLevel(),
                EntityType.DOLPHIN
        );

        createCondition("DARK_BLOCK_LIGHT",
                (world, position) -> world.getBrightness(EnumSkyBlock.b, position.getHandle()) <= 8,
                EntityType.PILLAGER
        );

        createCondition("IN_LAVA_AND_AIR_ABOVE", (world, position) -> {
            BlockPosition mutableBlockPosition = position.mutable();

            do {
                mutableBlockPosition.move(EnumDirection.b);
            } while (world.getFluidState(mutableBlockPosition.getHandle()).a(TagsFluid.b));

            return world.getBlockState(mutableBlockPosition.getHandle()).isAir();
        }, EntityType.STRIDER);

        createCondition("IN_SEA_SURFACE",
                (world, position) -> position.getY() < world.getSeaLevel() + 4,
                EntityType.TURTLE
        );

        createCondition("IN_SLIME_CHUNK_OR_SWAMP", (world, position) -> {
            Holder<BiomeBase> biomeBaseHolder = world.getBiome(position.getHandle());

            if (biomeBaseHolder.a(SWAMP_BIOME))
                return true;

            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(position.getHandle());
            boolean isSlimeChunk = SeededRandom.a(chunkcoordintpair.getX(), chunkcoordintpair.getZ(), ((GeneratorAccessSeed) world).D(),
                    world.getHandle().spigotConfig.slimeSeed).nextInt(10) == 0;
            return isSlimeChunk && position.getY() < 40;
        }, EntityType.SLIME);

        createCondition("IN_FISH_WATER",
                (world, position) -> world.getFluidState(position.below()).a(TagsFluid.b) &&
                        world.getBlockState(position.above()).getHandle().a(WATER_BLOCK) &&
                        position.getY() >= world.getSeaLevel() - 13 && position.getY() <= world.getSeaLevel(),
                EntityType.COD, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.DOLPHIN
        );

        createCondition("IN_FISH_WATER_OR_LUSH_CAVES",
                (world, position) -> world.getFluidState(position.below()).a(TagsFluid.b) &&
                        (world.getBiome(position.getHandle()).a(LUSH_CAVES_BIOME) ||
                                (world.getBlockState(position.above()).getHandle().a(WATER_BLOCK) &&
                                position.getY() >= world.getSeaLevel() - 13 && position.getY() <= world.getSeaLevel())),
                EntityType.TROPICAL_FISH
        );

        createCondition("COMPLETE_DARKNESS",
                (world, position) -> (world.isRaining() ? world.getMaxLocalRawBrightness(position.getHandle(), 10) :
                        world.getMaxLocalRawBrightness(position.getHandle())) == 0,
                EntityType.AXOLOTL, EntityType.DROWNED, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.GIANT, EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX,
                EntityType.VINDICATOR
        );

        createCondition("NOT_IN_OCEAN", (world, position) -> {
            Holder<BiomeBase> biomeBaseHolder = world.getBiome(position.getHandle());
            return !biomeBaseHolder.a(OCEAN_BIOME) && !biomeBaseHolder.a(DEEP_OCEAN_BIOME);
        }, EntityType.DOLPHIN);

        createCondition("IN_RIVER", (world, position) -> {
            Holder<BiomeBase> biomeBaseHolder = world.getBiome(position.getHandle());
            return biomeBaseHolder.a(RIVER_BIOME) || biomeBaseHolder.a(FROZEN_RIVER_BIOME);
        }, EntityType.DOLPHIN);

        createCondition("NOT_IN_OCEAN_DEEP",
                (world, position) -> position.getY() > 45,
                EntityType.DOLPHIN
        );

        createCondition("IN_OCEAN_DEEP",
                (world, position) -> world.getBlockState(position.getHandle()).getHandle().a(WATER_BLOCK) && position.getY() <= 30,
                EntityType.GLOW_SQUID
        );

        createCondition("NOT_ON_NETHER_WART_BLOCK",
                (world, position) -> !getBlockBelow(world, position).a(NETHER_WART_BLOCK_BLOCK),
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
                (world, position) -> getBlockBelow(world, position).a(GRASS_BLOCK_BLOCK),
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.GOAT, EntityType.HORSE,
                EntityType.LLAMA, EntityType.MULE, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON_HORSE,
                EntityType.WOLF, EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.PANDA, EntityType.TRADER_LLAMA
        );

        createCondition("ON_MYCELIUM",
                (world, position) -> getBlockBelow(world, position).a(MYCELIUM_BLOCK),
                EntityType.MUSHROOM_COW
        );

        createCondition("ON_NETHER_WART_BLOCK",
                (world, position) -> getBlockBelow(world, position).a(NETHER_WART_BLOCK_BLOCK),
                EntityType.HOGLIN, EntityType.PIGLIN
        );

        createCondition("ON_SAND",
                (world, position) -> getBlockBelow(world, position).a(SAND_BLOCK),
                EntityType.TURTLE
        );

        createCondition("ON_TREE_OR_AIR", (world, position) -> {
            IBlockData blockData = getBlockBelow(world, position);
            return blockData.a(LEAVES_TAG) || blockData.a(GRASS_BLOCK_BLOCK) ||
                    blockData.a(LOGS_TAG) || blockData.a(Blocks.a);
        }, EntityType.PARROT);
    }

    static class StackedMobSpawner extends MobSpawnerAbstract {

        private final WeakReference<WStackedSpawner> stackedSpawner;
        public String failureReason = "";

        private int spawnedEntities = 0;
        private WStackedEntity demoEntity = null;

        @Remap(classPath = "net.minecraft.world.level.BaseSpawner", name = "spawnDelay", type = Remap.Type.FIELD, remappedName = "c")
        @Remap(classPath = "net.minecraft.world.level.BaseSpawner", name = "nextSpawnData", type = Remap.Type.FIELD, remappedName = "e")
        @Remap(classPath = "net.minecraft.world.level.BaseSpawner", name = "minSpawnDelay", type = Remap.Type.FIELD, remappedName = "h")
        @Remap(classPath = "net.minecraft.world.level.BaseSpawner", name = "maxSpawnDelay", type = Remap.Type.FIELD, remappedName = "i")
        @Remap(classPath = "net.minecraft.world.level.BaseSpawner", name = "spawnCount", type = Remap.Type.FIELD, remappedName = "j")
        @Remap(classPath = "net.minecraft.world.level.BaseSpawner", name = "maxNearbyEntities", type = Remap.Type.FIELD, remappedName = "l")
        @Remap(classPath = "net.minecraft.world.level.BaseSpawner", name = "requiredPlayerRange", type = Remap.Type.FIELD, remappedName = "m")
        @Remap(classPath = "net.minecraft.world.level.BaseSpawner", name = "spawnRange", type = Remap.Type.FIELD, remappedName = "n")
        StackedMobSpawner(TileEntity tileEntity, StackedSpawner stackedSpawner) {
            this.stackedSpawner = new WeakReference<>((WStackedSpawner) stackedSpawner);

            MobSpawnerAbstract originalSpawner = tileEntity.getSpawner().getHandle();
            MOB_SPAWNER_ABSTRACT.set(tileEntity.getHandle(), this);

            this.e = originalSpawner.e;
            this.h = originalSpawner.h;
            this.i = originalSpawner.i;
            this.j = originalSpawner.j;
            this.l = originalSpawner.l;
            this.m = originalSpawner.m;
            this.n = originalSpawner.n;

            updateDemoEntity(tileEntity.getWorld(), tileEntity.getBlockPos());
            updateUpgrade(((WStackedSpawner) stackedSpawner).getUpgradeId());
        }

        @Override
        public void a(WorldServer nmsWorld, net.minecraft.core.BlockPosition nmsPosition) {
            WStackedSpawner stackedSpawner = this.stackedSpawner.get();

            if (stackedSpawner == null) {
                super.a(nmsWorld, nmsPosition);
                return;
            }

            World world = new World(nmsWorld);
            BlockPosition position = new BlockPosition(nmsPosition);

            if (!world.hasNearbyAlivePlayer(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, this.m)) {
                failureReason = "There are no nearby players.";
                return;
            }

            if (this.c <= -1)
                resetSpawnDelay(world, position);

            if (this.c > 0) {
                --this.c;
                return;
            }

            MobSpawnerData mobSpawnerData = new MobSpawnerData(this.e);
            Optional<EntityTypes<?>> entityTypesOptional = EntityTypes.a(mobSpawnerData.getEntityToSpawnNoMapping());

            if (!entityTypesOptional.isPresent()) {
                resetSpawnDelay(world, position);
                failureReason = "";
                return;
            }

            EntityTypes<?> entityTypes = entityTypesOptional.get();

            if (demoEntity == null) {
                super.a(nmsWorld, nmsPosition);
                failureReason = "";
                return;
            }

            Entity demoNMSEntity = new Entity(((CraftEntity) demoEntity.getLivingEntity()).getHandle());

            if (demoNMSEntity.getType() != entityTypes) {
                updateDemoEntity(world, position);

                if (demoEntity == null) {
                    super.a(nmsWorld, nmsPosition);
                    failureReason = "";
                    return;
                }

                updateUpgrade(stackedSpawner.getUpgradeId());

                demoNMSEntity = new Entity(((CraftEntity) demoEntity.getLivingEntity()).getHandle());
            }

            int stackAmount = stackedSpawner.getStackAmount();

            List<? extends net.minecraft.world.entity.Entity> nearbyEntities = world.getEntitiesOfClass(
                    demoNMSEntity.getHandle().getClass(), new AxisAlignedBB(position.getX(), position.getY(), position.getZ(),
                    position.getX() + 1, position.getY() + 1, position.getZ() + 1).g(this.n));

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

                    world.levelEvent(2004, position.getHandle(), 0);
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
                if (!attemptMobSpawning(world, position, entityTypes, mobsToSpawn, amountPerEntity, spawnCount, particlesAmount, stackedSpawner)) {
                    return;
                }
            }

            resetSpawnDelay(world, position);
        }

        @Override
        public void a(net.minecraft.world.level.World world, net.minecraft.core.BlockPosition position, int i) {
            world.a(position, SPAWNER_BLOCK, i, 0);
        }

        public void updateUpgrade(int upgradeId) {
            if (demoEntity != null)
                demoEntity.setUpgradeId(upgradeId);
        }

        private boolean attemptMobSpawning(World world, BlockPosition position, EntityTypes<?> entityTypes,
                                           int mobsToSpawn, int amountPerEntity, int spawnCount, short particlesAmount,
                                           WStackedSpawner stackedSpawner) {
            boolean hasSpawnedEntity = false;

            for (int i = 0; i < mobsToSpawn; i++) {
                java.util.Random random = world.getRandom();

                double x = position.getX() + (random.nextDouble() - random.nextDouble()) * this.n + 0.5D;
                double y = position.getY() + random.nextInt(3) - 1;
                double z = position.getZ() + (random.nextDouble() - random.nextDouble()) * this.n + 0.5D;

                Location location = new Location(world.getHandle().getWorld(), x, y, z);

                if (!world.noCollision(entityTypes.a(x, y, z))) {
                    failureReason = "Not enough space to spawn the entity.";
                    continue;
                }

                SpawnCondition failedCondition = plugin.getSystemManager().getSpawnConditions(demoEntity.getLivingEntity().getType())
                        .stream().filter(spawnCondition -> !spawnCondition.test(location)).findFirst().orElse(null);

                if (failedCondition != null) {
                    failureReason = "Cannot spawn entities due to " + failedCondition.getName() + " restriction.";
                    continue;
                }

                Entity bukkitEntity = generateEntity(world, x, y, z, true);

                if (bukkitEntity == null) {
                    resetSpawnDelay(world, position);
                    return false;
                }

                int amountToSpawn = spawnedEntities + amountPerEntity > spawnCount ? spawnCount - spawnedEntities : amountPerEntity;

                if (handleEntitySpawn(world, position, bukkitEntity, stackedSpawner, amountToSpawn, particlesAmount <= this.j)) {
                    spawnedEntities += amountPerEntity;
                    particlesAmount++;
                    hasSpawnedEntity = true;
                }
            }

            return hasSpawnedEntity;
        }

        private void resetSpawnDelay(World world, BlockPosition position) {
            if (this.i <= this.h) {
                this.c = this.h;
            } else {
                this.c = this.h + world.getRandom().nextInt(this.i - this.h);
            }

            // Set mob spawn data
            this.d.b(world.getRandom()).ifPresent(weightedEntry ->
                    this.a(world.getHandle(), position.getHandle(), weightedEntry.b()));

            spawnedEntities = 0;
            failureReason = "";

            a(world.getHandle(), position.getHandle(), 1);
        }

        private Entity generateEntity(World world, double x, double y, double z, boolean rotation) {
            MobSpawnerData mobSpawnerData = new MobSpawnerData(this.e);
            NBTTagCompound entityCompound = mobSpawnerData.getEntityToSpawn();
            net.minecraft.world.entity.Entity entity = EntityTypes.a(entityCompound.getHandle(), world.getHandle(), _entity -> {
                Entity mappedEntity = new Entity(_entity);
                mappedEntity.moveTo(x, y, z, rotation ? world.getRandom().nextFloat() * 360.0F : 0f, 0f);
                mappedEntity.setWorld(world.getHandle());
                _entity.valid = true;
                return _entity;
            });
            return entity == null ? null : new Entity(entity);
        }

        private boolean handleEntitySpawn(World world, BlockPosition position,
                                         Entity entity, WStackedSpawner stackedSpawner,
                                          int amountPerEntity, boolean spawnParticles) {
            StackedEntity stackedEntity = null;

            org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();

            EntityStorage.setMetadata(bukkitEntity, EntityFlag.SPAWN_CAUSE, SpawnCause.SPAWNER);

            if (amountPerEntity > 1 || stackedSpawner.getUpgradeId() != 0) {
                stackedEntity = WStackedEntity.of(bukkitEntity);
                ((WStackedEntity) stackedEntity).setUpgradeId(stackedSpawner.getUpgradeId());
                stackedEntity.setStackAmount(amountPerEntity, true);
            }

            if (entity.getHandle() instanceof EntityInsentient entityInsentient) {
                MobSpawnerData mobSpawnerData = new MobSpawnerData(this.e);
                NBTTagCompound entityToSpawn = mobSpawnerData.getEntityToSpawn();

                if (entityToSpawn.size() == 1 && entityToSpawn.contains("id", 8)) {
                    entity.finalizeSpawn((WorldServer) world.getHandle(), world.getCurrentDifficultyAt(entity.blockPosition()),
                            SPAWNER_MOB_SPAWN, null, null);
                }

                if (entity.getWorld().getHandle().spigotConfig.nerfSpawnerMobs) {
                    entityInsentient.aware = false;
                }
            }

            if (CraftEventFactory.callSpawnerSpawnEvent(entity.getHandle(), position.getHandle()).isCancelled()) {
                Entity vehicle = entity.getVehicle();
                if (vehicle != null) {
                    vehicle.discard();
                }

                for (net.minecraft.world.entity.Entity passenger : entity.getIndirectPassengers())
                    new Entity(passenger).discard();

                if (stackedEntity != null)
                    plugin.getSystemManager().removeStackObject(stackedEntity);
                EntityStorage.clearMetadata(bukkitEntity);
            } else {
                if (!addEntity(world, entity)) {
                    EntityStorage.clearMetadata(bukkitEntity);
                    return false;
                }

                if (spawnParticles)
                    world.levelEvent(2004, position.getHandle(), 0);

                if (entity.getHandle() instanceof EntityInsentient) {
                    entity.spawnAnim();
                }

                return true;
            }

            return false;
        }

        private boolean addEntity(World world, Entity entity) {
            entity.getHandle().valid = false;

            if (world.addFreshEntity(entity.getHandle(), CreatureSpawnEvent.SpawnReason.SPAWNER)) {
                entity.getPassengers().forEach(passenger -> addEntity(world, new Entity(passenger)));
                return true;
            }

            return false;
        }

        private StackedEntity getTargetEntity(StackedSpawner stackedSpawner, StackedEntity demoEntity,
                                              List<? extends net.minecraft.world.entity.Entity> nearbyEntities) {
            LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

            if (linkedEntity != null && linkedEntity.getType() == demoEntity.getType())
                return WStackedEntity.of(linkedEntity);

            Optional<CraftEntity> closestEntity = GeneralUtils.getClosestBukkit(stackedSpawner.getLocation(),
                    nearbyEntities.stream().map(net.minecraft.world.entity.Entity::getBukkitEntity).filter(entity ->
                            EntityUtils.isStackable(entity) &&
                                    demoEntity.runStackCheck(WStackedEntity.of(entity)) == StackCheckResult.SUCCESS));

            return closestEntity.map(WStackedEntity::of).orElse(null);
        }

        private void updateDemoEntity(World world, BlockPosition position) {
            Entity demoEntity = generateEntity(world, position.getX(), position.getY(), position.getZ(), false);

            if (demoEntity == null)
                return;

            WORLD_LEVEL_CALLBACK.get(world.getEntityManager()).a(demoEntity.getHandle());

            if (!EntityUtils.isStackable(demoEntity.getBukkitEntity())) {
                this.demoEntity = null;
                return;
            }

            this.demoEntity = (WStackedEntity) WStackedEntity.of(demoEntity.getBukkitEntity());
            this.demoEntity.setSpawnCause(SpawnCause.SPAWNER);
            this.demoEntity.setDemoEntity();
        }

    }

}
