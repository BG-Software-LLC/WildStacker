package com.bgsoftware.wildstacker.hooks;

import de.Keyle.MyPet.MyPetApi;
import org.bukkit.entity.Entity;

public class EntityTypeProvider_MyPet4 implements EntityTypeProvider {

    @Override
    public String checkStackEntity(Entity entity) {
        return MyPetApi.getPetManager().getPetFromEntity(entity) != null ?
                "Cannot get a stacked entity from a MyPet." : null;
    }

}
