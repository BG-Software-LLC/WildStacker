package com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.level;

import com.bgsoftware.wildstacker.nms.mapping.Remap;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.MappedObject;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.server.level.ChunkProviderServer;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.level.block.entity.TileEntity;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;

public class World extends MappedObject<net.minecraft.world.level.World> {

    public World(net.minecraft.world.level.World handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelAccessor",
            name = "levelEvent",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void levelEvent(EntityHuman entityHuman, int i, BlockPosition blockPosition, int j) {
        handle.a(entityHuman, i, blockPosition, j);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelAccessor",
            name = "getDifficulty",
            type = Remap.Type.METHOD,
            remappedName = "ag")
    public EnumDifficulty getDifficulty() {
        return handle.ag();
    }

    @Remap(classPath = "net.minecraft.world.level.LevelAccessor",
            name = "levelEvent",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public void levelEvent(int i, BlockPosition position, int j) {
        handle.c(i, position, j);
    }

    @Remap(classPath = "net.minecraft.world.level.BlockAndTintGetter",
            name = "getRawBrightness",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public int getRawBrightness(BlockPosition position, int ambientDarkness) {
        return handle.b(position, ambientDarkness);
    }

    @Remap(classPath = "net.minecraft.world.level.BlockAndTintGetter",
            name = "getBrightness",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public int getBrightness(EnumSkyBlock skyBlock, BlockPosition position) {
        return handle.a(skyBlock, position);
    }

    @Remap(classPath = "net.minecraft.world.level.EntityGetter",
            name = "getEntitiesOfClass",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public <T extends Entity> List<T> getEntitiesOfClass(Class<T> entityClass, AxisAlignedBB boundingBox) {
        return handle.a(entityClass, boundingBox);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelReader",
            name = "getMaxLocalRawBrightness",
            type = Remap.Type.METHOD,
            remappedName = "D")
    public int getMaxLocalRawBrightness(BlockPosition position) {
        return handle.D(position);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelReader",
            name = "getMaxLocalRawBrightness",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public int getMaxLocalRawBrightness(BlockPosition blockPosition, int i) {
        return handle.c(blockPosition, i);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelWriter",
            name = "addFreshEntity",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void addFreshEntity(Entity entity) {
        handle.b(entity);
    }

    public boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        return handle.addFreshEntity(entity, spawnReason);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getGameRules",
            type = Remap.Type.METHOD,
            remappedName = "W")
    public GameRules getGameRules() {
        return handle.W();
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getRandom",
            type = Remap.Type.METHOD,
            remappedName = "r_")
    public RandomSource getRandom() {
        return handle.r_();
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getEntities",
            type = Remap.Type.METHOD,
            remappedName = "F")
    public LevelEntityGetter<Entity> getEntities() {
        return handle.F();
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "broadcastEntityEvent",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void broadcastEntityEvent(Entity entity, byte status) {
        handle.a(entity, status);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getBlockEntity",
            type = Remap.Type.METHOD,
            remappedName = "c_")
    public TileEntity getBlockEntity(BlockPosition blockPosition) {
        return new TileEntity(handle.c_(blockPosition));
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getBlockState",
            type = Remap.Type.METHOD,
            remappedName = "a_")
    public net.minecraft.world.level.block.state.IBlockData getBlockStateNoMappings(BlockPosition blockPosition) {
        return handle.a_(blockPosition);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "isRaining",
            type = Remap.Type.METHOD,
            remappedName = "Y")
    public boolean isRaining() {
        return handle.Y();
    }

    public IBlockData getBlockState(BlockPosition blockPosition) {
        return new IBlockData(getBlockStateNoMappings(blockPosition));
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getSeaLevel",
            type = Remap.Type.METHOD,
            remappedName = "m_")
    public int getSeaLevel() {
        return handle.m_();
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getFluidState",
            type = Remap.Type.METHOD,
            remappedName = "b_")
    public Fluid getFluidState(BlockPosition blockPosition) {
        return handle.b_(blockPosition);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getCurrentDifficultyAt",
            type = Remap.Type.METHOD,
            remappedName = "d_")
    public DifficultyDamageScaler getCurrentDifficultyAt(BlockPosition blockPosition) {
        return handle.d_(blockPosition);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelReader",
            name = "getBiome",
            type = Remap.Type.METHOD,
            remappedName = "w")
    public Holder<BiomeBase> getBiome(BlockPosition blockPosition) {
        return handle.w(blockPosition);
    }

    @Remap(classPath = "net.minecraft.world.level.CollisionGetter",
            name = "noCollision",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public boolean noCollision(AxisAlignedBB boundingBox) {
        return handle.b(boundingBox);
    }

    @Remap(classPath = "net.minecraft.world.level.EntityGetter",
            name = "hasNearbyAlivePlayer",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public boolean hasNearbyAlivePlayer(double x, double y, double z, double range) {
        return handle.a(x, y, z, range);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "getChunkSource",
            type = Remap.Type.METHOD,
            remappedName = "k")
    public ChunkProviderServer getChunkSource() {
        return new ChunkProviderServer(((WorldServer) handle).k());
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "entityManager",
            type = Remap.Type.FIELD,
            remappedName = "P")
    public PersistentEntitySectionManager<Entity> getEntityManager() {
        return ((WorldServer) handle).P;
    }

}
