package com.bgsoftware.wildstacker.nms.v1_7_R4;

import com.bgsoftware.wildstacker.nms.v1_7_R4.world.WorldEntityListener;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import net.minecraft.server.v1_7_R4.BlockRotatable;
import net.minecraft.server.v1_7_R4.Chunk;
import net.minecraft.server.v1_7_R4.Entity;
import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.World;
import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NMSWorld implements com.bgsoftware.wildstacker.nms.NMSWorld {

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Entity entity = EntityTypes.a(bukkitEntity.getEntityId(), world);
        entity.setPosition(location.getX(), location.getY(), location.getZ());
        return !(entity instanceof EntityInsentient) || ((EntityInsentient) entity).canSpawn();
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getEntitiesAtChunk(ChunkPosition chunkPosition) {
        World world = ((CraftWorld) Bukkit.getWorld(chunkPosition.getWorld())).getHandle();

        Chunk chunk = world.getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());

        if (chunk == null)
            return new ArrayList<>();

        //noinspection unchecked
        return Arrays.stream((List<Entity>[]) chunk.entitySlices)
                .flatMap(Collection::stream)
                .map(Entity::getBukkitEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getNearbyEntities(Location location, int range,
                                                                  Predicate<org.bukkit.entity.Entity> filter) {
        return null;
    }

    @Override
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        world.addParticle(particle, location.getBlockX(), location.getBlockY(), location.getBlockZ(), offsetX, offsetY, offsetZ);
    }

    @Override
    public void attemptJoinRaid(Player player, org.bukkit.entity.Entity raider) {
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
        return world.getType(block.getX(), block.getY(), block.getZ()) instanceof BlockRotatable;
    }

    @Override
    public void setTurtleEggsAmount(Block turtleEggBlock, int amount) {
        // Do nothing.
    }

}
