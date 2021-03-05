package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.objects.StackedSpawner;

public interface NMSSpawners {

    boolean updateStackedSpawner(StackedSpawner stackedSpawner);

    void registerSpawnConditions();

}
