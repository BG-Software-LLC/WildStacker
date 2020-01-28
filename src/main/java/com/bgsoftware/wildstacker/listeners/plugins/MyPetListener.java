package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import de.Keyle.MyPet.api.event.MyPetCallEvent;
import de.Keyle.MyPet.entity.MyPet;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class MyPetListener implements Listener {

    @EventHandler
    public void onMyPetSpawn(MyPetCallEvent e){
        if(!(e.getMyPet() instanceof MyPet))
            return;

        MyPet myPet = (MyPet) e.getMyPet();

        Executor.sync(() -> {
            Entity entity = myPet.getEntity().orElse(null);
            if(entity != null && EntityUtils.isStackable(entity))
                WStackedEntity.of(entity).setSpawnCause(SpawnCause.MY_PET);
        }, 1L);
    }

}
