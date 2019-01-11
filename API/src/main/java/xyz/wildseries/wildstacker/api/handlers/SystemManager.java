package xyz.wildseries.wildstacker.api.handlers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import xyz.wildseries.wildstacker.api.loot.LootTable;
import xyz.wildseries.wildstacker.api.objects.StackedBarrel;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.api.objects.StackedItem;
import xyz.wildseries.wildstacker.api.objects.StackedObject;
import xyz.wildseries.wildstacker.api.objects.StackedSpawner;

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

    void performKillAll();

    LootTable getLootTable(LivingEntity livingEntity);

}
