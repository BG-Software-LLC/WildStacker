package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.database.SQLHelper;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
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
    public final Map<UUID, SpawnCause> CACHED_SPAWN_CAUSE_ENTITIES = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> CACHED_AMOUNT_ITEMS = new ConcurrentHashMap<>();

    public DataHandler(WildStackerPlugin plugin){
        this.plugin = plugin;

        try {
            SQLHelper.init(new File(plugin.getDataFolder(), "database.db"));
            loadDatabase();
            loadOldFiles();
            loadOldSQL();
        }catch(Exception ex){
            ex.printStackTrace();
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
            return;
        }
    }

    public void clearDatabase(){
        SQLHelper.close();
    }

    private void loadOldFiles(){
        File dataFile = new File(plugin.getDataFolder(), "data");

        for(File worldFolder : dataFile.listFiles()){
            if(worldFolder.isDirectory()) {
                if(worldFolder.listFiles().length == 0) {
                    worldFolder.delete();
                }
            }
        }

        if(dataFile.listFiles().length != 0){
            WildStackerPlugin.log("Fetching old data files...");
            plugin.getServer().getPluginManager().registerEvents(new Listener() {

                @EventHandler
                public void onChunkLoad(ChunkLoadEvent e){
                    loadOldChunkFile(e.getChunk());
                }

            }, plugin);
        }

    }

    private void loadOldSQL(){
        File dataFolder = new File(plugin.getDataFolder(), "data");

        for(File databaseFile : dataFolder.listFiles()){
            if(!databaseFile.isDirectory() && databaseFile.getName().endsWith(".db")){
                World world = Bukkit.getWorld(databaseFile.getName().replace(".db", ""));
                String sqlURL = "jdbc:sqlite:" + databaseFile.getAbsolutePath().replace("\\", "/");
                boolean delete = false;

                try(Connection conn = DriverManager.getConnection(sqlURL)){

                    try(ResultSet resultSet = conn.prepareStatement("SELECT * FROM entities;").executeQuery()){
                        while (resultSet.next()) {
                            UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                            int stackAmount = resultSet.getInt("amount");
                            SpawnCause spawnCause = SpawnCause.valueOf(resultSet.getString("spawn_reason"));

                            CACHED_AMOUNT_ENTITIES.put(uuid, stackAmount);
                            CACHED_SPAWN_CAUSE_ENTITIES.put(uuid, spawnCause);
                            if (!SQLHelper.doesConditionExist("SELECT * FROM entities WHERE uuid = '" + uuid.toString() + "';")) {
                                Query.ENTITY_INSERT.getStatementHolder()
                                        .setString(uuid.toString())
                                        .setInt(stackAmount)
                                        .setString(spawnCause.name())
                                        .execute(true);
                            }
                        }
                    }

                    try(ResultSet resultSet = conn.prepareStatement("SELECT * FROM items;").executeQuery()){
                        while (resultSet.next()) {
                            UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                            int stackAmount = resultSet.getInt("amount");

                            CACHED_AMOUNT_ITEMS.put(uuid, stackAmount);
                            if (!SQLHelper.doesConditionExist("SELECT * FROM items WHERE uuid = '" + uuid.toString() + "';")) {
                                Query.ITEM_INSERT.getStatementHolder()
                                        .setString(uuid.toString())
                                        .setInt(stackAmount)
                                        .execute(true);
                            }
                        }
                    }

                    try(ResultSet resultSet = conn.prepareStatement("SELECT * FROM spawners;").executeQuery()){
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

                    try(ResultSet resultSet = conn.prepareStatement("SELECT * FROM barrels;").executeQuery()){
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
                }catch(SQLException ex){
                    WildStackerPlugin.log("Couldn't load old db file " + databaseFile.getName());
                    ex.printStackTrace();
                }

                if(delete)
                    databaseFile.delete();
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

        SQLHelper.executeQuery("SELECT * FROM entities;", resultSet -> {
            while (resultSet.next()) {
                int stackAmount = resultSet.getInt("stackAmount");
                SpawnCause spawnCause = SpawnCause.valueOf(resultSet.getString("spawnCause"));
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                CACHED_AMOUNT_ENTITIES.put(uuid, stackAmount);
                CACHED_SPAWN_CAUSE_ENTITIES.put(uuid, spawnCause);
            }
        });

        SQLHelper.executeQuery("SELECT * FROM items;", resultSet -> {
            while (resultSet.next()) {
                int stackAmount = resultSet.getInt("stackAmount");
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                CACHED_AMOUNT_ITEMS.put(uuid, stackAmount);
            }
        });

        SQLHelper.executeQuery("SELECT * FROM spawners;", resultSet -> {
            while (resultSet.next()) {
                String[] locationSections = resultSet.getString("location").split(",");
                Location blockLocation = new Location(
                        Bukkit.getWorld(locationSections[0]),
                        Integer.valueOf(locationSections[1]),
                        Integer.valueOf(locationSections[2]),
                        Integer.valueOf(locationSections[3])
                );

                int stackAmount = resultSet.getInt("stackAmount");
                Block spawnerBlock = blockLocation.getBlock();

                if(spawnerBlock.getState() instanceof CreatureSpawner) {
                    StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) spawnerBlock.getState(), stackAmount);
                    CACHED_OBJECTS.put(spawnerBlock.getLocation(), stackedSpawner);
                }
            }
        });

        SQLHelper.executeQuery("SELECT * FROM barrels;", resultSet -> {
            while (resultSet.next()) {
                String[] locationSections = resultSet.getString("location").split(",");
                Location blockLocation = new Location(
                        Bukkit.getWorld(locationSections[0]),
                        Integer.valueOf(locationSections[1]),
                        Integer.valueOf(locationSections[2]),
                        Integer.valueOf(locationSections[3])
                );

                int stackAmount = resultSet.getInt("stackAmount");
                Block barrelBlock = blockLocation.getBlock();

                ItemStack barrelItem = plugin.getNMSAdapter().deserialize(resultSet.getString("item"));
                StackedBarrel stackedBarrel = new WStackedBarrel(barrelBlock, barrelItem, stackAmount);
                CACHED_OBJECTS.put(stackedBarrel.getLocation(), stackedBarrel);
            }
        });
    }

    private void loadOldChunkFile(Chunk chunk){
        Stream<LivingEntity> livingEntityList = Arrays.stream(chunk.getEntities())
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity);
        Stream<Item> itemList = Arrays.stream(chunk.getEntities())
                .filter(item -> item instanceof Item)
                .map(item -> (Item) item);

        Set<StackedSpawner> stackedSpawners = new HashSet<>();
        Set<StackedBarrel> stackedBarrels = new HashSet<>();

        Executor.async(() -> {
            try {
                File file = new File(plugin.getDataFolder(), "data/" + chunk.getWorld().getName() + "/" + chunk.getX() + "," + chunk.getZ() + ".yml");

                if (!file.exists())
                    return;

                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

                if (cfg.contains("entities")) {
                    for (String uuid : cfg.getConfigurationSection("entities").getKeys(false)) {
                        int stackAmount = cfg.getInt("entities." + uuid + ".amount", 1);
                        SpawnCause spawnCause = SpawnCause.valueOf(cfg.getString("entities." + uuid + ".spawn-reason", "CHUNK_GEN"));

                        UUID _uuid = UUID.fromString(uuid);

                        if(CACHED_AMOUNT_ENTITIES.containsKey(_uuid)){
                            stackAmount = CACHED_AMOUNT_ENTITIES.get(_uuid);
                            CACHED_AMOUNT_ENTITIES.remove(_uuid);
                        }

                        if(CACHED_SPAWN_CAUSE_ENTITIES.containsKey(_uuid)){
                            spawnCause = CACHED_SPAWN_CAUSE_ENTITIES.get(_uuid);
                            CACHED_SPAWN_CAUSE_ENTITIES.remove(_uuid);
                        }

                        try {
                            StackedEntity stackedEntity = new WStackedEntity(livingEntityList
                                    .filter(livingEntity -> livingEntity.equals(_uuid)).findFirst().get(),
                                    stackAmount,
                                    spawnCause
                            );

                            CACHED_OBJECTS.put(_uuid, stackedEntity);
                        }catch(Exception ignored){}
                    }
                }

                if (cfg.contains("items")) {
                    for (String uuid : cfg.getConfigurationSection("items").getKeys(false)) {
                        int stackAmount = cfg.getInt("items." + uuid, 1);
                        UUID _uuid = UUID.fromString(uuid);

                        if (CACHED_AMOUNT_ITEMS.containsKey(_uuid)){
                            stackAmount = CACHED_AMOUNT_ITEMS.get(_uuid);
                            CACHED_AMOUNT_ITEMS.remove(_uuid);
                        }

                        try {
                            StackedItem stackedItem = new WStackedItem(
                                    itemList.filter(item -> item.getUniqueId().equals(_uuid)).findFirst().get(), stackAmount);

                            CACHED_OBJECTS.put(_uuid, stackedItem);
                        }catch(Exception ignored){}
                    }
                }

                if (cfg.contains("spawners")) {
                    for (String location : cfg.getConfigurationSection("spawners").getKeys(false)) {
                        String[] locationSections = location.split(",");
                        int stackAmount = cfg.getInt("spawners." + location, 1);
                        Block spawnerBlock = chunk.getBlock(Integer.valueOf(locationSections[0]),
                                Integer.valueOf(locationSections[1]), Integer.valueOf(locationSections[2]));
                        if (spawnerBlock.getType() == Materials.SPAWNER.toBukkitType()) {
                            StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) spawnerBlock.getState(), stackAmount);
                            CACHED_OBJECTS.put(spawnerBlock.getLocation(), stackedSpawner);
                            stackedSpawners.add(stackedSpawner);
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
                            StackedBarrel stackedBarrel = new WStackedBarrel(barrelBlock, barrelItem, stackAmount);
                            CACHED_OBJECTS.put(stackedBarrel.getLocation(), stackedBarrel);
                            stackedBarrels.add(stackedBarrel);
                        }
                    }
                }

                file.delete();

                Executor.sync(() -> {
                    Iterator<StackedBarrel> stackedBarrelsIterator = stackedBarrels.iterator();
                    Iterator<StackedSpawner> stackedSpawnersIterator = stackedSpawners.iterator();

                    while(stackedSpawnersIterator.hasNext()) {
                        stackedSpawnersIterator.next().updateName();
                    }

                    while(stackedBarrelsIterator.hasNext()){
                        StackedBarrel stackedBarrel = stackedBarrelsIterator.next();
                        stackedBarrel.updateName();
                        stackedBarrel.createDisplayBlock();
                    }
                });

            }catch(IllegalStateException ignored){ }
        });

    }

}
