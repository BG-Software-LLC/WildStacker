package com.bgsoftware.wildstacker.nms.v1_18_R1;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.wildstacker.nms.v1_18_R1.mappings.net.minecraft.world.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTNumber;
import net.minecraft.nbt.NBTTagString;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.Objects;

public final class NMSEntities implements com.bgsoftware.wildstacker.nms.NMSEntities {

    @Remap(classPath = "net.minecraft.nbt.NumericTag", name = "getAsNumber", type = Remap.Type.METHOD, remappedName = "k")
    @Remap(classPath = "net.minecraft.nbt.Tag", name = "getAsString", type = Remap.Type.METHOD, remappedName = "e_")
    @Override
    public boolean checkEntityAttributes(LivingEntity bukkitEntity, Map<String, Object> attributes) {
        Entity entity = new Entity(((CraftEntity) bukkitEntity).getHandle());
        NBTTagCompound entityCompound = new NBTTagCompound();
        entity.addAdditionalSaveData(entityCompound.getHandle());

        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            NBTBase nbtBase = entityCompound.get(attribute.getKey());
            if (nbtBase instanceof NBTNumber) {
                if (!Objects.equals(attribute.getValue(), ((NBTNumber) nbtBase).k()))
                    return false;
            } else if (nbtBase instanceof NBTTagString) {
                if (!Objects.equals(attribute.getValue(), nbtBase.e_()))
                    return false;
            } else {
                return false;
            }
        }

        return true;
    }

}
