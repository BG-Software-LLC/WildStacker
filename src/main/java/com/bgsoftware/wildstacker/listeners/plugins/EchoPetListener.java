package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.dsh105.echopet.compat.api.event.PetPreSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class EchoPetListener implements Listener {

    @EventHandler
    public void onEchoPetSpawn(PetPreSpawnEvent e){
        LivingEntity livingEntity = e.getPet().getEntityPet().getBukkitEntity();
        if(EntityUtils.isStackable(livingEntity))
            WStackedEntity.of(livingEntity).setSpawnCause(SpawnCause.ECHO_PET);
    }

}
