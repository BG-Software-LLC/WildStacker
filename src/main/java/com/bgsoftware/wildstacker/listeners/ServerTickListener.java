package com.bgsoftware.wildstacker.listeners;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.google.common.collect.Sets;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;

@SuppressWarnings("unused")
public final class ServerTickListener implements Listener {

    private static final Set<Runnable> TICK_END_TASKS = Sets.newConcurrentHashSet();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerTickEnd(ServerTickEndEvent e){
        TICK_END_TASKS.forEach(Runnable::run);
        TICK_END_TASKS.clear();
    }

    public static void addTickEndTask(Runnable code){
        TICK_END_TASKS.add(code);
    }

}
