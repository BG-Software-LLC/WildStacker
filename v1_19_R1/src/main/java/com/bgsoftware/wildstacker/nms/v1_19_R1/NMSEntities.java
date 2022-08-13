package com.bgsoftware.wildstacker.nms.v1_19_R1;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.wildstacker.nms.v1_19_R1.mappings.net.minecraft.world.entity.Entity;
import com.bgsoftware.wildstacker.utils.entity.StackCheck;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTNumber;
import net.minecraft.nbt.NBTTagString;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Frog;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.Objects;

public final class NMSEntities implements com.bgsoftware.wildstacker.nms.NMSEntities {

    @Override
    public StackCheckResult areSimilar(EntityTypes entityType, LivingEntity en1, LivingEntity en2) {
        if (StackCheck.FROG_TOUNGE_TARGET.isEnabled() && StackCheck.FROG_TOUNGE_TARGET.isTypeAllowed(entityType)) {
            if (!Objects.equals(((Frog) en1).getTongueTarget(), ((Frog) en2).getTongueTarget()))
                return StackCheckResult.FROG_TOUNGE_TARGET;
        }

        if (StackCheck.FROG_TYPE.isEnabled() && StackCheck.FROG_TYPE.isTypeAllowed(entityType)) {
            if (((Frog) en1).getVariant() != ((Frog) en2).getVariant())
                return StackCheckResult.FROG_TYPE;
        }

        return StackCheckResult.SUCCESS;
    }

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
