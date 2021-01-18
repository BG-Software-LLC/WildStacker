package com.bgsoftware.wildstacker.utils.events;

import com.bgsoftware.wildstacker.api.events.BarrelDropEvent;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceEvent;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildstacker.api.events.DuplicateSpawnEvent;
import com.bgsoftware.wildstacker.api.events.EntityStackEvent;
import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import com.bgsoftware.wildstacker.api.events.ItemStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerDropEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackedEntitySpawnEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUpgradeEvent;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import org.bukkit.Bukkit;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class EventsCaller {

    private EventsCaller(){

    }

    public static ItemStack callBarrelDropEvent(StackedBarrel stackedBarrel, Player player, int amount){
        BarrelDropEvent barrelDropEvent = new BarrelDropEvent(stackedBarrel, player, stackedBarrel.getBarrelItem(amount));
        Bukkit.getPluginManager().callEvent(barrelDropEvent);
        return barrelDropEvent.getItemStack();
    }

    public static boolean callBarrelPlaceEvent(Player player, StackedBarrel stackedBarrel, ItemStack inHand){
        BarrelPlaceEvent barrelPlaceEvent = new BarrelPlaceEvent(player, stackedBarrel, inHand);
        Bukkit.getPluginManager().callEvent(barrelPlaceEvent);
        return !barrelPlaceEvent.isCancelled();
    }

    public static boolean callBarrelPlaceInventoryEvent(Player player, StackedBarrel stackedBarrel, int increaseAmount){
        BarrelPlaceInventoryEvent barrelPlaceInventoryEvent = new BarrelPlaceInventoryEvent(player, stackedBarrel, increaseAmount);
        Bukkit.getPluginManager().callEvent(barrelPlaceInventoryEvent);
        return !barrelPlaceInventoryEvent.isCancelled();
    }

    public static boolean callBarrelStackEvent(StackedBarrel barrel, StackedBarrel target){
        BarrelStackEvent barrelStackEvent = new BarrelStackEvent(barrel, target);
        Bukkit.getPluginManager().callEvent(barrelStackEvent);
        return !barrelStackEvent.isCancelled();
    }

    public static boolean callBarrelUnstackEvent(StackedBarrel barrel, Entity unstackSource, int unstackAmount){
        BarrelUnstackEvent barrelUnstackEvent = new BarrelUnstackEvent(barrel, unstackSource, unstackAmount);
        Bukkit.getPluginManager().callEvent(barrelUnstackEvent);
        return !barrelUnstackEvent.isCancelled();
    }

    public static void callDuplicateSpawnEvent(StackedEntity stackedEntity, StackedEntity duplicate){
        DuplicateSpawnEvent duplicateSpawnEvent = new DuplicateSpawnEvent(stackedEntity, duplicate);
        Bukkit.getPluginManager().callEvent(duplicateSpawnEvent);
    }

    public static boolean callEntityStackEvent(StackedEntity entity, StackedEntity target){
        EntityStackEvent entityStackEvent = new EntityStackEvent(entity, target);
        Bukkit.getPluginManager().callEvent(entityStackEvent);
        return !entityStackEvent.isCancelled();
    }

    public static Pair<Boolean, Integer> callEntityUnstackEvent(StackedEntity entity, Entity unstackSource, int unstackAmount){
        EntityUnstackEvent entityUnstackEvent = new EntityUnstackEvent(entity, unstackSource, unstackAmount);
        Bukkit.getPluginManager().callEvent(entityUnstackEvent);
        return new Pair<>(!entityUnstackEvent.isCancelled(), entityUnstackEvent.getAmount());
    }

    public static boolean callItemStackEvent(StackedItem item, StackedItem target){
        ItemStackEvent itemStackEvent = new ItemStackEvent(item, target);
        Bukkit.getPluginManager().callEvent(itemStackEvent);
        return !itemStackEvent.isCancelled();
    }

    public static ItemStack callSpawnerDropEvent(StackedSpawner stackedSpawner, Player player, int amount){
        SpawnerDropEvent spawnerDropEvent = new SpawnerDropEvent(stackedSpawner, player, stackedSpawner.getDropItem(amount));
        Bukkit.getPluginManager().callEvent(spawnerDropEvent);
        return spawnerDropEvent.getItemStack();
    }

    public static boolean callSpawnerPlaceEvent(Player player, StackedSpawner stackedSpawner, ItemStack inHand){
        SpawnerPlaceEvent spawnerPlaceEvent = new SpawnerPlaceEvent(player, stackedSpawner, inHand);
        Bukkit.getPluginManager().callEvent(spawnerPlaceEvent);
        return !spawnerPlaceEvent.isCancelled();
    }

    public static boolean callSpawnerPlaceInventoryEvent(Player player, StackedSpawner stackedSpawner, int increaseAmount){
        SpawnerPlaceInventoryEvent spawnerPlaceInventoryEvent = new SpawnerPlaceInventoryEvent(player, stackedSpawner, increaseAmount);
        return !spawnerPlaceInventoryEvent.isCancelled();
    }

    public static boolean callSpawnerStackEvent(StackedSpawner spawner, StackedSpawner target){
        SpawnerStackEvent spawnerStackEvent = new SpawnerStackEvent(spawner, target);
        Bukkit.getPluginManager().callEvent(spawnerStackEvent);
        return !spawnerStackEvent.isCancelled();
    }

    public static boolean callSpawnerUnstackEvent(StackedSpawner spawner, Entity unstackSource, int unstackAmount){
        SpawnerUnstackEvent spawnerUnstackEvent = new SpawnerUnstackEvent(spawner, unstackSource, unstackAmount);
        Bukkit.getPluginManager().callEvent(spawnerUnstackEvent);
        return !spawnerUnstackEvent.isCancelled();
    }

    public static void callSpawnerUpgradeEvent(StackedSpawner stackedSpawner, SpawnerUpgrade spawnerUpgrade){
        SpawnerUpgradeEvent spawnerUpgradeEvent = new SpawnerUpgradeEvent(stackedSpawner, spawnerUpgrade);
        Bukkit.getPluginManager().callEvent(spawnerUpgradeEvent);
    }

    public static boolean callSpawnerStackedEntitySpawnEvent(CreatureSpawner creatureSpawner){
        SpawnerStackedEntitySpawnEvent spawnerStackedEntitySpawnEvent = new SpawnerStackedEntitySpawnEvent(creatureSpawner);
        Bukkit.getPluginManager().callEvent(spawnerStackedEntitySpawnEvent);
        return spawnerStackedEntitySpawnEvent.shouldBeStacked();
    }

}
