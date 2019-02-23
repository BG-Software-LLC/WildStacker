package com.bgsoftware.wildstacker.api.handlers;

import com.bgsoftware.wildstacker.api.loot.LootTable;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface SystemManager {

    void removeStackObject(StackedObject stackedObject);

    StackedEntity getStackedEntity(LivingEntity livingEntity);

    StackedItem getStackedItem(Item item);

    StackedSpawner getStackedSpawner(CreatureSpawner spawner);

    StackedBarrel getStackedBarrel(Block block);

    List<StackedEntity> getStackedEntities();

    List<StackedItem> getStackedItems();

    List<StackedSpawner> getStackedSpawners();

    List<StackedBarrel> getStackedBarrels();

    boolean isStackedSpawner(Block block);

    boolean isStackedBarrel(Block block);

    void performCacheClear();

    void updateLinkedEntity(LivingEntity livingEntity, LivingEntity newLivingEntity);

    <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type);

    <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, CreatureSpawnEvent.SpawnReason spawnReason);

    void performKillAll();

    LootTable getLootTable(LivingEntity livingEntity);

}
