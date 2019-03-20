package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"WeakerAccess", "all"})
public final class DataHandler {

    private WildStackerPlugin plugin;

    public final Map<Object, StackedObject> CACHED_OBJECTS = new ConcurrentHashMap<>();

    //Here because we can't get the bukkit entity from an uuid if the chunk isn't loaded
    public final Map<UUID, Integer> CACHED_AMOUNT_ENTITIES = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> CACHED_AMOUNT_ITEMS = new ConcurrentHashMap<>();

    public DataHandler(WildStackerPlugin plugin){
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            long startTime = System.currentTimeMillis();
            WildStackerPlugin.log("Loading database started...");

            int cachedEntities = loadCachedEntities();
            //int cachedEntities = loadData(CACHED_ENTITIES, new File(instance.getDataFolder(), "data/entities.yml"));
            WildStackerPlugin.log(" - Found " + cachedEntities + " entities in files.");

            int cachedItems = loadCachedItems();
            //int cachedItems = loadData(CACHED_ITEMS, new File(instance.getDataFolder(), "data/items.yml"));
            WildStackerPlugin.log(" - Found " + cachedItems + " items in files.");

            int cachedSpawners = loadCachedSpawners();
            WildStackerPlugin.log(" - Found " + cachedSpawners + " spawners in files.");

            int cachedBarrels = loadCachedBarrels();
            WildStackerPlugin.log(" - Found " + cachedBarrels + " barrels in files.");

            WildStackerPlugin.log("Loading database done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
        }, 1L);
    }

    public void saveDatabase(){
        long startTime = System.currentTimeMillis();
        saveCachedEntities();
        saveCachedItems();
        saveCachedSpawners();
        saveCachedBarrels();
        WildStackerPlugin.log("Successfully saved database (" + (System.currentTimeMillis() - startTime) + "ms)");
    }

    private int loadCachedEntities(){
        File file = new File(plugin.getDataFolder(), "data/entities.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int dataAmount = 0;

        for(String uuid : cfg.getConfigurationSection("").getKeys(false)) {
            if(!uuid.equals("spawn-reasons")) {
                CACHED_AMOUNT_ENTITIES.put(UUID.fromString(uuid), cfg.getInt(uuid));
                dataAmount++;
            }
        }

        if(cfg.contains("spawn-reasons")){
            for(String uuid : cfg.getConfigurationSection("spawn-reasons").getKeys(false)){
                WStackedEntity.spawnReasons.put(UUID.fromString(uuid), CreatureSpawnEvent.SpawnReason.valueOf(cfg.getString("spawn-reasons." + uuid)));
            }
        }

        return dataAmount;
    }

    private int saveCachedEntities(){
        File file = new File(plugin.getDataFolder(), "data/entities.yml");

        if(file.exists())
            // @noinspection
            file.delete();

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch(IOException ex){
            ex.printStackTrace();
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int dataAmount = 0;

        for(StackedEntity stackedEntity : plugin.getSystemManager().getStackedEntities()){
            if(stackedEntity.getStackAmount() > 1){
                cfg.set(stackedEntity.getUniqueId().toString(), stackedEntity.getStackAmount());
                dataAmount++;
            }
        }

        for(UUID uuid : CACHED_AMOUNT_ENTITIES.keySet()) {
            if (CACHED_AMOUNT_ENTITIES.get(uuid) > 1) {
                cfg.set(uuid.toString(), CACHED_AMOUNT_ENTITIES.get(uuid));
                dataAmount++;
            }
        }

        for(UUID uuid : WStackedEntity.spawnReasons.keySet())
            cfg.set("spawn-reasons." + uuid, WStackedEntity.spawnReasons.get(uuid).name());

        try {
            cfg.save(file);
        } catch(IOException e){
            e.printStackTrace();
        }

        return dataAmount;
    }

    private int loadCachedItems(){
        File file = new File(plugin.getDataFolder(), "data/items.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int dataAmount = 0;

        for(String uuid : cfg.getConfigurationSection("").getKeys(false)) {
            CACHED_AMOUNT_ITEMS.put(UUID.fromString(uuid), cfg.getInt(uuid));
            dataAmount++;
        }

        for(UUID uuid : CACHED_AMOUNT_ITEMS.keySet()) {
            if (CACHED_AMOUNT_ITEMS.get(uuid) > 1) {
                cfg.set(uuid.toString(), CACHED_AMOUNT_ITEMS.get(uuid));
                dataAmount++;
            }
        }

        return dataAmount;
    }

    private int saveCachedItems(){
        File file = new File(plugin.getDataFolder(), "data/items.yml");

        if(file.exists())
            file.delete();

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch(IOException ex){
            ex.printStackTrace();
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int dataAmount = 0;

        for(StackedItem stackedItem : plugin.getSystemManager().getStackedItems()){
            if(stackedItem.getStackAmount() > 1){
                cfg.set(stackedItem.getUniqueId().toString(), stackedItem.getStackAmount());
                dataAmount++;
            }
        }

        try {
            cfg.save(file);
        } catch(IOException e){
            e.printStackTrace();
        }

        return dataAmount;
    }

    private int loadCachedSpawners(){
        File file = new File(plugin.getDataFolder(), "data/spawners.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int dataAmount = 0;

        for(String location : cfg.getConfigurationSection("").getKeys(false)) {
            String[] sections = location.split(",");
            Location realLocation = new Location(Bukkit.getWorld(sections[0]), Integer.valueOf(sections[1]), Integer.valueOf(sections[2]), Integer.valueOf(sections[3]));
            if(realLocation.getBlock().getState() instanceof CreatureSpawner) {
                StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) realLocation.getBlock().getState());
                stackedSpawner.setStackAmount(cfg.getInt(location), false);
                CACHED_OBJECTS.put(realLocation, stackedSpawner);
                dataAmount++;
            }
        }

        return dataAmount;
    }

    private int saveCachedSpawners(){
        File file = new File(plugin.getDataFolder(), "data/spawners.yml");

        if(file.exists())
            file.delete();

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch(IOException ex){
            ex.printStackTrace();
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int dataAmount = 0;

        for(StackedSpawner stackedSpawner : plugin.getSystemManager().getStackedSpawners()){
            if(stackedSpawner.getStackAmount() > 1) {
                Location location = stackedSpawner.getLocation();
                String locationString = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
                cfg.set(locationString, stackedSpawner.getStackAmount());
                dataAmount++;
            }
        }

        try {
            cfg.save(file);
        } catch(IOException e){
            e.printStackTrace();
        }

        return dataAmount;
    }

    private int loadCachedBarrels(){
        File file = new File(plugin.getDataFolder(), "data/barrels.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int dataAmount = 0;

        for(String location : cfg.getConfigurationSection("").getKeys(false)) {
            String[] sections = location.split(",");
            Location realLocation = new Location(Bukkit.getWorld(sections[0]), Integer.valueOf(sections[1]), Integer.valueOf(sections[2]), Integer.valueOf(sections[3]));
            StackedBarrel stackedBarrel = new WStackedBarrel(realLocation, cfg.getItemStack(location + ".item"));
            stackedBarrel.setStackAmount(cfg.getInt(location + ".amount"), false);
            CACHED_OBJECTS.put(realLocation, stackedBarrel);
            dataAmount++;
        }

        return dataAmount;
    }

    private int saveCachedBarrels(){
        File file = new File(plugin.getDataFolder(), "data/barrels.yml");

        if(file.exists())
            file.delete();

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch(IOException ex){
            ex.printStackTrace();
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        int dataAmount = 0;

        for(StackedBarrel stackedBarrel : plugin.getSystemManager().getStackedBarrels()){
            Location location = stackedBarrel.getLocation();
            String locationString = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
            cfg.set(locationString + ".amount", stackedBarrel.getStackAmount());
            cfg.set(locationString + ".item", stackedBarrel.getBarrelItem(1));
            dataAmount++;
        }

        try {
            cfg.save(file);
        } catch(IOException e){
            e.printStackTrace();
        }

        return dataAmount;
    }

}
