package com.bgsoftware.wildstacker.nms.v26_2;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;

public class NMSWorldImpl extends com.bgsoftware.wildstacker.nms.v26_2.AbstractNMSWorld {

    @Override
    protected boolean checkSpawnPlacementsRules(EntityType entityType, ServerLevel serverLevel, BlockPos blockPos) {
        return SpawnPlacements.checkSpawnRules(entityType, serverLevel, EntitySpawnReason.SPAWNER, blockPos, serverLevel.getRandom());
    }

    @Override
    protected BlockData getBlockData(BlockState blockState) {
        return blockState.asBlockData();
    }

    @Override
    protected BlockState getBlockState(Block block) {
        return ((CraftBlock) block).getBlockState();
    }

    @Override
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        World world = location.getWorld();
        if (world != null) {
            ServerLevel serverLevel = ((CraftWorld) world).getHandle();
            serverLevel.sendParticles(CraftParticle.createParticleParam(Particle.valueOf(particle), null),
                    false, false, location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                    count, offsetX, offsetY, offsetZ, extra);
        }
    }

}
