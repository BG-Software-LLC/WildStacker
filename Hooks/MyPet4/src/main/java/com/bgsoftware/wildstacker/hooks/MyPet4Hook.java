package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import de.Keyle.MyPet.api.event.PetCallEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class MyPet4Hook {

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onMyPetSpawn(PetCallEvent e) {
                Entity entity = e.getPet().getBukkitEntity();

                Executor.sync(() -> {
                    if (EntityUtils.isStackable(entity))
                        WStackedEntity.of(entity).setSpawnCause(SpawnCause.MY_PET);
                }, 1L);
            }
        }, plugin);
    }

}
