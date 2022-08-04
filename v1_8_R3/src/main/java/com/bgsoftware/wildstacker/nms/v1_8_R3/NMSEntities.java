package com.bgsoftware.wildstacker.nms.v1_8_R3;


import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagFloat;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagLong;
import net.minecraft.server.v1_8_R3.NBTTagShort;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.Objects;

public final class NMSEntities implements com.bgsoftware.wildstacker.nms.NMSEntities {

    @Override
    public boolean checkEntityAttributes(LivingEntity bukkitEntity, Map<String, Object> attributes) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound entityCompound = new NBTTagCompound();
        entity.e(entityCompound);

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            NBTBase nbtBase = entityCompound.get(attribute.getKey());
            Object attributeValue;

            if (nbtBase instanceof NBTTagByte) {
                attributeValue = ((NBTTagByte) nbtBase).f();
            } else if (nbtBase instanceof NBTTagDouble) {
                attributeValue = ((NBTTagDouble) nbtBase).g();
            } else if (nbtBase instanceof NBTTagFloat) {
                attributeValue = ((NBTTagFloat) nbtBase).h();
            } else if (nbtBase instanceof NBTTagInt) {
                attributeValue = ((NBTTagInt) nbtBase).d();
            } else if (nbtBase instanceof NBTTagLong) {
                attributeValue = ((NBTTagLong) nbtBase).c();
            } else if (nbtBase instanceof NBTTagShort) {
                attributeValue = ((NBTTagShort) nbtBase).e();
            } else if (nbtBase instanceof NBTTagString) {
                attributeValue = ((NBTTagString) nbtBase).a_();
            } else {
                return false;       // Value not allowed
            }

            if (!Objects.equals(attributeValue, attribute.getValue()))
                return false;
        }

        return true;
    }

}
