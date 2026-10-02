package com.bgsoftware.wildstacker.nms.v1_17;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;

public class NMSWorldImpl extends com.bgsoftware.wildstacker.nms.v1_17.AbstractNMSWorld {

    @Override
    protected boolean checkSpawnPlacementsRules(EntityType entityType, ServerLevel serverLevel, BlockPos blockPos) {
        return SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.SPAWNER, blockPos, serverLevel.getRandom());
    }

    @Override
    protected BlockData getBlockData(BlockState blockState) {
        return CraftBlockData.fromData(blockState);
    }

    @Override
    protected BlockState getBlockState(Block block) {
        return ((CraftBlock) block).getNMS();
    }

    @Override
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        World world = location.getWorld();
        if (world != null) {
            ServerLevel serverLevel = ((CraftWorld) world).getHandle();
            serverLevel.sendParticles(null,
                    CraftParticle.toNMS(Particle.valueOf(particle)),
                    location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                    count, offsetX, offsetY, offsetZ, extra, false);
        }
    }

}
