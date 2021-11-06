package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public final class JobsHook {

    private static final String SPAWNER_KEY = "jobsMobSpawner";

    private static Plugin jobs;

    public static void updateSpawnReason(LivingEntity livingEntity, SpawnCause spawnCause) {
        if (jobs != null && (spawnCause == SpawnCause.SPAWNER || spawnCause == SpawnCause.SPAWNER_EGG)) {
            livingEntity.setMetadata(SPAWNER_KEY, new FixedMetadataValue(jobs, true));
        }
    }

    public static void setEnabled(boolean enabled) {
        jobs = enabled ? Bukkit.getPluginManager().getPlugin("Jobs") : null;
    }

}
