package com.bgsoftware.wildstacker.utils.entity;

import java.util.Optional;

public final class SlimeSplitTracker {

    private int originalSlimeStackAmount;
    private int slimesToSpawn;

    public SlimeSplitTracker() {
    }

    public void startTracking(int originalSlimeStackAmount, int slimesToSpawn) {
        this.originalSlimeStackAmount = originalSlimeStackAmount;
        this.slimesToSpawn = slimesToSpawn;
    }

    public void resetTracker() {
        if(slimesToSpawn > 0) {
            originalSlimeStackAmount = 0;
        }
    }

    public Optional<Integer> getOriginalSlimeStackAmount() {
        return Optional.ofNullable(originalSlimeStackAmount <= 0 ? null : originalSlimeStackAmount);
    }

    public void decreaseSpawnCount() {
        if (--this.slimesToSpawn == 0)
            this.originalSlimeStackAmount = 0;
    }

}
