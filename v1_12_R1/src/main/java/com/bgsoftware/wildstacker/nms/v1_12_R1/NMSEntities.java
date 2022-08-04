package com.bgsoftware.wildstacker.nms.v1_12_R1;


import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagByte;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagDouble;
import net.minecraft.server.v1_12_R1.NBTTagFloat;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagLong;
import net.minecraft.server.v1_12_R1.NBTTagShort;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.Objects;

public final class NMSEntities implements com.bgsoftware.wildstacker.nms.NMSEntities {

    @Override
    public boolean checkEntityAttributes(LivingEntity bukkitEntity, Map<String, Object> attributes) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound entityCompound = entity.save(new NBTTagCompound());

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            NBTBase nbtBase = entityCompound.get(attribute.getKey());
            Object attributeValue;

            if (nbtBase instanceof NBTTagByte) {
                attributeValue = ((NBTTagByte) nbtBase).g();
            } else if (nbtBase instanceof NBTTagDouble) {
                attributeValue = ((NBTTagDouble) nbtBase).asDouble();
            } else if (nbtBase instanceof NBTTagFloat) {
                attributeValue = ((NBTTagFloat) nbtBase).i();
            } else if (nbtBase instanceof NBTTagInt) {
                attributeValue = ((NBTTagInt) nbtBase).e();
            } else if (nbtBase instanceof NBTTagLong) {
                attributeValue = ((NBTTagLong) nbtBase).d();
            } else if (nbtBase instanceof NBTTagShort) {
                attributeValue = ((NBTTagShort) nbtBase).f();
            } else if (nbtBase instanceof NBTTagString) {
                attributeValue = ((NBTTagString) nbtBase).c_();
            } else {
                return false;       // Value not allowed
            }

            if (!Objects.equals(attributeValue, attribute.getValue()))
                return false;
        }

        return true;
    }

}
