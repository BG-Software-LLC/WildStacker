package com.bgsoftware.wildstacker.data;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SQLDataHandler extends AbstractDataHandler {

    private final Map<String, Connection> connectionMap = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public SQLDataHandler(WildStackerPlugin plugin){
        super(plugin);

        try{
            Class.forName("org.sqlite.JDBC");
        }catch(Exception ex){
            Executor.sync(() -> {
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                throw new RuntimeException("Cannot initialize SQL database.");
            });
        }

        for(World world : Bukkit.getWorlds()){
            try{
                File file = new File(plugin.getDataFolder(), "data/" + world.getName() + ".db");

                if(!file.exists()){
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }

                String sqlURL = "jdbc:sqlite:" + file.getAbsolutePath().replace("\\", "/");
                Connection conn = DriverManager.getConnection(sqlURL);
                connectionMap.put(world.getName(), conn);

                //Creating default tables
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS entities (chunk VARCHAR, uuid VARCHAR, amount INTEGER, spawn_reason VARCHAR);").executeUpdate();
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS items (chunk VARCHAR, uuid VARCHAR, amount INTEGER);").executeUpdate();
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS spawners (chunk VARCHAR, location VARCHAR, amount INTEGER);").executeUpdate();
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS barrels (chunk VARCHAR, location VARCHAR, amount INTEGER, item VARCHAR);").executeUpdate();
            }catch(Exception ex){
                Executor.sync(() -> {
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                    throw new RuntimeException("Cannot initialize SQL database.");
                });
            }
        }
    }

    @Override
    public void loadChunkData(Chunk chunk) {
        ChunkRegistry chunkRegistry = new ChunkRegistry(chunk);
        Executor.async(() -> {
            try{
                Connection conn = connectionMap.get(chunk.getWorld().getName());
                ResultSet resultSet;

                //Entities
                if(doesTableExist(conn, "entities")) {
                    resultSet = conn.prepareStatement(
                            String.format("SELECT * FROM entities WHERE chunk = '%s';", chunk.getX() + "," + chunk.getZ())).executeQuery();
                    while (resultSet.next()) {
                        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                        int stackAmount = resultSet.getInt("amount");
                        CreatureSpawnEvent.SpawnReason spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(resultSet.getString("spawn_reason"));

                        StackedEntity stackedEntity = new WStackedEntity(chunkRegistry.getLivingEntity(uuid), stackAmount, spawnReason);

                        //Entities are moving. There's no point in storing them based on chunks.
                        CACHED_ENTITIES.put(DEFAULT_CHUNK, uuid, stackedEntity);
                    }
                }

                //Items
                if(doesTableExist(conn, "items")) {
                    resultSet = conn.prepareStatement(
                            String.format("SELECT * FROM items WHERE chunk = '%s';", chunk.getX() + "," + chunk.getZ())).executeQuery();
                    while (resultSet.next()) {
                        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                        int stackAmount = resultSet.getInt("amount");

                        StackedItem stackedItem = new WStackedItem(chunkRegistry.getItem(uuid), stackAmount);

                        CACHED_ITEMS.put(chunk, uuid, stackedItem);
                    }
                }

                //Spawners
                if(doesTableExist(conn, "spawners")) {
                    resultSet = conn.prepareStatement(
                            String.format("SELECT * FROM spawners WHERE chunk = '%s';", chunk.getX() + "," + chunk.getZ())).executeQuery();
                    while (resultSet.next()) {
                        String[] locationSections = resultSet.getString("location").split(",");
                        int stackAmount = resultSet.getInt("amount");
                        Block spawnerBlock = chunkRegistry.getBlock(Integer.valueOf(locationSections[0]),
                                Integer.valueOf(locationSections[1]), Integer.valueOf(locationSections[2]));
                        if (spawnerBlock.getType() == Materials.SPAWNER.toBukkitType()) {
                            StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) spawnerBlock.getState(), stackAmount);
                            CACHED_SPAWNERS.put(chunk, spawnerBlock.getLocation(), stackedSpawner);
                        }
                    }
                }

                //Barrels
                if(doesTableExist(conn, "barrels")) {
                    resultSet = conn.prepareStatement(
                            String.format("SELECT * FROM barrels WHERE chunk = '%s';", chunk.getX() + "," + chunk.getZ())).executeQuery();
                    while (resultSet.next()) {
                        String[] locationSections = resultSet.getString("location").split(",");
                        int stackAmount = resultSet.getInt("amount");
                        Block barrelBlock = chunkRegistry.getBlock(Integer.valueOf(locationSections[0]),
                                Integer.valueOf(locationSections[1]), Integer.valueOf(locationSections[2]));
                        if (barrelBlock.getType() == Material.CAULDRON) {
                            ItemStack barrelItem = plugin.getNMSAdapter().deserialize(resultSet.getString("item"));
                            StackedBarrel stackedBarrel = new WStackedBarrel(barrelBlock, barrelItem, stackAmount);
                            CACHED_BARRELS.put(chunk, stackedBarrel.getLocation(), stackedBarrel);

                        }
                    }
                }

                Iterator<StackedSpawner> stackedSpawners = CACHED_SPAWNERS.iterator(chunk);
                Iterator<StackedBarrel> stackedBarrels = CACHED_BARRELS.iterator(chunk);

                Executor.sync(() -> {
                    while(stackedSpawners.hasNext()) {
                        stackedSpawners.next().updateName();
                    }

                    while(stackedBarrels.hasNext()){
                        StackedBarrel stackedBarrel = stackedBarrels.next();
                        stackedBarrel.updateName();
                        stackedBarrel.createDisplayBlock();
                    }
                });
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void saveChunkData(Chunk chunk, boolean remove, boolean async) {
        List<String> preparedStatementList = new ArrayList<>();

        preparedStatementList.add(String.format("DELETE FROM entities WHERE chunk = '%s';", chunk.getX() + "," + chunk.getZ()));
        preparedStatementList.add(String.format("DELETE FROM items WHERE chunk = '%s';", chunk.getX() + "," + chunk.getZ()));
        preparedStatementList.add(String.format("DELETE FROM spawners WHERE chunk = '%s';", chunk.getX() + "," + chunk.getZ()));
        preparedStatementList.add(String.format("DELETE FROM barrels WHERE chunk = '%s';", chunk.getX() + "," + chunk.getZ()));

        Set<StackedEntity> stackedEntities = new HashSet<>();

        Iterator<StackedEntity> entities = CACHED_ENTITIES.iterator();
        //noinspection all
        while(entities.hasNext()){
            StackedEntity stackedEntity = entities.next();
            if (stackedEntity != null && stackedEntity.getLivingEntity() != null &&
                    isSameChunk(chunk, stackedEntity.getLivingEntity().getLocation())) {
                stackedEntities.add(stackedEntity);
                preparedStatementList.add(String.format("INSERT INTO entities VALUES('%s', '%s', %s, '%s');",
                        chunk.getX() + "," + chunk.getZ(),
                        stackedEntity.getUniqueId(),
                        stackedEntity.getStackAmount(),
                        stackedEntity.getSpawnReason().name())
                );
            }
            if(stackedEntity == null || stackedEntity.getLivingEntity() == null)
                Bukkit.broadcastMessage("1");
        }

        Iterator<StackedItem> items = CACHED_ITEMS.iterator(chunk);
        while(items.hasNext()){
            StackedItem stackedItem = items.next();
            if(stackedItem != null && stackedItem.getItem() != null && stackedItem.getStackAmount() > 1)
                preparedStatementList.add(String.format("INSERT INTO items VALUES('%s', '%s', %s);",
                        chunk.getX() + "," + chunk.getZ(),
                        stackedItem.getUniqueId(),
                        stackedItem.getStackAmount())
                );
        }

        Iterator<StackedSpawner> spawners = CACHED_SPAWNERS.iterator(chunk);
        while(spawners.hasNext()){
            StackedSpawner stackedSpawner = spawners.next();
            if(stackedSpawner.getStackAmount() > 1)
                preparedStatementList.add(String.format("INSERT INTO spawners VALUES('%s', '%s', %s);",
                        chunk.getX() + "," + chunk.getZ(),
                        getChunkLocation(stackedSpawner.getLocation()),
                        stackedSpawner.getStackAmount())
                );

            if(remove){
                plugin.getProviders().deleteHologram(stackedSpawner);
            }
        }

        Iterator<StackedBarrel> barrels = CACHED_BARRELS.iterator(chunk);
        while(barrels.hasNext()){
            StackedBarrel stackedBarrel = barrels.next();
            ItemStack barrelItem = stackedBarrel.getBarrelItem(1);
            preparedStatementList.add(String.format("INSERT INTO barrels VALUES('%s', '%s', %s, '%s');",
                    chunk.getX() + "," + chunk.getZ(),
                    getChunkLocation(stackedBarrel.getLocation()),
                    stackedBarrel.getStackAmount(),
                    plugin.getNMSAdapter().serialize(barrelItem))
            );
            if(remove){
                plugin.getProviders().deleteHologram(stackedBarrel);
                stackedBarrel.removeDisplayBlock();
            }
        }

        if(async)
            Executor.async(() -> executeUpdates(preparedStatementList, chunk));
        else
            executeUpdates(preparedStatementList, chunk);

        if(remove){
            stackedEntities.forEach(stackedEntity -> CACHED_ENTITIES.remove(DEFAULT_CHUNK, stackedEntity.getUniqueId()));
            CACHED_ITEMS.remove(chunk);
            CACHED_SPAWNERS.remove(chunk);
            CACHED_BARRELS.remove(chunk);
        }
    }

    private void executeUpdates(List<String> preparedStatementList, Chunk chunk){
        Connection conn = connectionMap.get(chunk.getWorld().getName());
        preparedStatementList.forEach(preparedStatement -> {
            try{
                conn.prepareStatement(preparedStatement).executeUpdate();
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void clearDatabase() {
        //We need to close all connections
        for(Connection conn : connectionMap.values()){
            try{
                conn.close();
            }catch(Exception ignored){}
        }
        //Clearing cache
        connectionMap.clear();
    }

    private boolean doesTableExist(Connection conn, String tableName){
        try{
            conn.prepareStatement("SELECT * FROM " + tableName + ";").executeQuery();
            return true;
        }catch(Exception ex){
            return false;
        }
    }

}
