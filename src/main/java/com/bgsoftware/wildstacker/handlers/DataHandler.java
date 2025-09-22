package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.common.databasebridge.sql.query.Column;
import com.bgsoftware.common.databasebridge.sql.query.QueryResult;
import com.bgsoftware.common.databasebridge.sql.transaction.DeleteSQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.sql.transaction.InsertSQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.sql.transaction.SQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.transaction.IDatabaseTransaction;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.data.StackedBarrelStore;
import com.bgsoftware.wildstacker.data.StackedEntityStore;
import com.bgsoftware.wildstacker.data.StackedItemStore;
import com.bgsoftware.wildstacker.data.StackedSpawnerStore;
import com.bgsoftware.wildstacker.database.DBSession;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedBarrel;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedSpawner;
import com.bgsoftware.wildstacker.utils.data.structures.Location2ObjectMap;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"WeakerAccess", "all"})
public final class DataHandler {

    public final StackedItemStore stackedItemStore = new StackedItemStore();
    public final StackedEntityStore stackedEntityStore = new StackedEntityStore();
    public final StackedSpawnerStore stackedSpawnerStore = new StackedSpawnerStore();
    public final StackedBarrelStore stackedBarrelStore = new StackedBarrelStore();

    public final Set<StackedObject> OBJECTS_TO_SAVE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final WildStackerPlugin plugin;

    public DataHandler(WildStackerPlugin plugin) {
        this.plugin = plugin;

        Executor.sync(() -> {
            try {
                if (!DBSession.createConnection(plugin)) {
                    WildStackerPlugin.log("&cCouldn't connect to database, closing server...");
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
                    return;
                }

                loadDatabase();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
                return;
            }
        }, 1L);
    }

    public void clearDatabase() {
        DBSession.close();
    }

    public void addStackedSpawner(StackedSpawner stackedSpawner) {
        this.stackedSpawnerStore.store(stackedSpawner.getLocation(), stackedSpawner);
    }

    public void removeStackedSpawner(StackedSpawner stackedSpawner) {
        this.stackedSpawnerStore.remove(stackedSpawner.getLocation());
        Executor.sync(() -> ((WStackedSpawner) stackedSpawner).removeHologram());
    }

    public void addStackedBarrel(StackedBarrel stackedBarrel) {
        this.stackedBarrelStore.store(stackedBarrel.getLocation(), stackedBarrel);
    }

    public void removeStackedBarrel(StackedBarrel stackedBarrel) {
        this.stackedSpawnerStore.remove(stackedBarrel.getLocation());
        stackedBarrel.removeDisplayBlock();
        Executor.sync(() -> ((WStackedBarrel) stackedBarrel).removeHologram());
    }

    public List<StackedObject> getStackedObjects() {
        List<StackedObject> stackedObjects = new LinkedList<>();
        this.stackedItemStore.collect(stackedObjects);
        this.stackedEntityStore.collect(stackedObjects);
        this.stackedSpawnerStore.collect(stackedObjects);
        this.stackedBarrelStore.collect(stackedObjects);
        return stackedObjects.isEmpty() ? Collections.emptyList() : stackedObjects;
    }

    public SQLDatabaseTransaction<?> insertSpawner(WStackedSpawner stackedSpawner,
                                                   @Nullable SQLDatabaseTransaction<?> transaction) {
        if (transaction == null) {
            transaction = new InsertSQLDatabaseTransaction(
                    "spawners", Arrays.asList("location", "stackAmount", "upgrade"));
        }

        transaction
                .bindObject(serializeLocationInternal(stackedSpawner.getLocation()))
                .bindObject(stackedSpawner.getStackAmount())
                .bindObject(stackedSpawner.getUpgradeId())
                .newBatch();

        return transaction;
    }

    public void insertSpawner(WStackedSpawner stackedSpawner) {
        insertSpawner(stackedSpawner, null);
    }

    public void deleteSpawner(@Nullable Location location) {
        DBSession.execute(new DeleteSQLDatabaseTransaction("spawners", Arrays.asList("location"))
                .bindObject(serializeLocationInternal(location)));
    }

    public void deleteSpawner(Location2ObjectMap.ILocationEntity locationEntity) {
        DBSession.execute(new DeleteSQLDatabaseTransaction("spawners", Arrays.asList("location"))
                .bindObject(serializeLocationInternal(locationEntity.getWorldName(),
                        locationEntity.getX(), locationEntity.getY(), locationEntity.getZ())));
    }

    public SQLDatabaseTransaction<?> insertBarrel(StackedBarrel stackedBarrel,
                                                  @Nullable SQLDatabaseTransaction<?> transaction) {
        if (transaction == null) {
            transaction = new InsertSQLDatabaseTransaction(
                    "barrels", Arrays.asList("location", "stackAmount", "item"));
        }

        transaction
                .bindObject(serializeLocationInternal(stackedBarrel.getLocation()))
                .bindObject(stackedBarrel.getStackAmount())
                .bindObject(serializeItemInternal(stackedBarrel.getBarrelItem(1)))
                .newBatch();

        return transaction;
    }

    public void insertBarrel(StackedBarrel stackedBarrel) {
        insertBarrel(stackedBarrel, null);
    }

    public void deleteBarrel(@Nullable Location location) {
        DBSession.execute(new DeleteSQLDatabaseTransaction("barrels", Arrays.asList("location"))
                .bindObject(serializeLocationInternal(location)));
    }

