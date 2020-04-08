package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.database.SQLHelper;
import com.bgsoftware.wildstacker.listeners.ChunksListener;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@SuppressWarnings({"WeakerAccess", "all"})
public final class DataHandler {

    private WildStackerPlugin plugin;

    public final Map<Object, StackedObject> CACHED_OBJECTS = new ConcurrentHashMap<>();

    //Here because we can't get the bukkit entity from an uuid if the chunk isn't loaded
    public final Map<UUID, Integer> CACHED_AMOUNT_ENTITIES = new ConcurrentHashMap<>();
    public final Map<Location, Integer> CACHED_AMOUNT_SPAWNERS = new ConcurrentHashMap<>();
    public final Map<UUID, SpawnCause> CACHED_SPAWN_CAUSE_ENTITIES = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> CACHED_AMOUNT_ITEMS = new ConcurrentHashMap<>();
    public final Set<UUID> CACHED_DEAD_ENTITIES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public DataHandler(WildStackerPlugin plugin){
        this.plugin = plugin;

        Executor.sync(() -> {
            try {
                SQLHelper.init(new File(plugin.getDataFolder(), "database.db"));
                loadDatabase();
                loadOldFiles();
                loadOldSQL();

                //Set all holograms of spawners
                for (StackedSpawner stackedSpawner : plugin.getSystemManager().getStackedSpawners())
                    stackedSpawner.updateName();

                //Set all holograms and block displays of barrlels
                for (StackedBarrel stackedBarrel : plugin.getSystemManager().getStackedBarrels()) {
                    stackedBarrel.updateName();
                    stackedBarrel.getLocation().getChunk().load(true);
                    stackedBarrel.createDisplayBlock();
                }
            }catch(Exception ex){
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
                return;
            }
        },1L);
    }

    public void clearDatabase(){
        SQLHelper.close();
    }

