package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;

public interface NMSWorld {

    boolean canSpawnOn(Entity entity, Location location);

    Collection<Entity> getEntitiesAtChunk(ChunkPosition chunkPosition);

    @Nullable
    Collection<Entity> getNearbyEntities(Location location, int range, Predicate<Entity> filter);

    void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra);

    void attemptJoinRaid(Player player, Entity raider);

    void startEntityListen(World world);

    Object getBlockData(Material type, short data);

    boolean isRotatable(Block block);

    void setTurtleEggsAmount(Block turtleEggBlock, int amount);

}
