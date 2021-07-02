package com.bgsoftware.wildstacker.listeners.plugins;

import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import me.hexedhero.pp.api.PinataSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class PinataPartyListener implements Listener {

    @EventHandler
    public void onPinataSpawn(PinataSpawnEvent e){
        StackedEntity stackedEntity = WStackedEntity.of(e.getPinata().getEntity());
        stackedEntity.setFlag(EntityFlag.AVOID_ONE_SHOT, true);
    }

}
