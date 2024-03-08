package com.bgsoftware.wildstacker.nms.v1_18.spawner;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.lang.ref.WeakReference;

public class SpawnerWatcherTickingBlockEntity implements TickingBlockEntity {

    private static final String CLASS_NAME = SpawnerWatcherTickingBlockEntity.class.getSimpleName();
    private static final int CHECK_INTERVAL = 20;

    private final WeakReference<StackedSpawner> stackedSpawner;
    private final SpawnerBlockEntity spawnerBlockEntity;
    private final TickingBlockEntity original;
    private final String type;

    private int currentTick = CHECK_INTERVAL;

    public SpawnerWatcherTickingBlockEntity(StackedSpawner stackedSpawner,
                                            SpawnerBlockEntity spawnerBlockEntity,
                                            TickingBlockEntity original) {
        this.stackedSpawner = new WeakReference<>(stackedSpawner);
        this.spawnerBlockEntity = spawnerBlockEntity;
        this.original = original;
        this.type = CLASS_NAME + " watching " + original.getType();
    }

    @Override
    public void tick() {
        this.original.tick();

        StackedSpawner stackedSpawner = this.stackedSpawner.get();
        if (stackedSpawner == null)
            return;

        if (currentTick++ < CHECK_INTERVAL)
            return;

        currentTick = 0;

        BaseSpawner baseSpawner = this.spawnerBlockEntity.getSpawner();
        if (!(baseSpawner instanceof StackedBaseSpawner)) {
            StackedBaseSpawner stackedBaseSpawner = new StackedBaseSpawner(spawnerBlockEntity, stackedSpawner);
            if (this.spawnerBlockEntity.getSpawner() != stackedBaseSpawner)
                throw new RuntimeException("Failed to update spawner");
        }
    }

    @Override
    public boolean isRemoved() {
        StackedSpawner stackedSpawner = this.stackedSpawner.get();
        return stackedSpawner == null || this.spawnerBlockEntity.isRemoved();
    }

    @Override
    public BlockPos getPos() {
        return this.spawnerBlockEntity.getBlockPos();
    }

    @Override
    public String getType() {
        return this.type;
    }

}
