package com.bgsoftware.wildstacker.nms.v1_16_R3;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.nms.v1_16_R3.spawner.StackedMobSpawner;
import com.bgsoftware.wildstacker.nms.v1_16_R3.spawner.SyncedCreatureSpawnerImpl;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Debug;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.Biomes;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.EntityMonster;
import net.minecraft.server.v1_16_R3.EnumDifficulty;
import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.EnumSkyBlock;
import net.minecraft.server.v1_16_R3.GeneratorAccessSeed;
import net.minecraft.server.v1_16_R3.ResourceKey;
import net.minecraft.server.v1_16_R3.SeededRandom;
import net.minecraft.server.v1_16_R3.TagsBlock;
import net.minecraft.server.v1_16_R3.TagsFluid;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.EntityType;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public final class NMSSpawners implements com.bgsoftware.wildstacker.nms.NMSSpawners {

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
        boolean isDebug = ((WStackedSpawner) stackedSpawner).isDebug();

        if (isDebug)
            Debug.debug("NMSSpawners", "updateStackedSpawner", "Trying to update spawner");

        World world = ((CraftWorld) stackedSpawner.getWorld()).getHandle();
        Location location = stackedSpawner.getLocation();

        TileEntity tileEntity = world.getTileEntity(new BlockPosition(location.getX(), location.getY(), location.getZ()));

        if (isDebug && tileEntity instanceof TileEntityMobSpawner)
            Debug.debug("NMSSpawners", "updateStackedSpawner", "mobSpawner=" + ((TileEntityMobSpawner) tileEntity).getSpawner());

        if (tileEntity instanceof TileEntityMobSpawner && (
                !(((TileEntityMobSpawner) tileEntity).getSpawner() instanceof StackedMobSpawner) ||
                        !((StackedMobSpawner) ((TileEntityMobSpawner) tileEntity).getSpawner()).isValid())) {
            if (isDebug)
                Debug.debug("NMSSpawners", "updateStackedSpawner", "Setting mobSpawner to new one.");
            new StackedMobSpawner((TileEntityMobSpawner) tileEntity, stackedSpawner);
            return true;
        }

        return false;
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
            Optional<ResourceKey<BiomeBase>> biomeBase = world.i(position);
            boolean coldBiome = Objects.equals(biomeBase, Optional.of(Biomes.FROZEN_OCEAN)) ||
                    Objects.equals(biomeBase, Optional.of(Biomes.DEEP_FROZEN_OCEAN));
            Block block = world.getType(position.down()).getBlock();
            return world.getLightLevel(position, 0) > 8 && (coldBiome ? block.a(Blocks.GRASS_BLOCK) : block.a(Blocks.ICE));
        }, EntityType.POLAR_BEAR);

        createCondition("BELOW_SEA_LEVEL",
                (world, position) -> position.getY() < world.getSeaLevel(),
                EntityType.DOLPHIN
        );

        createCondition("DARK_BLOCK_LIGHT",
                (world, position) -> world.getBrightness(EnumSkyBlock.BLOCK, position) <= 8,
                EntityType.PILLAGER
        );

        createCondition("IN_LAVA_AND_AIR_ABOVE", (world, position) -> {
            BlockPosition.MutableBlockPosition mutableBlockPosition = position.i();

            do {
                mutableBlockPosition.c(EnumDirection.UP);
            } while (world.getFluid(mutableBlockPosition).a(TagsFluid.LAVA));

            return world.getType(mutableBlockPosition).isAir();
        }, EntityType.STRIDER);

        createCondition("IN_SEA_SURFACE",
                (world, position) -> position.getY() < world.getSeaLevel() + 4,
                EntityType.TURTLE
        );

        createCondition("IN_SLIME_CHUNK_OR_SWAMP", (world, position) -> {
            if (Objects.equals(world.i(position), Optional.of(Biomes.SWAMP)))
                return true;

            ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(position);
            boolean isSlimeChunk = SeededRandom.a(chunkcoordintpair.x, chunkcoordintpair.z, ((GeneratorAccessSeed) world).getSeed(),
                    world.getMinecraftWorld().spigotConfig.slimeSeed).nextInt(10) == 0;
            return isSlimeChunk && position.getY() < 40;
        }, EntityType.SLIME);

        createCondition("IN_WATER_DEEP",
                (world, position) -> world.getFluid(position).a(TagsFluid.WATER) && world.getFluid(position.up()).a(TagsFluid.WATER),
                EntityType.COD, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.DOLPHIN
        );

        createCondition("MONSTER_LIGHT",
                (world, position) -> EntityMonster.a((WorldServer) world, position, world.random),
                EntityType.DROWNED, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.GIANT, EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX,
                EntityType.VINDICATOR
        );

        createCondition("NOT_IN_OCEAN", (world, position) -> {
            ResourceKey<BiomeBase> biomeBase = world.i(position).orElse(null);
            return biomeBase == null || (!biomeBase.equals(Biomes.OCEAN) && !biomeBase.equals(Biomes.DEEP_OCEAN));
        }, EntityType.DOLPHIN);

        createCondition("IN_RIVER", (world, position) -> {
            Optional<ResourceKey<BiomeBase>> biomeBase = world.i(position);
            return Objects.equals(biomeBase, Optional.of(Biomes.RIVER)) ||
                    Objects.equals(biomeBase, Optional.of(Biomes.FROZEN_RIVER));
        }, EntityType.DOLPHIN);

        createCondition("NOT_IN_OCEAN_DEEP",
                (world, position) -> position.getY() > 45,
                EntityType.DOLPHIN
        );

        createCondition("NOT_ON_NETHER_WART_BLOCK",
                (world, position) -> !world.getType(position.down()).a(Blocks.NETHER_WART_BLOCK),
                EntityType.ZOMBIFIED_PIGLIN
        );

        createCondition("NOT_PEACEFUL",
                (world, position) -> world.getDifficulty() != EnumDifficulty.PEACEFUL,
                EntityType.DROWNED, EntityType.GUARDIAN, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.GHAST, EntityType.GIANT, EntityType.HUSK,
                EntityType.MAGMA_CUBE, EntityType.PILLAGER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON,
                EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER,
                EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.VEX, EntityType.VINDICATOR, EntityType.ELDER_GUARDIAN
        );

        createCondition("ON_GRASS",
                (world, position) -> world.getType(position.down()).a(Blocks.GRASS_BLOCK),
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA,
                EntityType.MULE, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.WOLF,
                EntityType.ZOMBIE_HORSE, EntityType.CAT, EntityType.FOX, EntityType.PANDA, EntityType.TRADER_LLAMA
        );

        createCondition("ON_GRASS_OR_SAND_OR_SNOW", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block.a(Blocks.GRASS_BLOCK) || block.a(Blocks.SNOW) || block.a(Blocks.SAND);
        }, EntityType.RABBIT);

        createCondition("ON_MYCELIUM",
                (world, position) -> world.getType(position.down()).a(Blocks.MYCELIUM),
                EntityType.MUSHROOM_COW
        );

        createCondition("ON_NETHER_WART_BLOCK",
                (world, position) -> world.getType(position.down()).a(Blocks.NETHER_WART_BLOCK),
                EntityType.HOGLIN, EntityType.PIGLIN
        );

        createCondition("ON_SAND",
                (world, position) -> world.getType(position.down()).a(Blocks.SAND),
                EntityType.TURTLE
        );

        createCondition("ON_TREE_OR_AIR", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block.a(TagsBlock.LEAVES) || block.a(Blocks.GRASS_BLOCK) || block.a(TagsBlock.LOGS) || block.a(Blocks.AIR);
        }, EntityType.PARROT);
    }

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        return new SyncedCreatureSpawnerImpl(creatureSpawner.getBlock());
    }

}
