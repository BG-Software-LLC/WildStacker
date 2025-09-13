package com.bgsoftware.wildstacker.hooks;

import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import org.bukkit.entity.Entity;

public class EntityTypeProvider_MyPet implements EntityTypeProvider {

    @Override
    public String checkStackEntity(Entity entity) {
        return entity instanceof MyPetBukkitEntity ? "Cannot get a stacked entity from a MyPet." : null;
    }

}
