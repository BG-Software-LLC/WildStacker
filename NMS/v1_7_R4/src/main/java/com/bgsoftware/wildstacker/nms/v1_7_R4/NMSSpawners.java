package com.bgsoftware.wildstacker.nms.v1_7_R4;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.nms.v1_7_R4.spawner.StackedMobSpawner;
import com.bgsoftware.wildstacker.nms.v1_7_R4.spawner.SyncedCreatureSpawnerImpl;
import com.bgsoftware.wildstacker.nms.v1_7_R4.world.BlockPosition;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Debug;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import net.minecraft.server.v1_7_R4.BiomeBase;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.Blocks;
import net.minecraft.server.v1_7_R4.EnumDifficulty;
import net.minecraft.server.v1_7_R4.EnumSkyBlock;
import net.minecraft.server.v1_7_R4.Material;
import net.minecraft.server.v1_7_R4.TileEntity;
import net.minecraft.server.v1_7_R4.TileEntityMobSpawner;
import net.minecraft.server.v1_7_R4.World;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.EntityType;

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

        TileEntity tileEntity = world.getTileEntity(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (isDebug && tileEntity instanceof TileEntityMobSpawner)
            Debug.debug("NMSSpawners", "updateStackedSpawner", "mobSpawner=" + ((TileEntityMobSpawner) tileEntity).getSpawner());

        if (tileEntity instanceof TileEntityMobSpawner && !(((TileEntityMobSpawner) tileEntity).getSpawner() instanceof StackedMobSpawner)) {
            if (isDebug)
                Debug.debug("NMSSpawners", "updateStackedSpawner", "Setting mobSpawner to new one.");
            new StackedMobSpawner((TileEntityMobSpawner) tileEntity, stackedSpawner);
            return true;
        }

        return false;
    }

    @Override
    public void registerSpawnConditions() {
        createCondition("ABOVE_SEA_LEVEL",
                (world, position) -> position.y >= 63,
                EntityType.OCELOT
        );

        createCondition("ANIMAL_LIGHT",
                (world, position) -> world.j(position.x, position.y, position.z) > 8,
                EntityType.CHICKEN, EntityType.COW, EntityType.HORSE, EntityType.MUSHROOM_COW, EntityType.PIG,
                EntityType.SHEEP, EntityType.WOLF
        );

        createCondition("IN_SLIME_CHUNK_OR_SWAMP",
                (world, position) -> world.getBiome(position.x, position.z) == BiomeBase.SWAMPLAND ||
                        world.getChunkAtWorldCoords(position.x, position.z).a(987234911L).nextInt(10) == 0 &&
                                position.y < 40,
                EntityType.SLIME
        );

        createCondition("MONSTER_LIGHT", (world, position) -> {
                    if (world.b(EnumSkyBlock.SKY, position.x, position.y, position.z) > world.random.nextInt(32)) {
                        return false;
                    } else {
                        int lightLevel = world.getLightLevel(position.x, position.y, position.z);

                        if (world.P()) {
                            int j = world.j;
                            world.c(10);
                            lightLevel = world.getLightLevel(position.x, position.y, position.z);
                            world.c(j);
                        }

                        return lightLevel <= world.random.nextInt(8);
                    }
                }, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.GIANT, EntityType.SKELETON,
                EntityType.SPIDER, EntityType.WITCH, EntityType.WITHER, EntityType.ZOMBIE
        );

        createCondition("NOT_PEACEFUL",
                (world, position) -> world.difficulty != EnumDifficulty.PEACEFUL,
                EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.ENDERMAN, EntityType.GHAST,
                EntityType.GIANT, EntityType.MAGMA_CUBE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME,
                EntityType.SPIDER, EntityType.WITCH, EntityType.WITHER, EntityType.ZOMBIE, EntityType.PIG_ZOMBIE
        );

        createCondition("ON_GRASS",
                (world, position) -> world.getType(position.x, position.y - 1, position.z) == Blocks.GRASS,
                EntityType.CHICKEN, EntityType.COW, EntityType.HORSE, EntityType.PIG, EntityType.SHEEP, EntityType.WOLF
        );

        createCondition("ON_GRASS_OR_LEAVES", (world, position) -> {
            Block block = world.getType(position.x, position.y - 1, position.z);
            return block == Blocks.GRASS || block.getMaterial() == Material.LEAVES;
        }, EntityType.OCELOT);

        createCondition("ON_MYCELIUM",
                (world, position) -> world.getType(position.x, position.y - 1, position.z) == Blocks.MYCEL,
                EntityType.MUSHROOM_COW
        );
    }

    @Override
    public SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner) {
        return new SyncedCreatureSpawnerImpl(creatureSpawner.getBlock());
    }

}
