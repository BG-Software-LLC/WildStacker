package com.bgsoftware.wildstacker.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockRotatable;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.EntityPositionTypes;
import net.minecraft.server.v1_16_R3.EntityRaider;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NMSWorld implements com.bgsoftware.wildstacker.nms.NMSWorld {

    private static final ReflectField<Collection[]> ENTITY_SLICES = new ReflectField<>(Chunk.class, Collection[].class, "entitySlices");

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        assert location.getWorld() != null;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        EntityTypes<?> entityTypes = ((CraftEntity) bukkitEntity).getHandle().getEntityType();
        return EntityPositionTypes.a(entityTypes, world.getMinecraftWorld(), EnumMobSpawn.SPAWNER, new BlockPosition(location.getX(), location.getY(), location.getZ()), world.getRandom());
    }

    @Override
    public Collection<Entity> getEntitiesAtChunk(ChunkPosition chunkPosition) {
        World world = ((CraftWorld) Bukkit.getWorld(chunkPosition.getWorld())).getHandle();

        Chunk chunk;

        try {
            chunk = world.getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());
        } catch (Throwable ex) {
            chunk = world.getChunkProvider().getChunkAt(chunkPosition.getX(), chunkPosition.getZ(), false);
        }

        if (chunk == null)
            return new ArrayList<>();

        Collection<net.minecraft.server.v1_16_R3.Entity>[] entitySlices = null;

        try {
            entitySlices = chunk.entitySlices;
        } catch (Throwable ex) {
            if (ENTITY_SLICES.isValid())
                // noinspection unchecked
                entitySlices = ENTITY_SLICES.get(chunk);
        }

        if (entitySlices == null)
            return new ArrayList<>();

        return Arrays.stream(entitySlices)
                .flatMap(Collection::stream)
                .map(net.minecraft.server.v1_16_R3.Entity::getBukkitEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, int range, Predicate<Entity> filter) {
        return null;
    }

    @Override
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        assert location.getWorld() != null;
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        world.sendParticles(null, CraftParticle.toNMS(Particle.valueOf(particle)), location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                count, offsetX, offsetY, offsetZ, extra, false);
    }

    @Override
    public void attemptJoinRaid(Player player, org.bukkit.entity.Entity raider) {
        EntityRaider entityRaider = (EntityRaider) ((CraftEntity) raider).getHandle();
        if (entityRaider.fb())
            entityRaider.fa().a((net.minecraft.server.v1_16_R3.Entity) ((CraftPlayer) player).getHandle());
    }

    @Override
    public void startEntityListen(org.bukkit.World world) {
        // Do nothing.
    }

    @Override
    public Object getBlockData(Material type, short data) {
        return CraftBlockData.fromData(CraftMagicNumbers.getBlock(type, (byte) data));
    }

    @Override
    public boolean isRotatable(Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        return world.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock() instanceof BlockRotatable;
    }

    @Override
    public void setTurtleEggsAmount(Block turtleEggBlock, int amount) {
        TurtleEgg turtleEgg = (TurtleEgg) turtleEggBlock.getBlockData();
        turtleEgg.setEggs(amount);
        turtleEggBlock.setBlockData(turtleEgg, true);
    }

}
