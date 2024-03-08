package com.bgsoftware.wildstacker.nms.v1_12_R1.spawner;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import net.minecraft.server.v1_12_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_12_R1.TileEntityMobSpawner;

import java.lang.ref.WeakReference;

public class TileEntityMobSpawnerWatcher extends TileEntityMobSpawner {

    private static final int CHECK_INTERVAL = 20;

    private final WeakReference<StackedSpawner> stackedSpawner;
    private final TileEntityMobSpawner mobSpawner;

    private int currentTick = CHECK_INTERVAL;

    public TileEntityMobSpawnerWatcher(StackedSpawner stackedSpawner,
                                       TileEntityMobSpawner mobSpawner) {
        this.stackedSpawner = new WeakReference<>(stackedSpawner);
        this.mobSpawner = mobSpawner;
        this.position = mobSpawner.getPosition();
        this.world = mobSpawner.getWorld();
    }

    @Override
    public void e() {
        StackedSpawner stackedSpawner = this.stackedSpawner.get();
        if (stackedSpawner == null)
            return;

        if (currentTick++ >= CHECK_INTERVAL) {
            currentTick = 0;
            MobSpawnerAbstract mobSpawnerAbstract = this.mobSpawner.getSpawner();
            if (!(mobSpawnerAbstract instanceof StackedMobSpawner)) {
                StackedMobSpawner stackedBaseSpawner = new StackedMobSpawner(this.mobSpawner, stackedSpawner);
                if (this.mobSpawner.getSpawner() != stackedBaseSpawner)
                    throw new RuntimeException("Failed to update spawner");
            }
        }

        this.mobSpawner.e();
    }

}
