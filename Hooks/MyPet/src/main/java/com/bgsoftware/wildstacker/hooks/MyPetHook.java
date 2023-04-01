package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.stacker.entities.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import de.Keyle.MyPet.api.event.MyPetCallEvent;
import de.Keyle.MyPet.entity.MyPet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

@SuppressWarnings("unused")
public final class MyPetHook {

    private static final ReflectMethod<Optional<MyPetBukkitEntity>> GET_ENTITY_METHOD =
            new ReflectMethod<>(MyPet.class, Optional.class, "getEntity");

    public static void register(WildStackerPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onMyPetSpawn(MyPetCallEvent e) {
                if (!(e.getMyPet() instanceof MyPet))
                    return;

                MyPet myPet = (MyPet) e.getMyPet();

                Executor.sync(() -> {
                    Optional<MyPetBukkitEntity> entityOptional = GET_ENTITY_METHOD.isValid() ?
                            GET_ENTITY_METHOD.invoke(myPet) : myPet.getEntity();

                    entityOptional.ifPresent(entity -> {
                        if (EntityUtils.isStackable(entity))
                            WStackedEntity.of(entity).setSpawnCause(SpawnCause.MY_PET);
                    });
                }, 1L);
            }
        }, plugin);
    }

}
