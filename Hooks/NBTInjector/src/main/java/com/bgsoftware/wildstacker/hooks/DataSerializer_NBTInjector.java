package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.stacker.entities.WStackedEntity;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtinjector.NBTInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

@SuppressWarnings("unused")
public final class DataSerializer_NBTInjector implements IDataSerializer, Listener {

    private static final ReflectField<Entity> ENTITY_FIELD = new ReflectField<>(EntityEvent.class, Entity.class, "entity");

    public DataSerializer_NBTInjector(WildStackerPlugin plugin) {
        if (NBTInjector.isInjected()) {
            plugin.getSystemManager().setDataSerializer(this);
            Bukkit.getPluginManager().registerEvents(this, plugin);
            WildStackerPlugin.log("- Using NBTInjector to store entity data.");
        }
    }

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        NBTCompound nbtCompound = NBTInjector.getNbtData(stackedEntity.getLivingEntity());
        nbtCompound.setInteger("ws:stack-amount", stackedEntity.getStackAmount());
        nbtCompound.setString("ws:stack-cause", stackedEntity.getSpawnCause().name());
        if (stackedEntity.hasNameTag())
            nbtCompound.setBoolean("ws:name-tag", true);
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        NBTCompound nbtCompound = NBTInjector.getNbtData(stackedEntity.getLivingEntity());
        if (nbtCompound.hasKey("ws:stack-amount"))
            stackedEntity.setStackAmount(nbtCompound.getInteger("ws:stack-amount"), false);
        if (nbtCompound.hasKey("ws:stack-cause"))
            stackedEntity.setSpawnCause(SpawnCause.valueOf(nbtCompound.getString("ws:stack-cause")));
        if (nbtCompound.hasKey("ws:name-tag"))
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
        if (nbtCompound.hasKey("ws:stack-amount"))
            stackedItem.setStackAmount(nbtCompound.getInteger("ws:stack-amount"), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if(ENTITY_FIELD.isValid())
            ENTITY_FIELD.set(event, NBTInjector.patchEntity(event.getEntity()));
    }

}
