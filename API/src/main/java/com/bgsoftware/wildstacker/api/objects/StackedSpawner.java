package com.bgsoftware.wildstacker.api.objects;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface StackedSpawner extends StackedObject<CreatureSpawner> {

    /**
     * Get the creature-spawner object of bukkit.
     */
    CreatureSpawner getSpawner();

    /**
     * Get the spawned-type of the creature-spawner.
     */
    EntityType getSpawnedType();

    /**
     * Get the entity that is linked into this spawner.
     * May be null if there is no linked entity or it's too far away.
     */
    LivingEntity getLinkedEntity();

    /**
     * Set an entity to be linked into this spawner.
     * @param linkedEntity an entity to link
     */
    void setLinkedEntity(LivingEntity linkedEntity);

    /**
     * Get all the nearby stacked-spawners that the spawner can stack into in the merge range.
     */
    List<StackedSpawner> getNearbySpawners();

    /**
     * Get the drop item of the spawner.
     */
    ItemStack getDropItem();

}
