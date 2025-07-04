package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedBarrel;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedSpawner;
import com.bgsoftware.wildstacker.database.SQLHelper;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedBarrel;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedSpawner;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.data.structures.Location2ObjectMap;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"WeakerAccess", "all"})
public final class DataHandler {

    public final Map<UUID, StackedItem> CACHED_ITEMS = new ConcurrentHashMap<>();
    public final Map<UUID, StackedEntity> CACHED_ENTITIES = new ConcurrentHashMap<>();
    public final Location2ObjectMap<StackedSpawner> CACHED_SPAWNERS = new Location2ObjectMap<>();
    public final Map<ChunkPosition, Set<StackedSpawner>> CACHED_SPAWNERS_BY_CHUNKS = new ConcurrentHashMap<>();
    public final Location2ObjectMap<StackedBarrel> CACHED_BARRELS = new Location2ObjectMap<>();
    public final Map<ChunkPosition, Set<StackedBarrel>> CACHED_BARRELS_BY_CHUNKS = new ConcurrentHashMap<>();
    public final Set<StackedObject> OBJECTS_TO_SAVE = Collections.newSetFromMap(new ConcurrentHashMap<>());
    //References for all the data from database
    public final Map<UUID, Integer> CACHED_ITEMS_RAW = new ConcurrentHashMap<>();
    public final Map<UUID, Pair<Integer, SpawnCause>> CACHED_ENTITIES_RAW = new ConcurrentHashMap<>();
    public final Location2ObjectMap<UnloadedStackedSpawner> CACHED_SPAWNERS_RAW = new Location2ObjectMap<>();
    public final Location2ObjectMap<UnloadedStackedBarrel> CACHED_BARRELS_RAW = new Location2ObjectMap<>();
    public final Set<UUID> CACHED_DEAD_ENTITIES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private WildStackerPlugin plugin;

    public DataHandler(WildStackerPlugin plugin) {
        this.plugin = plugin;

        Executor.sync(() -> {
            try {
                //Database.start(new File(plugin.getDataFolder(), "database.db"));
                SQLHelper.createConnection(plugin);
                loadDatabase();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
                return;
            }
        }, 1L);
    }

    private static void addColumnIfNotExists(String column, String table, String def, String type) {
        String defaultSection = " DEFAULT " + def;

        String statementStr = "ALTER TABLE " + table + " ADD " + column + " " + type + defaultSection + ";";

        SQLHelper.executeUpdate(statementStr, ex -> {
            if (!ex.getMessage().toLowerCase().contains("duplicate")) {
                WildStackerPlugin.log("Statement: " + statementStr);
                ex.printStackTrace();
            }
        });
    }

    public void clearDatabase() {
        //Database.stop();
        SQLHelper.close();
    }

