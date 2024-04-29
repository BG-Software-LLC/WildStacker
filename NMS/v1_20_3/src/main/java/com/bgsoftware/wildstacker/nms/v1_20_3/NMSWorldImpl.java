package com.bgsoftware.wildstacker.nms.v1_20_3;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.nms.NMSWorld;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.craftbukkit.v1_20_R3.CraftParticle;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftRaider;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class NMSWorldImpl implements NMSWorld {

    private static final ReflectMethod<Boolean> RAIDER_CAN_RAID = new ReflectMethod<>(Raider.class, boolean.class, "gt");
    private static final ReflectMethod<Raid> RAIDER_RAID = new ReflectMethod<>(Raider.class, Raid.class, "gs");

    @Override
    public boolean canSpawnOn(org.bukkit.entity.Entity bukkitEntity, Location location) {
        if (location.getWorld() == null)
            return false;

        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        return SpawnPlacements.checkSpawnRules(entity.getType(), serverLevel, MobSpawnType.SPAWNER,
                blockPos, serverLevel.getRandom());
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getEntitiesAtChunk(ChunkPosition chunkPosition) {
        return new ArrayList<>();
    }

    @Override
    public Collection<org.bukkit.entity.Entity> getNearbyEntities(Location location, int range, Predicate<org.bukkit.entity.Entity> filter) {
        if (location.getWorld() == null)
            return Collections.emptyList();

        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        List<org.bukkit.entity.Entity> entities = new LinkedList<>();

        AABB searchArea = new AABB(
                location.getBlockX() - range,
                location.getBlockY() - range,
                location.getBlockZ() - range,
                location.getBlockX() + range,
                location.getBlockY() + range,
                location.getBlockZ() + range
        );

        serverLevel.getEntities().get(searchArea, entity -> {
            if (filter == null || filter.test(entity.getBukkitEntity()))
                entities.add(entity.getBukkitEntity());
        });

        return entities;
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

    @Override
    public void attemptJoinRaid(org.bukkit.entity.Player player, org.bukkit.entity.Entity bukkitRaider) {
        Raider raider = ((CraftRaider) bukkitRaider).getHandle();
        boolean hasActiveRaid;
        Raid raid;

        try {
            hasActiveRaid = raider.hasActiveRaid();
            raid = raider.getCurrentRaid();
        } catch (Throwable error) {
            hasActiveRaid = RAIDER_CAN_RAID.invoke(raider);
            raid = RAIDER_RAID.invoke(raider);
        }

        if (hasActiveRaid)
            raid.addHeroOfTheVillage(((CraftPlayer) player).getHandle());
    }

    @Override
    public void startEntityListen(World world) {
        // Do nothing.
    }

    @Override
    public Object getBlockData(Material type, short data) {
        return CraftBlockData.fromData(CraftMagicNumbers.getBlock(type, (byte) data));
    }

    @Override
    public boolean isRotatable(Block block) {
        BlockState blockState = ((CraftBlock) block).getNMS();
        return blockState.getBlock() instanceof RotatedPillarBlock;
    }

    @Override
    public void setTurtleEggsAmount(Block turtleEggBlock, int amount) {
        TurtleEgg turtleEgg = (TurtleEgg) turtleEggBlock.getBlockData();
        turtleEgg.setEggs(amount);
        turtleEggBlock.setBlockData(turtleEgg, true);
    }

}
