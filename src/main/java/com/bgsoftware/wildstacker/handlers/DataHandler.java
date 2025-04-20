package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.*;
import com.bgsoftware.wildstacker.database.sql.SQLHelper;
import com.bgsoftware.wildstacker.database.sql.session.QueryResult;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedBarrel;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedSpawner;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DataHandler {

    private final WildStackerPlugin plugin;

    public final Map<UUID, StackedItem> CACHED_ITEMS = new ConcurrentHashMap<>();
    public final Map<UUID, StackedEntity> CACHED_ENTITIES = new ConcurrentHashMap<>();
    public final Map<Location, StackedSpawner> CACHED_SPAWNERS = new ConcurrentHashMap<>();
    public final Map<ChunkPosition, Set<StackedSpawner>> CACHED_SPAWNERS_BY_CHUNKS = new ConcurrentHashMap<>();
    public final Map<Location, StackedBarrel> CACHED_BARRELS = new ConcurrentHashMap<>();
    public final Map<ChunkPosition, Set<StackedBarrel>> CACHED_BARRELS_BY_CHUNKS = new ConcurrentHashMap<>();
    public final Set<StackedObject> OBJECTS_TO_SAVE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public final Map<UUID, Integer> CACHED_ITEMS_RAW = new ConcurrentHashMap<>();
    public final Map<UUID, Pair<Integer, SpawnCause>> CACHED_ENTITIES_RAW = new ConcurrentHashMap<>();
    public final Map<ChunkPosition, Map<Location, UnloadedStackedSpawner>> CACHED_SPAWNERS_RAW = new ConcurrentHashMap<>();
    public final Map<ChunkPosition, Map<Location, UnloadedStackedBarrel>> CACHED_BARRELS_RAW = new ConcurrentHashMap<>();
    public final Set<UUID> CACHED_DEAD_ENTITIES = Collections.newSetFromMap(new ConcurrentHashMap<>());


    public DataHandler(WildStackerPlugin plugin) {
        this.plugin = plugin;
        Executor.sync(() -> {
            if (!SQLHelper.createConnection(plugin)) {
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
                return;
            }
            loadDatabase();
        }, 1L);
    }

    public void clearDatabase() {
        SQLHelper.close();
    }

    private void loadDatabase() {
        SQLHelper.createTable("spawners",
                new Pair<>("location", "LONG_UNIQUE_TEXT PRIMARY KEY"),
                new Pair<>("stackAmount", "INTEGER"),
                new Pair<>("upgrade", "INTEGER")
        );

        SQLHelper.createTable("barrels",
                new Pair<>("location", "LONG_UNIQUE_TEXT PRIMARY KEY"),
                new Pair<>("stackAmount", "INTEGER"),
                new Pair<>("item", "TEXT")
        );

        if (plugin.getSettings().getEntities().isStoreEntitiesEnabled()) {
            loadEntities();
        }

        if (plugin.getSettings().getItems().isStoreItemsEnabled()) {
            loadItems();
        }
        loadSpawners();
        loadBarrels();
        plugin.getSystemManager().setDataLoaded();

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                plugin.getSystemManager().handleChunkLoad(chunk, SystemHandler.CHUNK_FULL_STAGE);
            }
        }


    }

    private void loadEntities() {
        SQLHelper.select("entities", "", new QueryResult<ResultSet>()
                .onSuccess(resultSet -> {
                    while (resultSet.next()) {
                        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                        int stackAmount = resultSet.getInt("stackAmount");
                        SpawnCause cause = SpawnCause.matchCause(resultSet.getString("spawnCause"));
                        CACHED_ENTITIES_RAW.put(uuid, new Pair<>(stackAmount, cause));
                    }
                })
                .onFail(Throwable::printStackTrace)
        );
    }

    private void loadItems() {
        SQLHelper.select("items", "", new QueryResult<ResultSet>()
                .onSuccess(resultSet -> {
                    while (resultSet.next()) {
                        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                        int stackAmount = resultSet.getInt("stackAmount");
                        CACHED_ITEMS_RAW.put(uuid, stackAmount);
                    }
                })
                .onFail(Throwable::printStackTrace)
        );
    }

    private void loadSpawners() {
        SQLHelper.select("spawners", "", new QueryResult<ResultSet>()
                .onSuccess(resultSet -> {
                    while (resultSet.next()) {
                        parseLocationData(resultSet, true);
                    }
                })
                .onFail(Throwable::printStackTrace)
        );
    }

    private void loadBarrels() {
        SQLHelper.select("barrels", "", new QueryResult<ResultSet>()
                .onSuccess(resultSet -> {
                    while (resultSet.next()) {
                        parseLocationData(resultSet, false);
                    }
                })
                .onFail(Throwable::printStackTrace)
        );
    }

    private void parseLocationData(ResultSet resultSet, boolean isSpawner) {
        try {
            String location = resultSet.getString("location");
            String[] split = location.split(",");
            World world = Bukkit.getWorld(split[0]);
            if (world == null) throw new IllegalArgumentException("Null world");

            Location loc = new Location(world,
                    Integer.parseInt(split[1]),
                    Integer.parseInt(split[2]),
                    Integer.parseInt(split[3]));

            if (isSpawner) {
                int amount = resultSet.getInt("stackAmount");
                int upgrade = resultSet.getInt("upgrade");
                CACHED_SPAWNERS_RAW.computeIfAbsent(new ChunkPosition(loc), s -> Maps.newConcurrentMap())
                        .put(loc, new WUnloadedStackedSpawner(loc, amount, upgrade));
            } else {
                int amount = resultSet.getInt("stackAmount");
                String serialized = resultSet.getString("item");
                ItemStack item = serialized.isEmpty() ? null : plugin.getNMSAdapter().deserialize(serialized);
                CACHED_BARRELS_RAW.computeIfAbsent(new ChunkPosition(loc), s -> Maps.newConcurrentMap())
                        .put(loc, new WUnloadedStackedBarrel(loc, amount, item));
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to load " + (isSpawner ? "spawner" : "barrel") + ": " + ex.getMessage());
        }
    }

    public void addStackedSpawner(StackedSpawner spawner) {
        CACHED_SPAWNERS.put(spawner.getLocation(), spawner);
        CACHED_SPAWNERS_BY_CHUNKS.computeIfAbsent(new ChunkPosition(spawner.getLocation()), s -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(spawner);
    }

    public void removeStackedSpawner(StackedSpawner spawner) {
        CACHED_SPAWNERS.remove(spawner.getLocation());
        Set<StackedSpawner> chunk = CACHED_SPAWNERS_BY_CHUNKS.get(new ChunkPosition(spawner.getLocation()));
        if (chunk != null)
            chunk.remove(spawner);
        Executor.sync(() -> ((WStackedSpawner) spawner).removeHologram());
    }

    public void addStackedBarrel(StackedBarrel barrel) {
        CACHED_BARRELS.put(barrel.getLocation(), barrel);
        CACHED_BARRELS_BY_CHUNKS.computeIfAbsent(new ChunkPosition(barrel.getLocation()), s -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(barrel);
    }

    public void removeStackedBarrel(StackedBarrel barrel) {
        CACHED_BARRELS.remove(barrel.getLocation());
        Set<StackedBarrel> chunk = CACHED_BARRELS_BY_CHUNKS.get(new ChunkPosition(barrel.getLocation()));
        if (chunk != null)
            chunk.remove(barrel);
        barrel.removeDisplayBlock();
        Executor.sync(() -> ((WStackedBarrel) barrel).removeHologram());
    }

    public List<StackedObject> getStackedObjects() {
        List<StackedObject> all = new ArrayList<>();
        all.addAll(CACHED_ITEMS.values());
        all.addAll(CACHED_ENTITIES.values());
        all.addAll(CACHED_SPAWNERS.values());
        all.addAll(CACHED_BARRELS.values());
        return all;
    }
}