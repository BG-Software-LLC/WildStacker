package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.hooks.listeners.IEntityDeathListener;
import org.bukkit.Bukkit;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
public final class JobsHook {

    private static final String SPAWNER_KEY = "jobsMobSpawner";

    private static Plugin jobs;

    public static void register(WildStackerPlugin plugin) {
        jobs = Bukkit.getPluginManager().getPlugin("Jobs");
        plugin.getProviders().registerEntityDeathListener(JobsHook::handleDeath);
    }

    private static void handleDeath(StackedEntity stackedEntity, IEntityDeathListener.Type type) {
        if(type != IEntityDeathListener.Type.AFTER_DEATH_EVENT)
            return;

        SpawnCause spawnCause = stackedEntity.getSpawnCause();
        if(spawnCause == SpawnCause.SPAWNER || spawnCause == SpawnCause.SPAWNER_EGG)
            stackedEntity.getLivingEntity().setMetadata(SPAWNER_KEY, new FixedMetadataValue(jobs, true));
    }

}
