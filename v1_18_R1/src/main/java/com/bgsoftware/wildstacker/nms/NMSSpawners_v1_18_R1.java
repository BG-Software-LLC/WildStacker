package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;

@SuppressWarnings("unused")
public final class NMSSpawners_v1_18_R1 implements NMSSpawners {

    @Override
    public boolean updateStackedSpawner(StackedSpawner stackedSpawner) {
        return true;
    }

    @Override
    public void registerSpawnConditions() {
    }

}
