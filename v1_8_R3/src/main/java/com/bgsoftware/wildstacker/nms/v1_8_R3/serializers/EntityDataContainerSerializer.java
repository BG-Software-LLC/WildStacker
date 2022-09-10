package com.bgsoftware.wildstacker.nms.v1_8_R3.serializers;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.hooks.IDataSerializer;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;

public class EntityDataContainerSerializer implements IDataSerializer {

    private static final ReflectField<NBTTagCompound> ENTITY_DATA_CONTAINER = new ReflectField<>(Entity.class, NBTTagCompound.class, "entityDataContainerTag");

    public static boolean isValid() {
        return ENTITY_DATA_CONTAINER.isValid();
    }

    public EntityDataContainerSerializer() {
        WildStackerPlugin.log("- Using EntityDataContainer to store entity data.");
    }

    @Override
    public void saveEntity(StackedEntity stackedEntity) {
        Entity entity = ((CraftEntity) stackedEntity.getLivingEntity()).getHandle();

        NBTTagCompound entityDataContainer = ENTITY_DATA_CONTAINER.get(entity);

        if (entityDataContainer == null) {
            entityDataContainer = new NBTTagCompound();
            ENTITY_DATA_CONTAINER.set(entity, entityDataContainer);
        }

        entityDataContainer.setInt("ws:stack-amount", stackedEntity.getStackAmount());
        entityDataContainer.setString("ws:stack-cause", stackedEntity.getSpawnCause().name());
        if (stackedEntity.hasNameTag())
            entityDataContainer.setBoolean("ws:name-tag", true);
    }

    @Override
    public void loadEntity(StackedEntity stackedEntity) {
        Entity entity = ((CraftEntity) stackedEntity.getLivingEntity()).getHandle();

        NBTTagCompound entityDataContainer = ENTITY_DATA_CONTAINER.get(entity);

        if (entityDataContainer == null)
            return;

        if (entityDataContainer.hasKey("ws:stack-amount"))
            stackedEntity.setStackAmount(entityDataContainer.getInt("ws:stack-amount"), false);
        if (entityDataContainer.hasKey("ws:stack-cause"))
            stackedEntity.setSpawnCause(SpawnCause.valueOf(entityDataContainer.getString("ws:stack-cause")));
        if (entityDataContainer.hasKey("ws:name-tag"))
            ((WStackedEntity) stackedEntity).setNameTag();
    }

    @Override
    public void saveItem(StackedItem stackedItem) {
        Entity entity = ((CraftEntity) stackedItem.getItem()).getHandle();

        NBTTagCompound entityDataContainer = ENTITY_DATA_CONTAINER.get(entity);

        if (entityDataContainer == null) {
            entityDataContainer = new NBTTagCompound();
            ENTITY_DATA_CONTAINER.set(entity, entityDataContainer);
        }

        entityDataContainer.setInt("ws:stack-amount", stackedItem.getStackAmount());
    }

    @Override
    public void loadItem(StackedItem stackedItem) {
        Entity entity = ((CraftEntity) stackedItem.getItem()).getHandle();

        NBTTagCompound entityDataContainer = ENTITY_DATA_CONTAINER.get(entity);

        if (entityDataContainer == null)
            return;

        if (entityDataContainer.hasKey("ws:stack-amount"))
            stackedItem.setStackAmount(entityDataContainer.getInt("ws:stack-amount"), false);
    }
}
