package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;

@SuppressWarnings("unused")
public final class NMSSpawners_v1_17_R1 implements NMSSpawners {

    @Override
    public boolean updateStackedSpawner(StackedSpawner stackedSpawner) {
        throw new UnsupportedOperationException("Not supported in 1.17.");
    }

    @Override
    public void registerSpawnConditions() {
        throw new UnsupportedOperationException("Not supported in 1.17.");
    }

}
