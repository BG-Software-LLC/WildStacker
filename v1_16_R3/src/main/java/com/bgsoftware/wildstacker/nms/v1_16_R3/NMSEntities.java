package com.bgsoftware.wildstacker.nms.v1_16_R3;


import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTNumber;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagString;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
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
            if (nbtBase instanceof NBTNumber) {
                if (!Objects.equals(attribute.getValue(), ((NBTNumber) nbtBase).k()))
                    return false;
            } else if (nbtBase instanceof NBTTagString) {
                if (!Objects.equals(attribute.getValue(), nbtBase.asString()))
                    return false;
            } else {
                return false;
            }
        }

        return true;
    }

}
