package com.bgsoftware.wildstacker.nms.v1_19;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.nms.v1_19.spawner.SpawnerWatcherTickingBlockEntity;
import com.bgsoftware.wildstacker.nms.v1_19.spawner.SyncedCreatureSpawnerImpl;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_19_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;

public final class NMSSpawners implements com.bgsoftware.wildstacker.nms.NMSSpawners {

    private static final ReflectField<List<TickingBlockEntity>> LEVEL_BLOCK_ENTITY_TICKERS_PROTECTED = new ReflectField<>(
            Level.class, List.class, Modifier.PROTECTED | Modifier.FINAL, 1);

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static void createCondition(String id, BiPredicate<ServerLevel, BlockPos> predicate, EntityType... entityTypes) {
        SpawnCondition spawnCondition = SpawnCondition.register(new SpawnCondition(id, EntityUtils.format(id)) {
            @Override
            public boolean test(Location location) {
                return predicate.test(((CraftWorld) location.getWorld()).getHandle(),
                        new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            }
        });
        plugin.getSystemManager().addSpawnCondition(spawnCondition, entityTypes);
    }

    private static BlockState getBlockBelow(ServerLevel serverLevel, BlockPos blockPos) {
        return serverLevel.getBlockState(blockPos.below());
    }

    @Override
    public void updateStackedSpawners(Chunk chunk) {
        World bukkitWorld = chunk.getWorld();
        LevelChunk levelChunk = (LevelChunk) ((CraftChunk) chunk).getHandle(ChunkStatus.FULL);
        ServerLevel serverLevel = levelChunk.level;

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        List<TickingBlockEntity> watchersToAdd = new LinkedList<>();
        List<TickingBlockEntity> blockEntityTickers = LEVEL_BLOCK_ENTITY_TICKERS_PROTECTED.get(serverLevel);
        Iterator<TickingBlockEntity> blockEntityIterator = blockEntityTickers.iterator();

        while (blockEntityIterator.hasNext()) {
            TickingBlockEntity tickingBlockEntity = blockEntityIterator.next();
            if (tickingBlockEntity instanceof SpawnerWatcherTickingBlockEntity)
                continue;
            ChunkPos chunkPos = new ChunkPos(tickingBlockEntity.getPos());
            if (chunkPos.x == chunkX && chunkPos.z == chunkZ) {
                BlockPos blockPos = tickingBlockEntity.getPos();
                BlockEntity blockEntity = levelChunk.getBlockEntity(blockPos);
                if (blockEntity instanceof SpawnerBlockEntity) {
                    StackedSpawner stackedSpawner = WStackedSpawner.of(bukkitWorld.getBlockAt(
                            blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                    watchersToAdd.add(new SpawnerWatcherTickingBlockEntity(
                            stackedSpawner, (SpawnerBlockEntity) blockEntity, tickingBlockEntity));
                    blockEntityIterator.remove();
                }
            }
        }

        blockEntityTickers.addAll(watchersToAdd);
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ANIMAL_LIGHT",
                (world, position) -> world.getRawBrightness(position, 0) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.GOAT, EntityType.HORSE,
                EntityType.LLAMA, EntityType.MUSHROOM_COW, EntityType.MULE, EntityType.PARROT, EntityType.PIG,
                EntityType.RABBIT, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.TURTLE, EntityType.WOLF,
                EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.FOX, EntityType.PANDA, EntityType.TRADER_LLAMA
        );

        createCondition("ANIMAL_LIGHT_AND_COLD", (world, position) -> {
            Holder<Biome> biome = world.getBiome(position);
            boolean coldBiome = biome.is(Biomes.FROZEN_OCEAN) || biome.is(Biomes.DEEP_FROZEN_OCEAN);
            BlockState blockState = getBlockBelow(world, position);
            return world.getRawBrightness(position, 0) > 8 &&
                    (coldBiome ? blockState.is(Blocks.GRASS_BLOCK) : blockState.is(Blocks.ICE));
        }, EntityType.POLAR_BEAR);

        createCondition("ON_AXOLOTL_SPAWNABLE",
                (world, position) -> getBlockBelow(world, position).is(BlockTags.AXOLOTLS_SPAWNABLE_ON),
                EntityType.AXOLOTL
        );

        createCondition("ON_FOX_SPAWNABLE",
                (world, position) -> getBlockBelow(world, position).is(BlockTags.FOXES_SPAWNABLE_ON),
                EntityType.FOX
        );

        createCondition("ON_GOAT_SPAWNABLE",
                (world, position) -> getBlockBelow(world, position).is(BlockTags.GOATS_SPAWNABLE_ON),
                EntityType.GOAT
        );

        createCondition("ON_RABBITS_SPAWNABLE",
                (world, position) -> getBlockBelow(world, position).is(BlockTags.RABBITS_SPAWNABLE_ON),
                EntityType.RABBIT
        );

        createCondition("BELOW_SEA_LEVEL",
                (world, position) -> position.getY() < world.getSeaLevel(),
                EntityType.DOLPHIN
        );

        createCondition("DARK_BLOCK_LIGHT",
                (world, position) -> world.getBrightness(LightLayer.SKY, position) <= 8,
                EntityType.PILLAGER
        );

        createCondition("IN_LAVA_AND_AIR_ABOVE", (world, position) -> {
            BlockPos.MutableBlockPos mutableBlockPos = position.mutable();

            do {
                mutableBlockPos.move(Direction.UP);
            } while (world.getFluidState(mutableBlockPos).is(FluidTags.LAVA));

            return world.getBlockState(mutableBlockPos).isAir();
        }, EntityType.STRIDER);

        createCondition("IN_SEA_SURFACE",
                (world, position) -> position.getY() < world.getSeaLevel() + 4,
                EntityType.TURTLE
        );

        createCondition("IN_SLIME_CHUNK_OR_SWAMP", (world, position) -> {
            Holder<Biome> biome = world.getBiome(position);

            if (biome.is(Biomes.SWAMP))
                return true;

            ChunkPos chunkPos = new ChunkPos(position);
            boolean isSlimeChunk = WorldgenRandom.seedSlimeChunk(chunkPos.x, chunkPos.z, ((WorldGenLevel) world).getSeed(),
                    world.spigotConfig.slimeSeed).nextInt(10) == 0;
            return isSlimeChunk && position.getY() < 40;
        }, EntityType.SLIME);

        createCondition("IN_FISH_WATER",
                (world, position) -> world.getFluidState(position.below()).is(FluidTags.WATER) &&
                        world.getBlockState(position.above()).is(Blocks.WATER) &&
                        position.getY() >= world.getSeaLevel() - 13 && position.getY() <= world.getSeaLevel(),
                EntityType.COD, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.DOLPHIN
        );

        createCondition("IN_FISH_WATER_OR_LUSH_CAVES",
                (world, position) -> world.getFluidState(position.below()).is(FluidTags.WATER) &&
                        (world.getBiome(position).is(Biomes.LUSH_CAVES) ||
                                (world.getBlockState(position.above()).is(Blocks.WATER) &&
                                        position.getY() >= world.getSeaLevel() - 13 && position.getY() <= world.getSeaLevel())),
                EntityType.TROPICAL_FISH
        );

        createCondition("COMPLETE_DARKNESS",
                (world, position) -> (world.isRaining() ? world.getMaxLocalRawBrightness(position, 10) :
                        world.getMaxLocalRawBrightness(position)) == 0,
                EntityType.AXOLOTL, EntityType.DROWNED, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.GIANT, EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX,
                EntityType.VINDICATOR
        );

        createCondition("NOT_IN_OCEAN", (world, position) -> {
            Holder<Biome> biome = world.getBiome(position);
            return !biome.is(Biomes.OCEAN) && !biome.is(Biomes.DEEP_OCEAN);
        }, EntityType.DOLPHIN);

        createCondition("IN_RIVER", (world, position) -> {
            Holder<Biome> biome = world.getBiome(position);
            return !biome.is(Biomes.RIVER) && !biome.is(Biomes.FROZEN_RIVER);
        }, EntityType.DOLPHIN);

        createCondition("NOT_IN_OCEAN_DEEP",
                (world, position) -> position.getY() > 45,
                EntityType.DOLPHIN
        );

        createCondition("IN_OCEAN_DEEP",
                (world, position) -> world.getBlockState(position).is(Blocks.WATER) && position.getY() <= 30,
                EntityType.GLOW_SQUID
        );

        createCondition("NOT_ON_NETHER_WART_BLOCK",
                (world, position) -> !getBlockBelow(world, position).is(Blocks.NETHER_WART_BLOCK),
                EntityType.ZOMBIFIED_PIGLIN
        );

        createCondition("NOT_PEACEFUL",
                (world, position) -> world.getDifficulty() != Difficulty.PEACEFUL,
                EntityType.DROWNED, EntityType.GUARDIAN, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.GHAST, EntityType.GIANT, EntityType.HUSK,
                EntityType.MAGMA_CUBE, EntityType.PILLAGER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON,
                EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER,
                EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX, EntityType.VINDICATOR, EntityType.ELDER_GUARDIAN
        );

        createCondition("ON_GRASS",
                (world, position) -> getBlockBelow(world, position).is(Blocks.GRASS_BLOCK),
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.GOAT, EntityType.HORSE,
                EntityType.LLAMA, EntityType.MULE, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON_HORSE,
                EntityType.WOLF, EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.PANDA, EntityType.TRADER_LLAMA
        );

        createCondition("ON_MYCELIUM",
                (world, position) -> getBlockBelow(world, position).is(Blocks.MYCELIUM),
                EntityType.MUSHROOM_COW
        );

        createCondition("ON_NETHER_WART_BLOCK",
                (world, position) -> getBlockBelow(world, position).is(Blocks.NETHER_WART_BLOCK),
                EntityType.HOGLIN, EntityType.PIGLIN
        );

        createCondition("ON_SAND",
                (world, position) -> getBlockBelow(world, position).is(Blocks.SAND),
                EntityType.TURTLE
        );

        createCondition("ON_TREE_OR_AIR", (world, position) -> {
            BlockState blockState = getBlockBelow(world, position);
            return blockState.is(BlockTags.LEAVES) || blockState.is(Blocks.GRASS_BLOCK) ||
                    blockState.is(BlockTags.LOGS) || blockState.is(Blocks.AIR);
        }, EntityType.PARROT);
    }

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        World bukkitWorld = creatureSpawner.getWorld();
        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(creatureSpawner.getX(), creatureSpawner.getY(), creatureSpawner.getZ());
        SpawnerBlockEntity spawnerBlockEntity = (SpawnerBlockEntity) serverLevel.getBlockEntity(blockPos);
        return new SyncedCreatureSpawnerImpl(bukkitWorld, spawnerBlockEntity);
    }

}
