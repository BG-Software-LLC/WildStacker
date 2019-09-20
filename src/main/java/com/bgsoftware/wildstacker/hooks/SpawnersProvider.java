package com.bgsoftware.wildstacker.hooks;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface SpawnersProvider {

    ItemStack getSpawnerItem(CreatureSpawner spawner, int amount);

    void dropOrGiveItem(Entity entity, CreatureSpawner spawner, int amount, UUID explodeSource);

    void dropOrGiveItem(Player player, CreatureSpawner spawner, int amount, boolean isExplodeSource);

    void setSpawnerType(CreatureSpawner spawner, ItemStack itemStack, boolean updateName);

    EntityType getSpawnerType(ItemStack itemStack);

}
