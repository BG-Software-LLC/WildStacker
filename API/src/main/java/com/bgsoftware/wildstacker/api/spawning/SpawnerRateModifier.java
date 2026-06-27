package com.bgsoftware.wildstacker.api.spawning;

import com.bgsoftware.wildstacker.api.WildStackerAPI;

import java.util.Objects;

public abstract class SpawnerRateModifier {

    private final String id;

    protected SpawnerRateModifier(String id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public static SpawnerRateModifier register(SpawnerRateModifier modifier) {
        return WildStackerAPI.getWildStacker().getSystemManager().registerSpawnerRateModifier(modifier);
    }

    public final String getId() {
        return id;
    }

    public abstract double getMultiplier(SpawnerRateContext context);

}
