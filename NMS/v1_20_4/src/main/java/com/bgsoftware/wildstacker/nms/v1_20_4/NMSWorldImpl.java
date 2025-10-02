package com.bgsoftware.wildstacker.nms.v1_20_4;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftWorld;

public class NMSWorldImpl extends com.bgsoftware.wildstacker.nms.v1_20_4.AbstractNMSWorld {

    @Override
    protected boolean checkSpawnPlacementsRules(EntityType entityType, ServerLevel serverLevel, BlockPos blockPos) {
        return SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.SPAWNER, blockPos, serverLevel.getRandom());
    }

    @Override
    public void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra) {
        World world = location.getWorld();
        if (world != null) {
            ServerLevel serverLevel = ((CraftWorld) world).getHandle();
            serverLevel.sendParticles(null,
                    CraftParticle.createParticleParam(Particle.valueOf(particle), null),
                    location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                    count, offsetX, offsetY, offsetZ, extra, false);
        }
    }

}
