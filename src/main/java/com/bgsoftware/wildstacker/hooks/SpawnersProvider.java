package com.bgsoftware.wildstacker.hooks;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface SpawnersProvider {

    ItemStack getSpawnerItem(CreatureSpawner spawner, int amount);

    void dropOrGiveItem(Entity entity, CreatureSpawner spawner, int amount);

    void dropOrGiveItem(Player player, CreatureSpawner spawner, int amount);

    void setSpawnerType(CreatureSpawner spawner, ItemStack itemStack, boolean updateName);

    EntityType getSpawnerType(ItemStack itemStack);

}
