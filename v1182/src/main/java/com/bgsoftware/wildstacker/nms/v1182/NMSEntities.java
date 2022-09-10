package com.bgsoftware.wildstacker.nms.v1182;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity;

import java.util.Map;
import java.util.Objects;

public final class NMSEntities implements com.bgsoftware.wildstacker.nms.NMSEntities {

    @Override
    public boolean checkEntityAttributes(org.bukkit.entity.LivingEntity bukkitEntity, Map<String, Object> attributes) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitEntity).getHandle();
        CompoundTag entityCompound = new CompoundTag();
        livingEntity.addAdditionalSaveData(entityCompound);

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            Tag tag = entityCompound.get(attribute.getKey());
            if (tag instanceof NumericTag) {
                if (!Objects.equals(attribute.getValue(), ((NumericTag) tag).getAsNumber()))
                    return false;
            } else if (tag instanceof StringTag) {
                if (!Objects.equals(attribute.getValue(), tag.getAsString()))
                    return false;
            } else {
                return false;
            }
        }

        return true;
    }

}
