package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import me.hexedhero.pp.api.PinataSpawnEvent;
import org.bukkit.entity.Llama;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PinataPartyHook {

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void onPinataSpawn(PinataSpawnEvent e) {
                // The pinata is actually spawned later, therefore there must be a delay.
                Executor.sync(() -> {
                    Llama llama = e.getPinata().getEntity();
                    if (llama != null) {
                        StackedEntity stackedEntity = WStackedEntity.of(llama);
                        stackedEntity.setFlag(EntityFlag.AVOID_ONE_SHOT, true);
                    }
                }, 20L);
            }
        }, plugin);
    }

}
