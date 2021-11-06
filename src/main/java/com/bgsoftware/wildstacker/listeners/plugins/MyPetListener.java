package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.google.common.base.Optional;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import de.Keyle.MyPet.api.event.MyPetCallEvent;
import de.Keyle.MyPet.entity.MyPet;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class MyPetListener implements Listener {

    private static Method GET_ENTITY_METHOD = null;

    static {
        try {
            GET_ENTITY_METHOD = MyPet.class.getMethod("getEntity");
        } catch (Throwable ignored) {
        }
    }

    @EventHandler
    public void onMyPetSpawn(MyPetCallEvent e) {
        if (!(e.getMyPet() instanceof MyPet))
            return;

        MyPet myPet = (MyPet) e.getMyPet();

        Executor.sync(() -> {
            Entity entity;

            try {
                //noinspection all
                Optional<MyPetBukkitEntity> optional = (Optional<MyPetBukkitEntity>) GET_ENTITY_METHOD.invoke(myPet);
                entity = optional.orNull();
            } catch (Exception ex) {
                entity = myPet.getEntity().orElse(null);
            }

            if (entity != null && EntityUtils.isStackable(entity))
                WStackedEntity.of(entity).setSpawnCause(SpawnCause.MY_PET);
        }, 1L);
    }

}