    public void deleteBarrel(Location2ObjectMap.ILocationEntity locationEntity) {
        DBSession.execute(new DeleteSQLDatabaseTransaction("barrels", Arrays.asList("location"))
                .bindObject(serializeLocationInternal(locationEntity.getWorldName(),
                        locationEntity.getX(), locationEntity.getY(), locationEntity.getZ())));
    }

    private void loadDatabase() {
        prepareDatabase();

        loadEntities();
        loadItems();

        List<IDatabaseTransaction> transactionsToExecute = new LinkedList<>();

        try {
            loadSpawners(transactionsToExecute);
            loadBarrles(transactionsToExecute);
        } finally {
            if (!transactionsToExecute.isEmpty())
                DBSession.execute(transactionsToExecute);
        }

        plugin.getSystemManager().setDataLoaded();

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks())
                plugin.getSystemManager().handleChunkLoad(chunk, SystemHandler.CHUNK_FULL_STAGE);
        }
    }

    private void prepareDatabase() {
        DBSession.createTable("spawners",
                new Column("location", "LONG_UNIQUE_TEXT PRIMARY KEY"),
                new Column("stackAmount", "INTEGER"),
                new Column("upgrade", "INTEGER"));

        // Adding upgrade column if it doesn't exist
        addColumnIfNotExists("upgrade", "spawners", "0", "INTEGER");

        //Creating default barrels table
        DBSession.createTable("barrels",
                new Column("location", "LONG_UNIQUE_TEXT PRIMARY KEY"),
                new Column("stackAmount", "INTEGER"),
                new Column("item", "TEXT"));
    }

    private void loadEntities() {
        if (!plugin.getSettings().storeEntities)
            return;

        long startTime = System.currentTimeMillis();

        WildStackerPlugin.log("Starting to load entities...");

        DBSession.select("entities", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                int stackAmount = resultSet.getInt("stackAmount");
                SpawnCause spawnCause = SpawnCause.matchCause(resultSet.getString("spawnCause"));
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                this.stackedEntityStore.storeUnloaded(uuid, new StackedEntityStore.Unloaded(stackAmount, spawnCause));
            }
        }));

        WildStackerPlugin.log("Loading entities done! Took " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    private void loadItems() {
        if (!plugin.getSettings().storeItems)
            return;

        long startTime = System.currentTimeMillis();

        WildStackerPlugin.log("Starting to load items...");

        DBSession.select("items", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                int stackAmount = resultSet.getInt("stackAmount");
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                this.stackedItemStore.storeUnloaded(uuid, new StackedItemStore.Unloaded(stackAmount));
            }
        }));

        WildStackerPlugin.log("Loading items done! Took " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    private void loadSpawners(List<IDatabaseTransaction> transactionsToExecute) {
        long startTime = System.currentTimeMillis();

        WildStackerPlugin.log("Starting to load spawners...");

        DBSession.select("spawners", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            DeleteSQLDatabaseTransaction deleteNullWorldTransaction = new DeleteSQLDatabaseTransaction(
                    "spawners", Arrays.asList("location"));

            boolean calledDeleteTransaction = false;

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
                    this.stackedSpawnerStore.storeUnloaded(unloadedStackedSpawner);
                    continue;
                } catch (Exception ex) {
                    exceptionReason = "Exception was thrown.";
                }

                WildStackerPlugin.log("Couldn't load spawner: " + location);
                WildStackerPlugin.log(exceptionReason);

                if (exceptionReason.contains("Null") && plugin.getSettings().deleteInvalidWorlds) {
                    deleteNullWorldTransaction.bindObject(location).newBatch();
                    WildStackerPlugin.log("Deleted spawner (" + location + ") from database.");
                    calledDeleteTransaction = true;
                }
            }

            if (calledDeleteTransaction)
                transactionsToExecute.add(deleteNullWorldTransaction);

        }));

        WildStackerPlugin.log("Loading spawners done! Took " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    private void loadBarrles(List<IDatabaseTransaction> transactionsToExecute) {
        long startTime = System.currentTimeMillis();

        WildStackerPlugin.log("Starting to load barrels...");

        DBSession.select("barrels", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            DeleteSQLDatabaseTransaction deleteNullWorldTransaction = new DeleteSQLDatabaseTransaction(
                    "barrels", Arrays.asList("location"));

            boolean calledDeleteTransaction = false;

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
                    this.stackedBarrelStore.storeUnloaded(unloadedStackedBarrel);
                    continue;
                } catch (Exception ex) {
                    exceptionReason = "Exception was thrown.";
                }

                WildStackerPlugin.log("Couldn't load barrel: " + location);
                WildStackerPlugin.log(exceptionReason);

                if (exceptionReason.contains("Null") && plugin.getSettings().deleteInvalidWorlds) {
                    deleteNullWorldTransaction.bindObject(location).newBatch();
                    WildStackerPlugin.log("Deleted barrel (" + location + ") from database.");
                    calledDeleteTransaction = true;
                }
            }

            if (calledDeleteTransaction)
                transactionsToExecute.add(deleteNullWorldTransaction);
        }));

        WildStackerPlugin.log("Loading barrels done! Took " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    private String serializeItemInternal(@Nullable ItemStack itemStack) {
        return itemStack == null ? "" : plugin.getNMSAdapter().serialize(itemStack);
    }

    private static void addColumnIfNotExists(String column, String table, String def, String type) {
        String defaultSection = " DEFAULT " + def;
        DBSession.addColumn(table, column, type + defaultSection);
    }

    private static String serializeLocationInternal(@Nullable Location location) {
        return location == null ? "" : serializeLocationInternal(
                location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static String serializeLocationInternal(String worldName, int x, int y, int z) {
        return worldName + "," + x + "," + y + "," + z;
    }

}
