package com.bgsoftware.wildstacker.nms.v1_8_R3;

import com.bgsoftware.wildstacker.nms.v1_8_R3.world.WorldEntityListener;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.BlockRotatable;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NMSWorld implements com.bgsoftware.wildstacker.nms.NMSWorld {

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.Entity entity = EntityTypes.a(bukkitEntity.getEntityId(), world);

        if (entity == null)
            return false;

        entity.setPosition(location.getX(), location.getY(), location.getZ());
        return !(entity instanceof EntityInsentient) || (((EntityInsentient) entity).bR() && ((EntityInsentient) entity).canSpawn());
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getEntitiesAtChunk(ChunkPosition chunkPosition) {
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) Bukkit.getWorld(chunkPosition.getWorld())).getHandle();

        Chunk chunk = world.getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());

        if (chunk == null)
            return new ArrayList<>();

        return Arrays.stream(chunk.entitySlices)
                .flatMap(Collection::stream)
                .map(net.minecraft.server.v1_8_R3.Entity::getBukkitEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, int range, Predicate<Entity> filter) {
        return null;
    }

    @Override
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        world.sendParticles(null, EnumParticle.valueOf(particle), true, location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                count, offsetX, offsetY, offsetZ, extra);
    }

    @Override
    public void attemptJoinRaid(Player player, Entity raider) {
        // Do nothing.
    }

    @Override
    public void startEntityListen(org.bukkit.World world) {
        ((CraftWorld) world).getHandle().addIWorldAccess(WorldEntityListener.getInstance());
    }

    @Override
    public Object getBlockData(Material type, short data) {
        throw new UnsupportedOperationException("Not supported in this version.");
    }

    @Override
    public boolean isRotatable(Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        return world.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock() instanceof BlockRotatable;
    }

    @Override
    public void setTurtleEggsAmount(Block turtleEggBlock, int amount) {
        // Do nothing.
    }

}
