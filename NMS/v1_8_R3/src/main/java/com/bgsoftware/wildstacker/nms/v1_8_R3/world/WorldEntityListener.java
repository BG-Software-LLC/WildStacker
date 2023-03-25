package com.bgsoftware.wildstacker.nms.v1_8_R3.world;

import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.IWorldAccess;

public class WorldEntityListener implements IWorldAccess {

    private static final WorldEntityListener INSTANCE = new WorldEntityListener();

    public static WorldEntityListener getInstance() {
        return INSTANCE;
    }

    private WorldEntityListener() {

    }

    @Override
    public void a(BlockPosition blockPosition) {
        // Do nothing.
    }

    @Override
    public void b(BlockPosition blockPosition) {
        // Do nothing.
    }

    @Override
    public void a(int i, int i1, int i2, int i3, int i4, int i5) {
        // Do nothing.
    }

    @Override
    public void a(String s, double v, double v1, double v2, float v3, float v4) {
        // Do nothing.
    }

    @Override
    public void a(EntityHuman entityHuman, String s, double v, double v1, double v2, float v3, float v4) {
        // Do nothing.
    }

    @Override
    public void a(int i, boolean b, double v, double v1, double v2, double v3, double v4, double v5, int... ints) {
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
    public void a(String s, BlockPosition blockPosition) {
        // Do nothing.
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
