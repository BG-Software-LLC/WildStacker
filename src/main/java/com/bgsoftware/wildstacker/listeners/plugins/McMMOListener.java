package com.bgsoftware.wildstacker.listeners.plugins;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.events.party.McMMOPartyXpGainEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class McMMOListener implements Listener {

    @EventHandler
    public void onMcMMOPlayerXpGain(McMMOPlayerXpGainEvent e){
        Bukkit.broadcastMessage(e.getRawXpGained() + "");
    }

    @EventHandler
    public void onMcMMOPlayerXpGain(McMMOPartyXpGainEvent e){
        Bukkit.broadcastMessage(e.getRawXpGained() + "");
    }

}
