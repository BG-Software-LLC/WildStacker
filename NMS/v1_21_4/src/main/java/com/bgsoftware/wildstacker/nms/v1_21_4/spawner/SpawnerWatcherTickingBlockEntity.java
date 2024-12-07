package com.bgsoftware.wildstacker.nms.v1_21_4.spawner;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.bukkit.Location;

import java.lang.ref.WeakReference;

public class SpawnerWatcherTickingBlockEntity implements TickingBlockEntity {

    private static final ReflectMethod<Long> GET_CHUNK_COORD_KEY = new ReflectMethod<>(
            TickingBlockEntity.class, "getChunkCoordinateKey");

    private static final WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private static final String CLASS_NAME = SpawnerWatcherTickingBlockEntity.class.getSimpleName();
    private static final int CHECK_INTERVAL = 20;

    private final SpawnerBlockEntity spawnerBlockEntity;
    private final TickingBlockEntity original;
    private final String type;

    private WeakReference<StackedSpawner> stackedSpawner;
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

        if (currentTick++ < CHECK_INTERVAL)
            return;

        currentTick = 0;

        BaseSpawner baseSpawner = this.spawnerBlockEntity.getSpawner();
        if (!(baseSpawner instanceof StackedBaseSpawner)) {
            StackedSpawner stackedSpawner = this.stackedSpawner.get();

            if (stackedSpawner == null) {
                Location location = new Location(this.spawnerBlockEntity.getLevel().getWorld(),
                        getPos().getX(), getPos().getY(), getPos().getZ());
                if (!plugin.getSystemManager().isStackedSpawner(location))
                    return;
                stackedSpawner = plugin.getSystemManager().getStackedSpawner(location);
                this.stackedSpawner = new WeakReference<>(stackedSpawner);
            }

            StackedBaseSpawner stackedBaseSpawner = new StackedBaseSpawner(spawnerBlockEntity, stackedSpawner);
            if (this.spawnerBlockEntity.getSpawner() != stackedBaseSpawner)
                throw new RuntimeException("Failed to update spawner");
        }
    }

    @Override
    public boolean isRemoved() {
        return this.spawnerBlockEntity.isRemoved();
    }

    @Override
    public BlockPos getPos() {
        return this.spawnerBlockEntity.getBlockPos();
    }

    @Override
    public String getType() {
        return this.type;
    }

    public long getChunkCoordinateKey() {
        return GET_CHUNK_COORD_KEY.invoke(this.original);
    }

}