    public void addStackedSpawner(StackedSpawner stackedSpawner) {
        CACHED_SPAWNERS.put(stackedSpawner.getLocation(), stackedSpawner);
        CACHED_SPAWNERS_BY_CHUNKS.computeIfAbsent(new ChunkPosition(stackedSpawner.getLocation()),
                s -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(stackedSpawner);
    }

    public void removeStackedSpawner(StackedSpawner stackedSpawner) {
        CACHED_SPAWNERS.remove(stackedSpawner.getLocation());
        Set<StackedSpawner> chunkSpawners = CACHED_SPAWNERS_BY_CHUNKS.get(new ChunkPosition(stackedSpawner.getLocation()));
        if (chunkSpawners != null)
            chunkSpawners.remove(stackedSpawner);
        Executor.sync(() -> ((WStackedSpawner) stackedSpawner).removeHologram());
    }

    public void addStackedBarrel(StackedBarrel stackedBarrel) {
        CACHED_BARRELS.put(stackedBarrel.getLocation(), stackedBarrel);
        CACHED_BARRELS_BY_CHUNKS.computeIfAbsent(new ChunkPosition(stackedBarrel.getLocation()),
                s -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(stackedBarrel);
    }

    public void removeStackedBarrel(StackedBarrel stackedBarrel) {
        CACHED_BARRELS.remove(stackedBarrel.getLocation());
        Set<StackedBarrel> chunkBarrels = CACHED_BARRELS_BY_CHUNKS.get(new ChunkPosition(stackedBarrel.getLocation()));
        if (chunkBarrels != null)
            chunkBarrels.remove(stackedBarrel);
        stackedBarrel.removeDisplayBlock();
        Executor.sync(() -> ((WStackedBarrel) stackedBarrel).removeHologram());
    }

    public List<StackedObject> getStackedObjects() {
        List<StackedObject> stackedObjects = new LinkedList<>();
        stackedObjects.addAll(CACHED_ITEMS.values());
        stackedObjects.addAll(CACHED_ENTITIES.values());
        CACHED_SPAWNERS.collect(stackedObjects);
        CACHED_BARRELS.collect(stackedObjects);
        return stackedObjects;
    }

    private void loadDatabase() {
        //Creating default spawners table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS spawners (location VARCHAR PRIMARY KEY, stackAmount INTEGER, upgrade INTEGER);");
        // Adding upgrade column if it doesn't exist
        addColumnIfNotExists("upgrade", "spawners", "0", "INTEGER");

        //Creating default barrels table
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS barrels (location VARCHAR PRIMARY KEY, stackAmount INTEGER, item VARCHAR);");

        long startTime = System.currentTimeMillis();

        if (plugin.getSettings().storeEntities) {
            WildStackerPlugin.log("Starting to load entities...");

            SQLHelper.executeQuery("SELECT * FROM entities;", resultSet -> {
                while (resultSet.next()) {
                    int stackAmount = resultSet.getInt("stackAmount");
                    SpawnCause spawnCause = SpawnCause.matchCause(resultSet.getString("spawnCause"));
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    CACHED_ENTITIES_RAW.put(uuid, new Pair<>(stackAmount, spawnCause));
                }
            }, ex -> {
            });

            WildStackerPlugin.log("Loading entities done! Took " + (System.currentTimeMillis() - startTime) + " ms.");
        }

        startTime = System.currentTimeMillis();

        if (plugin.getSettings().storeItems) {
            WildStackerPlugin.log("Starting to load items...");

            SQLHelper.executeQuery("SELECT * FROM items;", resultSet -> {
                while (resultSet.next()) {
                    int stackAmount = resultSet.getInt("stackAmount");
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    CACHED_ITEMS_RAW.put(uuid, stackAmount);
                }
            }, ex -> {
            });

            WildStackerPlugin.log("Loading items done! Took " + (System.currentTimeMillis() - startTime) + " ms.");
        }

        startTime = System.currentTimeMillis();
        WildStackerPlugin.log("Starting to load spawners...");

        SQLHelper.executeQuery("SELECT * FROM spawners;", resultSet -> {
            while (resultSet.next()) {
                String location = resultSet.getString("location");
                String[] locationSections = location.split(",");

                String worldName = locationSections[0];
                int locX = Integer.valueOf(locationSections[1]);
                int locY = Integer.valueOf(locationSections[2]);
                int locZ = Integer.valueOf(locationSections[3]);

                String exceptionReason = null;

                try {
                    int stackAmount = resultSet.getInt("stackAmount");
                    int upgradeId = resultSet.getInt("upgrade");
                    WUnloadedStackedSpawner unloadedStackedSpawner =
                            new WUnloadedStackedSpawner(worldName, locX, locY, locZ, stackAmount, upgradeId);
                    CACHED_SPAWNERS_RAW.put(unloadedStackedSpawner, unloadedStackedSpawner);
                    continue;
                } catch (Exception ex) {
                    exceptionReason = "Exception was thrown.";
                }

                WildStackerPlugin.log("Couldn't load spawner: " + location);
                WildStackerPlugin.log(exceptionReason);

                if (exceptionReason.contains("Null") && plugin.getSettings().deleteInvalidWorlds) {
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

                String worldName = locationSections[0];
                int locX = Integer.valueOf(locationSections[1]);
                int locY = Integer.valueOf(locationSections[2]);
                int locZ = Integer.valueOf(locationSections[3]);

                String exceptionReason = null;

                try {
                    int stackAmount = resultSet.getInt("stackAmount");
                    ItemStack barrelItem = resultSet.getString("item").isEmpty() ? null :
                            plugin.getNMSAdapter().deserialize(resultSet.getString("item"));
                    WUnloadedStackedBarrel unloadedStackedBarrel =
                            new WUnloadedStackedBarrel(worldName, locX, locY, locZ, stackAmount, barrelItem);
                    CACHED_BARRELS_RAW.put(unloadedStackedBarrel, unloadedStackedBarrel);
                    continue;
                } catch (Exception ex) {
                    exceptionReason = "Exception was thrown.";
                }

                WildStackerPlugin.log("Couldn't load barrel: " + location);
                WildStackerPlugin.log(exceptionReason);

                if (exceptionReason.contains("Null") && plugin.getSettings().deleteInvalidWorlds) {
                    SQLHelper.executeUpdate("DELETE FROM barrels WHERE location = '" + location + "';");
                    WildStackerPlugin.log("Deleted barrel (" + location + ") from database.");
                }
            }
        });

        WildStackerPlugin.log("Loading barrels done! Took " + (System.currentTimeMillis() - startTime) + " ms.");

        plugin.getSystemManager().setDataLoaded();

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks())
                plugin.getSystemManager().handleChunkLoad(chunk, SystemHandler.CHUNK_FULL_STAGE);
        }
    }

}
