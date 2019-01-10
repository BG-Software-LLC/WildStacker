package xyz.wildseries.wildstacker.api.objects;

import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public interface StackedSpawner extends StackedObject<CreatureSpawner> {

    CreatureSpawner getSpawner();

    EntityType getSpawnedType();

    Location getLocation();

    LivingEntity getLinkedEntity();

    void setLinkedEntity(LivingEntity linkedEntity);

}
