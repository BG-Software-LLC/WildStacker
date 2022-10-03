package com.bgsoftware.wildstacker.nms.v1_12_R1.world;

import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.IWorldAccess;
import net.minecraft.server.v1_12_R1.SoundCategory;
import net.minecraft.server.v1_12_R1.SoundEffect;
import net.minecraft.server.v1_12_R1.World;

import javax.annotation.Nullable;

public class WorldEntityListener implements IWorldAccess {

    private static final WorldEntityListener INSTANCE = new WorldEntityListener();

    public static WorldEntityListener getInstance() {
        return INSTANCE;
    }

    private WorldEntityListener() {

    }

    @Override
    public void a(World world, BlockPosition blockPosition, IBlockData iBlockData, IBlockData iBlockData1, int i) {
        // Do nothing.
    }

    @Override
    public void a(BlockPosition blockPosition) {
        // Do nothing.
    }

    @Override
    public void a(int i, int i1, int i2, int i3, int i4, int i5) {
        // Do nothing.
    }

    @Override
    public void a(@Nullable EntityHuman entityHuman, SoundEffect soundEffect, SoundCategory soundCategory, double v, double v1, double v2, float v3, float v4) {
        // Do nothing.
    }

    @Override
    public void a(SoundEffect soundEffect, BlockPosition blockPosition) {
        // Do nothing.
    }

    @Override
    public void a(int i, boolean b, double v, double v1, double v2, double v3, double v4, double v5, int... ints) {
        // Do nothing.
    }

    @Override
    public void a(int i, boolean b, boolean b1, double v, double v1, double v2, double v3, double v4, double v5, int... ints) {
        // Do nothing.
    }

    @Override
    public void a(Entity entity) {
        // Do nothing.
    }

    @Override
    public void b(Entity entity) {
        EntitiesListener.IMP.handleEntityRemove(entity.getBukkitEntity());
    }

    @Override
    public void a(int i, BlockPosition blockPosition, int i1) {
        // Do nothing.
    }

    @Override
    public void a(EntityHuman entityHuman, int i, BlockPosition blockPosition, int i1) {
        // Do nothing.
    }

    @Override
    public void b(int i, BlockPosition blockPosition, int i1) {
        // Do nothing.
    }

}
