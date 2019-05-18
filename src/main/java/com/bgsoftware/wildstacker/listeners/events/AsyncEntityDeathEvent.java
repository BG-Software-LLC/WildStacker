package com.bgsoftware.wildstacker.listeners.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.List;

public class AsyncEntityDeathEvent extends EntityDeathEvent {

    public AsyncEntityDeathEvent(LivingEntity what, List<ItemStack> drops, int droppedExp){
        super(what, drops, droppedExp);
        try{
            Field asyncField = Event.class.getDeclaredField("async");
            asyncField.setAccessible(true);
            asyncField.set(this, true);
            asyncField.setAccessible(false);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
