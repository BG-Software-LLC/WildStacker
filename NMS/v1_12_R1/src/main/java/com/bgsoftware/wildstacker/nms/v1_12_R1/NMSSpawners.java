package com.bgsoftware.wildstacker.nms.v1_12_R1;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.nms.v1_12_R1.spawner.SyncedCreatureSpawnerImpl;
import com.bgsoftware.wildstacker.nms.v1_12_R1.spawner.TileEntityMobSpawnerWatcher;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.Biomes;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockLeaves;
import net.minecraft.server.v1_12_R1.BlockLogAbstract;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.EnumDifficulty;
import net.minecraft.server.v1_12_R1.EnumSkyBlock;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.Material;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.EntityType;

import java.util.LinkedList;
import java.util.List;
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

    private static boolean isChunkContainsSpawners(Chunk chunk) {
        for (TileEntity tileEntity : chunk.getTileEntities().values()) {
            if (tileEntity instanceof TileEntityMobSpawner)
                return true;
        }

        return false;
    }

    @Override
    public void updateStackedSpawners(org.bukkit.Chunk bukkitChunk) {
        org.bukkit.World bukkitWorld = bukkitChunk.getWorld();
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        if (!isChunkContainsSpawners(chunk))
            return;

        WorldServer worldServer = (WorldServer) chunk.world;

        List<TileEntity> watchersToAdd = new LinkedList<>();

        for (TileEntity tileEntity : chunk.getTileEntities().values()) {
            if (tileEntity instanceof TileEntityMobSpawner && !(tileEntity instanceof TileEntityMobSpawnerWatcher)) {
                BlockPosition blockPosition = tileEntity.getPosition();

                StackedSpawner stackedSpawner = WStackedSpawner.of(bukkitWorld.getBlockAt(
                        blockPosition.getX(), blockPosition.getY(), blockPosition.getZ()));

                TileEntityMobSpawnerWatcher tileEntityMobSpawnerWatcher = new TileEntityMobSpawnerWatcher(
                        stackedSpawner, (TileEntityMobSpawner) tileEntity);

                watchersToAdd.add(tileEntityMobSpawnerWatcher);
            }
        }

        for(TileEntity tileEntity : watchersToAdd) {
            worldServer.s(tileEntity.getPosition());
            worldServer.setTileEntity(tileEntity.getPosition(), tileEntity);
        }
    }

    @Override
    public void updateStackedSpawner(StackedSpawner stackedSpawner) {
        Location location = stackedSpawner.getLocation();

        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();

        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();

        Chunk chunk = worldServer.getChunkAt(blockX >> 4, blockZ >> 4);

        TileEntity watcherToAdd = null;

        for (TileEntity tileEntity : chunk.getTileEntities().values()) {
            if (tileEntity instanceof TileEntityMobSpawner && !(tileEntity instanceof TileEntityMobSpawnerWatcher)) {
                BlockPosition blockPosition = tileEntity.getPosition();
                if (blockPosition.getX() == blockX && blockPosition.getY() == blockY && blockPosition.getZ() == blockZ) {
                    watcherToAdd = new TileEntityMobSpawnerWatcher(
                            stackedSpawner, (TileEntityMobSpawner) tileEntity);
                    break;
                }
            }
        }

        if (watcherToAdd != null) {
            worldServer.s(watcherToAdd.getPosition());
            worldServer.setTileEntity(watcherToAdd.getPosition(), watcherToAdd);
        }
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ABOVE_SEA_LEVEL",
                (world, position) -> position.getY() >= world.getSeaLevel(),
                EntityType.OCELOT
        );

        createCondition("ANIMAL_LIGHT",
                (world, position) -> world.j(position) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA,
                EntityType.MUSHROOM_COW, EntityType.MULE, EntityType.PARROT, EntityType.PIG, EntityType.RABBIT,
                EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.WOLF, EntityType.ZOMBIE_HORSE
        );

        createCondition("ANIMAL_LIGHT_AND_COLD", (world, position) -> {
            BiomeBase biomeBase = world.getBiome(position);
            boolean coldBiome = biomeBase == Biomes.l || biomeBase == Biomes.Z;
            Block block = world.getType(position.down()).getBlock();
            return world.j(position) > 8 && block == (coldBiome ? Blocks.GRASS : Blocks.ICE);
        }, EntityType.POLAR_BEAR);

        createCondition("IN_SLIME_CHUNK_OR_SWAMP",
                (world, position) -> world.getBiome(position) == Biomes.h || world.getChunkAtWorldCoords(position)
                        .a(world.spigotConfig.slimeSeed).nextInt(10) == 0 && position.getY() < 40,
                EntityType.SLIME
        );

        createCondition("MONSTER_LIGHT", (world, position) -> {
                    if (world.getBrightness(EnumSkyBlock.SKY, position) > world.random.nextInt(32)) {
                        return false;
                    } else {
                        int lightLevel = world.getLightLevel(position);

                        if (world.X()) {
                            int j = world.ah();
                            world.c(10);
                            lightLevel = world.getLightLevel(position);
                            world.c(j);
                        }

                        return lightLevel <= world.random.nextInt(8);
                    }
                }, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.GIANT,
                EntityType.HUSK, EntityType.SKELETON, EntityType.SPIDER,
                EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON, EntityType.ZOMBIE,
                EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.VEX, EntityType.VINDICATOR
        );

        createCondition("NOT_PEACEFUL",
                (world, position) -> world.getDifficulty() != EnumDifficulty.PEACEFUL,
                EntityType.GUARDIAN, EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER,
                EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.GHAST, EntityType.GIANT, EntityType.HUSK,
                EntityType.MAGMA_CUBE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.STRAY, EntityType.WITCH, EntityType.WITHER, EntityType.WITHER_SKELETON,
                EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.EVOKER,
                EntityType.ILLUSIONER, EntityType.VEX, EntityType.VINDICATOR, EntityType.ELDER_GUARDIAN
        );

        createCondition("ON_GRASS",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.GRASS,
                EntityType.CHICKEN, EntityType.COW, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA,
                EntityType.MULE, EntityType.PIG, EntityType.SHEEP, EntityType.SKELETON_HORSE, EntityType.WOLF,
                EntityType.ZOMBIE_HORSE
        );

        createCondition("ON_GRASS_OR_LEAVES", (world, position) -> {
            IBlockData blockData = world.getType(position.down());
            return blockData.getBlock() == Blocks.GRASS || blockData.getMaterial() == Material.LEAVES;
        }, EntityType.OCELOT);

        createCondition("ON_GRASS_OR_SAND_OR_SNOW", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block == Blocks.GRASS || block == Blocks.SAND || block == Blocks.SNOW;
        }, EntityType.RABBIT);

        createCondition("ON_MYCELIUM",
                (world, position) -> world.getType(position.down()).getBlock() == Blocks.MYCELIUM,
                EntityType.MUSHROOM_COW
        );

        createCondition("ON_TREE_OR_AIR", (world, position) -> {
            Block block = world.getType(position.down()).getBlock();
            return block instanceof BlockLeaves || block == Blocks.GRASS ||
                    block instanceof BlockLogAbstract || block == Blocks.AIR;
        }, EntityType.PARROT);
    }

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        return new SyncedCreatureSpawnerImpl(creatureSpawner.getBlock());
    }

}
