package com.bgsoftware.wildstacker.utils.entity;

import java.util.Optional;

public final class FutureEntityTracker {

    private int originalStackAmount;
    private int entitiesToSpawn;

    public FutureEntityTracker() {
    }

    public void startTracking(int originalStackAmount, int entitiesToSpawn) {
        this.originalStackAmount = originalStackAmount;
        this.entitiesToSpawn = entitiesToSpawn;
    }

    public void resetTracker() {
        if(entitiesToSpawn > 0) {
            originalStackAmount = 0;
        }
    }

    public Optional<Integer> getOriginalStackAmount() {
        return Optional.ofNullable(originalStackAmount <= 0 ? null : originalStackAmount);
    }

    public void decreaseSpawnCount() {
        if (--this.entitiesToSpawn == 0)
            this.originalStackAmount = 0;
    }

}