    private void loadOldFiles(){
        File dataFile = new File(plugin.getDataFolder(), "data");

        if(dataFile.exists()) {
            boolean save = false;

            for (File worldFolder : dataFile.listFiles()) {
                if (worldFolder.isDirectory()) {
                    if (worldFolder.listFiles().length == 0) {
                        worldFolder.delete();
                    }else{
                        save = true;
                    }
                }
            }

            if (save) {
                WildStackerPlugin.log("Fetching old data files...");
                plugin.getServer().getPluginManager().registerEvents(new Listener() {

                    @EventHandler
                    public void onChunkLoad(ChunkLoadEvent e) {
                        loadOldChunkFile(e.getChunk());
                    }

                }, plugin);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for(World world : Bukkit.getWorlds()){
                        for(Chunk chunk : world.getLoadedChunks())
                            loadOldChunkFile(chunk);
                    }
                }, 20L);

            }
        }

    }

    private void loadOldSQL(){
        File dataFolder = new File(plugin.getDataFolder(), "data");

        if(dataFolder.exists()) {
            for (File databaseFile : dataFolder.listFiles()) {
                if (!databaseFile.isDirectory() && databaseFile.getName().endsWith(".db")) {
                    World world = Bukkit.getWorld(databaseFile.getName().replace(".db", ""));
                    String sqlURL = "jdbc:sqlite:" + databaseFile.getAbsolutePath().replace("\\", "/");
                    boolean delete = false;

                    try (Connection conn = DriverManager.getConnection(sqlURL)) {

                        try (ResultSet resultSet = conn.prepareStatement("SELECT * FROM entities;").executeQuery()) {
                            while (resultSet.next()) {
                                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                                int stackAmount = resultSet.getInt("amount");
                                SpawnCause spawnCause = SpawnCause.matchCause(resultSet.getString("spawn_reason"));
                                CACHED_AMOUNT_ENTITIES.put(uuid, stackAmount);
                                CACHED_SPAWN_CAUSE_ENTITIES.put(uuid, spawnCause);
                            }
                        }

                        try (ResultSet resultSet = conn.prepareStatement("SELECT * FROM items;").executeQuery()) {
                            while (resultSet.next()) {
                                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                                int stackAmount = resultSet.getInt("amount");
                                CACHED_AMOUNT_ITEMS.put(uuid, stackAmount);
                            }
                        }

                        try (ResultSet resultSet = conn.prepareStatement("SELECT * FROM spawners;").executeQuery()) {
                            while (resultSet.next()) {
                                String chunk = resultSet.getString("chunk");
                                int chunkX = Integer.valueOf(chunk.split(",")[0]), chunkZ = Integer.valueOf(chunk.split(",")[1]);

                                String[] locationSections = resultSet.getString("location").split(",");
                                int x = Integer.valueOf(locationSections[0]), y = Integer.valueOf(locationSections[1]),
                                        z = Integer.valueOf(locationSections[2]);
                                Location blockLocation = new Location(world, chunkX << 4 | x & 15, y, chunkZ << 4 | z & 15);

                                int stackAmount = resultSet.getInt("amount");
                                Block spawnerBlock = blockLocation.getBlock();

                                if (spawnerBlock.getType() == Materials.SPAWNER.toBukkitType()) {
                                    System.out.println(blockLocation + ": " + stackAmount);
                                    StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) spawnerBlock.getState(), stackAmount);
                                    CACHED_OBJECTS.put(stackedSpawner.getLocation(), stackedSpawner);
                                }
                            }
                        }

                        try (ResultSet resultSet = conn.prepareStatement("SELECT * FROM barrels;").executeQuery()) {
                            while (resultSet.next()) {
                                String chunk = resultSet.getString("chunk");
                                int chunkX = Integer.valueOf(chunk.split(",")[0]), chunkZ = Integer.valueOf(chunk.split(",")[1]);

                                String[] locationSections = resultSet.getString("location").split(",");
                                int x = Integer.valueOf(locationSections[0]), y = Integer.valueOf(locationSections[1]),
                                        z = Integer.valueOf(locationSections[2]);
                                Location blockLocation = new Location(world, chunkX << 4 | x & 15, y, chunkZ << 4 | z & 15);

                                int stackAmount = resultSet.getInt("amount");
                                Block barrelBlock = blockLocation.getBlock();

                                if (barrelBlock.getType() == Material.CAULDRON) {
                                    ItemStack barrelItem = plugin.getNMSAdapter().deserialize(resultSet.getString("item"));
                                    StackedBarrel stackedBarrel = new WStackedBarrel(barrelBlock, barrelItem, stackAmount);
                                    CACHED_OBJECTS.put(stackedBarrel.getLocation(), stackedBarrel);
                                }
                            }
                        }

                        delete = true;
                    } catch (SQLException ex) {
                        WildStackerPlugin.log("Couldn't load old db file " + databaseFile.getName());
                        ex.printStackTrace();
                    }

                    if (delete)
                        databaseFile.delete();
                }
            }
        }
    }

    private void loadDatabase(){
        //Creating default entities table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS entities (uuid VARCHAR PRIMARY KEY, stackAmount INTEGER, spawnCause VARCHAR);");

        //Creating default items table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS items (uuid VARCHAR PRIMARY KEY, stackAmount INTEGER);");

        //Creating default spawners table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS spawners (location VARCHAR PRIMARY KEY, stackAmount INTEGER);");

        //Creating default barrels table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS barrels (location VARCHAR PRIMARY KEY, stackAmount INTEGER, item VARCHAR);");

        long startTime = System.currentTimeMillis();
        WildStackerPlugin.log("Starting to load entities...");

        SQLHelper.executeQuery("SELECT * FROM entities;", resultSet -> {
            while (resultSet.next()) {
                int stackAmount = resultSet.getInt("stackAmount");
                SpawnCause spawnCause = SpawnCause.matchCause(resultSet.getString("spawnCause"));
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                CACHED_AMOUNT_ENTITIES.put(uuid, stackAmount);
                CACHED_SPAWN_CAUSE_ENTITIES.put(uuid, spawnCause);
            }
        });

        WildStackerPlugin.log("Loading entities done! Took " + (System.currentTimeMillis() - startTime) + " ms.");
        startTime = System.currentTimeMillis();
        WildStackerPlugin.log("Starting to load items...");

        SQLHelper.executeQuery("SELECT * FROM items;", resultSet -> {
            while (resultSet.next()) {
                int stackAmount = resultSet.getInt("stackAmount");
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                CACHED_AMOUNT_ITEMS.put(uuid, stackAmount);
            }
        });

        WildStackerPlugin.log("Loading items done! Took " + (System.currentTimeMillis() - startTime) + " ms.");
        startTime = System.currentTimeMillis();
        WildStackerPlugin.log("Starting to load spawners...");

        SQLHelper.executeQuery("SELECT * FROM spawners;", resultSet -> {
            while (resultSet.next()) {
                String location = resultSet.getString("location");
                String[] locationSections = location.split(",");
                World blockWorld = Bukkit.getWorld(locationSections[0]);

                String exceptionReason = "Null world.";

                if(blockWorld != null) {
                    Location blockLocation = new Location(
                            blockWorld,
                            Integer.valueOf(locationSections[1]),
                            Integer.valueOf(locationSections[2]),
                            Integer.valueOf(locationSections[3])
                    );


                    try {
                        int stackAmount = resultSet.getInt("stackAmount");

                        if(plugin.getSettings().checkInvalidBlocks) {
                            Block spawnerBlock = blockLocation.getBlock();

                            if (spawnerBlock.getState() instanceof CreatureSpawner) {
                                StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) spawnerBlock.getState(), stackAmount);
                                CACHED_OBJECTS.put(spawnerBlock.getLocation(), stackedSpawner);
                                continue;
                            } else {
                                exceptionReason = "Block doesn't exist anymore.";
                            }
                        }
                        else{
                            CACHED_AMOUNT_SPAWNERS.put(blockLocation, stackAmount);
                            continue;
                        }
                    }catch(Exception ex){
                        exceptionReason = "Exception was thrown.";
                    }
                }

                WildStackerPlugin.log("Couldn't load spawner: " + location);
                WildStackerPlugin.log(exceptionReason);

                if((exceptionReason.contains("Null") && plugin.getSettings().deleteInvalidWorlds) ||
                        (exceptionReason.contains("Block") && plugin.getSettings().deleteInvalidBlocks)) {
                    SQLHelper.executeUpdate("DELETE FROM spawners WHERE location = '" + location + "';");
                    WildStackerPlugin.log("Deleted spawner (" + location + ") from database.");
                }
            }
        });

        WildStackerPlugin.log("Loading spawners done! Took " + (System.currentTimeMillis() - startTime) + " ms.");
        startTime = System.currentTimeMillis();
        WildStackerPlugin.log("Starting to load barrels...");

        SQLHelper.executeQuery("SELECT * FROM barrels;", resultSet -> {
            while (resultSet.next()) {
                String location = resultSet.getString("location");
                String[] locationSections = location.split(",");
                World blockWorld = Bukkit.getWorld(locationSections[0]);

                String exceptionReason = "Null world.";

                if(blockWorld != null) {
                    Location blockLocation = new Location(
                            blockWorld,
                            Integer.valueOf(locationSections[1]),
                            Integer.valueOf(locationSections[2]),
                            Integer.valueOf(locationSections[3])
                    );

                    try {
                        int stackAmount = resultSet.getInt("stackAmount");
                        Block barrelBlock = blockLocation.getBlock();
                        if(barrelBlock.getType() != Material.CAULDRON && plugin.getSettings().forceCauldron)
                            barrelBlock.setType(Material.CAULDRON);
                        if (barrelBlock.getType() == Material.CAULDRON) {
                            ItemStack barrelItem = resultSet.getString("item").isEmpty() ? null :
                                    plugin.getNMSAdapter().deserialize(resultSet.getString("item"));
                            StackedBarrel stackedBarrel = new WStackedBarrel(barrelBlock, barrelItem, stackAmount);
                            CACHED_OBJECTS.put(stackedBarrel.getLocation(), stackedBarrel);
                            continue;
                        }
                        else{
                            exceptionReason = "Block doesn't exist anymore.";
                        }
                    } catch (Exception ex) {
                        exceptionReason = "Exception was thrown.";
                    }
                }

                WildStackerPlugin.log("Couldn't load barrel: " + location);
                WildStackerPlugin.log(exceptionReason);

                if((exceptionReason.contains("Null") && plugin.getSettings().deleteInvalidWorlds) ||
                        (exceptionReason.contains("Block") && plugin.getSettings().deleteInvalidBlocks)) {
                    SQLHelper.executeUpdate("DELETE FROM barrels WHERE location = '" + location + "';");
                    WildStackerPlugin.log("Deleted barrel (" + location + ") from database.");
                }
            }
        });

        WildStackerPlugin.log("Loading barrels done! Took " + (System.currentTimeMillis() - startTime) + " ms.");

        ChunksListener.loadedData = true;
    }

    private void loadOldChunkFile(Chunk chunk){
        Stream<LivingEntity> livingEntityList = Arrays.stream(chunk.getEntities())
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity);
        Stream<Item> itemList = Arrays.stream(chunk.getEntities())
                .filter(item -> item instanceof Item)
                .map(item -> (Item) item);

        Set<RawStackedSpawner> stackedSpawners = new HashSet<>();
        Set<RawStackedBarrel> stackedBarrels = new HashSet<>();

        Executor.async(() -> {
            try {
                File file = new File(plugin.getDataFolder(), "data/" + chunk.getWorld().getName() + "/" + chunk.getX() + "," + chunk.getZ() + ".yml");

                if (!file.exists())
                    return;

                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

                if (cfg.contains("entities")) {
                    for (String uuid : cfg.getConfigurationSection("entities").getKeys(false)) {
                        int stackAmount = cfg.getInt("entities." + uuid + ".amount", 1);
                        SpawnCause spawnCause = SpawnCause.matchCause(cfg.getString("entities." + uuid + ".spawn-reason", "CHUNK_GEN"));
                        UUID _uuid = UUID.fromString(uuid);
                        CACHED_AMOUNT_ENTITIES.put(_uuid, stackAmount);
                        CACHED_SPAWN_CAUSE_ENTITIES.put(_uuid, spawnCause);
                    }
                }

                if (cfg.contains("items")) {
                    for (String uuid : cfg.getConfigurationSection("items").getKeys(false)) {
                        int stackAmount = cfg.getInt("items." + uuid, 1);
                        UUID _uuid = UUID.fromString(uuid);
                        CACHED_AMOUNT_ITEMS.put(_uuid, stackAmount);
                    }
                }

                if (cfg.contains("spawners")) {
                    for (String location : cfg.getConfigurationSection("spawners").getKeys(false)) {
                        String[] locationSections = location.split(",");
                        int stackAmount = cfg.getInt("spawners." + location, 1);
                        Block spawnerBlock = chunk.getBlock(Integer.valueOf(locationSections[0]),
                                Integer.valueOf(locationSections[1]), Integer.valueOf(locationSections[2]));
                        if (spawnerBlock.getType() == Materials.SPAWNER.toBukkitType()) {
                            stackedSpawners.add(new RawStackedSpawner(spawnerBlock, stackAmount));
                        }
                    }
                }

                if (cfg.contains("barrels")) {
                    for (String location : cfg.getConfigurationSection("barrels").getKeys(false)) {
                        String[] locationSections = location.split(",");
                        int stackAmount = cfg.getInt("barrels." + location + ".amount", 1);
                        Block barrelBlock = chunk.getBlock(Integer.valueOf(locationSections[0]),
                                Integer.valueOf(locationSections[1]), Integer.valueOf(locationSections[2]));
                        if (barrelBlock.getType() == Material.CAULDRON) {
                            ItemStack barrelItem = cfg.getItemStack("barrels." + location + ".item");
                            stackedBarrels.add(new RawStackedBarrel(barrelBlock, barrelItem, stackAmount));
                        }
                    }
                }

                file.delete();

                Executor.sync(() -> {
                    Iterator<RawStackedBarrel> stackedBarrelsIterator = stackedBarrels.iterator();
                    Iterator<RawStackedSpawner> stackedSpawnersIterator = stackedSpawners.iterator();

                    while(stackedSpawnersIterator.hasNext()) {
                        StackedSpawner stackedSpawner = stackedSpawnersIterator.next().create();
                        CACHED_OBJECTS.put(stackedSpawner.getLocation(), stackedSpawner);
                        stackedSpawner.updateName();
                    }

                    while(stackedBarrelsIterator.hasNext()){
                        StackedBarrel stackedBarrel = stackedBarrelsIterator.next().create();
                        CACHED_OBJECTS.put(stackedBarrel.getLocation(), stackedBarrel);
                        stackedBarrel.updateName();
                        stackedBarrel.createDisplayBlock();
                    }
                });

            }catch(IllegalStateException ex){
                ex.printStackTrace();
            }
        });

    }

    private class RawStackedSpawner{

        private int stackAmount;
        private Block block;

        RawStackedSpawner(Block block, int stackAmount){
            this.block = block;
            this.stackAmount = stackAmount;
        }

        StackedSpawner create(){
            return new WStackedSpawner((CreatureSpawner) block.getState(), stackAmount);
        }

    }

    private class RawStackedBarrel{

        private int stackAmount;
        private Block block;
        private ItemStack barrelItem;

        RawStackedBarrel(Block block, ItemStack barrelItem, int stackAmount){
            this.block = block;
            this.barrelItem = barrelItem;
            this.stackAmount = stackAmount;
        }

        StackedBarrel create(){
            return new WStackedBarrel(block, barrelItem, stackAmount);
        }

    }

}
