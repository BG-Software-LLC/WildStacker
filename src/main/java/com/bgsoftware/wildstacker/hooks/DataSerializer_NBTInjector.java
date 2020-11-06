package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtinjector.NBTInjector;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.lang.reflect.Field;

public final class DataSerializer_NBTInjector implements IDataSerializer, Listener {

    private static Field ENTITY_FIELD = null;

    static {
        try {
            ENTITY_FIELD = EntityEvent.class.getDeclaredField("entity");
            ENTITY_FIELD.setAccessible(true);
        }catch (Throwable ignored){}
    }

    public static void register(WildStackerPlugin plugin){
        if(NBTInjector.isInjected())
            plugin.getSystemManager().setDataSerializer(new DataSerializer_NBTInjector(plugin));
    }

    private DataSerializer_NBTInjector(WildStackerPlugin plugin){
        Bukkit.getPluginManager().registerEvents(this, plugin);
        WildStackerPlugin.log("- Using NBTInjector to store entity data.");
    }

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        NBTCompound nbtCompound = NBTInjector.getNbtData(stackedEntity.getLivingEntity());
        nbtCompound.setInteger("ws:stack-amount", stackedEntity.getStackAmount());
        nbtCompound.setString("ws:stack-cause", stackedEntity.getSpawnCause().name());
        if(stackedEntity.hasNameTag())
            nbtCompound.setBoolean("ws:name-tag", true);
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        NBTCompound nbtCompound = NBTInjector.getNbtData(stackedEntity.getLivingEntity());
        if(nbtCompound.hasKey("ws:stack-amount"))
            stackedEntity.setStackAmount(nbtCompound.getInteger("ws:stack-amount"), false);
        if(nbtCompound.hasKey("ws:stack-cause"))
            stackedEntity.setSpawnCause(SpawnCause.valueOf(nbtCompound.getString("ws:stack-cause")));
        if(nbtCompound.hasKey("ws:name-tag"))
            ((WStackedEntity) stackedEntity).setNameTag();
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        NBTCompound nbtCompound = NBTInjector.getNbtData(stackedItem.getItem());
        nbtCompound.setInteger("ws:stack-amount", stackedItem.getStackAmount());
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        NBTCompound nbtCompound = NBTInjector.getNbtData(stackedItem.getItem());
        if(nbtCompound.hasKey("ws:stack-amount"))
            stackedItem.setStackAmount(nbtCompound.getInteger("ws:stack-amount"), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent e){
        if(ENTITY_FIELD != null){
            try {
                ENTITY_FIELD.set(e, NBTInjector.patchEntity(e.getEntity()));
            }catch (Exception ignored){}
        }
    }

}
