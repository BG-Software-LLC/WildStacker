package com.bgsoftware.wildstacker.nms.v1_8_R3;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.nms.v1_8_R3.spawner.SyncedCreatureSpawnerImpl;
import com.bgsoftware.wildstacker.nms.v1_8_R3.spawner.TileEntityMobSpawnerWatcher;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.EnumDifficulty;
import net.minecraft.server.v1_8_R3.EnumSkyBlock;
import net.minecraft.server.v1_8_R3.Material;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
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

    @Override
    public void updateStackedSpawners(org.bukkit.Chunk bukkitChunk) {
        org.bukkit.World bukkitWorld = bukkitChunk.getWorld();
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
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

        for (TileEntity tileEntity : watchersToAdd) {
            worldServer.t(tileEntity.getPosition());
            worldServer.setTileEntity(tileEntity.getPosition(), tileEntity);
        }
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ABOVE_SEA_LEVEL",
                (world, position) -> position.getY() >= world.F(),
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

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        return new SyncedCreatureSpawnerImpl(creatureSpawner.getBlock());
    }

}
