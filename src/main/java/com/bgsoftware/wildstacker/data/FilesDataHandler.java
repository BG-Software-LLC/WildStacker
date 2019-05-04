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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"WeakerAccess", "all"})
public final class FilesDataHandler extends AbstractDataHandler {

    //Here because we can't get the bukkit entity from an uuid if the chunk isn't loaded
    public final Map<UUID, CreatureSpawnEvent.SpawnReason> CACHED_SPAWN_REASON_ENTITIES = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> CACHED_AMOUNT_ENTITIES = new ConcurrentHashMap<>();
    public final Map<UUID, Integer> CACHED_AMOUNT_ITEMS = new ConcurrentHashMap<>();

    public FilesDataHandler(WildStackerPlugin plugin){
        super(plugin);
        Executor.sync(() -> {
            loadCachedEntities();
            loadCachedItems();
            loadCachedSpawners();
            loadCachedBarrels();
        }, 1L);
    }

    @Override
    public void loadChunkData(Chunk chunk){
        ChunkRegistry chunkRegistry = new ChunkRegistry(chunk);
        Executor.async(() -> {
            File file = chunkRegistry.getFile();

            if(!file.exists())
                return;

            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            CACHED_AMOUNT_ENTITIES.keySet().stream()
                    .filter(uuid -> chunkRegistry.getLivingEntity(uuid) != null)
                    .forEach(uuid -> cfg.set("entities." + uuid  + ".amount", CACHED_AMOUNT_ENTITIES.get(uuid)));

            CACHED_AMOUNT_ITEMS.keySet().stream()
                    .filter(uuid -> chunkRegistry.getItem(uuid) != null)
                    .forEach(uuid -> cfg.set("items." + uuid, CACHED_AMOUNT_ITEMS.get(uuid)));

            CACHED_SPAWN_REASON_ENTITIES.keySet().stream()
                    .filter(uuid -> chunkRegistry.getLivingEntity(uuid) != null)
                    .forEach(uuid -> cfg.set("entities." + uuid  + ".spawn-reason", CACHED_SPAWN_REASON_ENTITIES.get(uuid).name()));

            if(cfg.contains("entities")){
                for(String uuid : cfg.getConfigurationSection("entities").getKeys(false)) {
                    int stackAmount = cfg.getInt("entities." + uuid + ".amount", 1);
                    CreatureSpawnEvent.SpawnReason spawnReason =
                            CreatureSpawnEvent.SpawnReason.valueOf(cfg.getString("entities." + uuid + ".spawn-reason", "CHUNK_GEN"));

                    UUID _uuid = UUID.fromString(uuid);

                    StackedEntity stackedEntity = new WStackedEntity(chunkRegistry.getLivingEntity(_uuid), stackAmount, spawnReason);

                    //Entities are moving. There's no point in storing them based on chunks.
                    CACHED_ENTITIES.put(DEFAULT_CHUNK, _uuid, stackedEntity);

                    if(CACHED_AMOUNT_ENTITIES.containsKey(_uuid))
                        CACHED_AMOUNT_ENTITIES.remove(_uuid);

                    if(CACHED_SPAWN_REASON_ENTITIES.containsKey(_uuid))
                        CACHED_SPAWN_REASON_ENTITIES.remove(_uuid);
                }
            }

            if (cfg.contains("items")) {
                for(String uuid : cfg.getConfigurationSection("items").getKeys(false)) {
                    int stackAmount = cfg.getInt("items." + uuid, 1);
                    UUID _uuid = UUID.fromString(uuid);

                    StackedItem stackedItem = new WStackedItem(chunkRegistry.getItem(_uuid), stackAmount);

                    CACHED_ITEMS.put(chunk, _uuid, stackedItem);

                    if(CACHED_AMOUNT_ITEMS.containsKey(_uuid))
                        CACHED_AMOUNT_ITEMS.remove(_uuid);
                }
            }

            if(cfg.contains("spawners")){
                for(String location : cfg.getConfigurationSection("spawners").getKeys(false)) {
                    String[] locationSections = location.split(",");
                    int stackAmount = cfg.getInt("spawners." + location, 1);
                    Block spawnerBlock = chunkRegistry.getBlock(Integer.valueOf(locationSections[0]),
                            Integer.valueOf(locationSections[1]), Integer.valueOf(locationSections[2]));
                    if(spawnerBlock.getType() == Materials.SPAWNER.toBukkitType()){
                        StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) spawnerBlock.getState(), stackAmount);
                        CACHED_SPAWNERS.put(chunk, spawnerBlock.getLocation(), stackedSpawner);
                    }
                }
            }

            if(cfg.contains("barrels")){
                for(String location : cfg.getConfigurationSection("barrels").getKeys(false)) {
                    String[] locationSections = location.split(",");
                    int stackAmount = cfg.getInt("barrels." + location + ".amount", 1);
                    Block barrelBlock = chunkRegistry.getBlock(Integer.valueOf(locationSections[0]),
                            Integer.valueOf(locationSections[1]), Integer.valueOf(locationSections[2]));
                    if(barrelBlock.getType() == Material.CAULDRON){
                        ItemStack barrelItem = cfg.getItemStack("barrels." + location + ".item");
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
        });
    }

    @Override
    public void saveChunkData(boolean remove, boolean async) {
        super.saveChunkData(remove, async);

        if(!CACHED_SPAWN_REASON_ENTITIES.isEmpty() || !CACHED_AMOUNT_ENTITIES.isEmpty()){
            saveCachedEntities();
        }

        if(!CACHED_AMOUNT_ITEMS.isEmpty()){
            saveCachedItems();
        }
    }

    @Override
    public void saveChunkData(Chunk chunk, boolean remove, boolean async){
        if(async && Bukkit.isPrimaryThread()){
            Executor.async(() -> saveChunkData(chunk, remove, async));
            return;
        }

        File file = new File(plugin.getDataFolder(), "data/" + chunk.getWorld().getName() + "/" + chunk.getX() + "," + chunk.getZ() + ".yml");

        YamlConfiguration cfg = new YamlConfiguration();

        Iterator<StackedEntity> entities = CACHED_ENTITIES.iterator();
        while(entities.hasNext()){
            StackedEntity stackedEntity = entities.next();
            if(stackedEntity != null && stackedEntity.getLivingEntity() != null &&
                    isSameChunk(chunk, stackedEntity.getLivingEntity().getLocation())) {
                cfg.set("entities." + stackedEntity.getUniqueId() + ".amount", stackedEntity.getStackAmount());
                cfg.set("entities." + stackedEntity.getUniqueId() + ".spawn-reason", stackedEntity.getSpawnReason().name());
            }
        }

        Iterator<StackedItem> items = CACHED_ITEMS.iterator(chunk);
        while(items.hasNext()){
            StackedItem stackedItem = items.next();
            if(stackedItem.getStackAmount() > 1)
                cfg.set("items." + stackedItem.getUniqueId(), stackedItem.getStackAmount());
        }

        Iterator<StackedSpawner> spawners = CACHED_SPAWNERS.iterator(chunk);
        while(spawners.hasNext()){
            StackedSpawner stackedSpawner = spawners.next();
            if(stackedSpawner.getStackAmount() > 1)
                cfg.set("spawners." + getChunkLocation(stackedSpawner.getLocation()), stackedSpawner.getStackAmount());
            if(remove) plugin.getProviders().deleteHologram(stackedSpawner);
        }

        Iterator<StackedBarrel> barrels = CACHED_BARRELS.iterator(chunk);
        while(barrels.hasNext()){
            StackedBarrel stackedBarrel = barrels.next();
            String location = getChunkLocation(stackedBarrel.getLocation());
            cfg.set("barrels." + location + ".amount", stackedBarrel.getStackAmount());
            cfg.set("barrels." + location + ".item", stackedBarrel.getBarrelItem(1));
            if(remove) {
                plugin.getProviders().deleteHologram(stackedBarrel);
                stackedBarrel.removeDisplayBlock();
            }
        }

        if(cfg.contains("entities") || cfg.contains("items") || cfg.contains("spawners") || cfg.contains("barrels")) {
            try {
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }

                cfg.save(file);
            }catch(Throwable ex){
                throw new RuntimeException("Couldn't save the file " + file.getPath() + " - data won't be saved!");
            }

            if (remove) {
                asList(CACHED_ENTITIES.iterator()).stream()
                        .filter(stackedEntity -> isSameChunk(chunk, stackedEntity.getLivingEntity().getLocation()))
                        .forEach(stackedEntity -> CACHED_ENTITIES.remove(DEFAULT_CHUNK, stackedEntity.getUniqueId()));
                CACHED_ITEMS.remove(chunk);
                CACHED_SPAWNERS.remove(chunk);
                CACHED_BARRELS.remove(chunk);
            }
        }
    }

    /*
     *  Old Database
     */

    private void saveCachedEntities(){
        File file = new File(plugin.getDataFolder(), "data/entities.yml");

        YamlConfiguration cfg = new YamlConfiguration();

        for(UUID uuid : CACHED_AMOUNT_ENTITIES.keySet()) {
            if (CACHED_AMOUNT_ENTITIES.get(uuid) > 1) {
                cfg.set(uuid.toString(), CACHED_AMOUNT_ENTITIES.get(uuid));
            }
        }

        for(UUID uuid : CACHED_SPAWN_REASON_ENTITIES.keySet())
            cfg.set("spawn-reasons." + uuid, CACHED_SPAWN_REASON_ENTITIES.get(uuid).name());

        try {
            cfg.save(file);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void loadCachedEntities(){
        File file = new File(plugin.getDataFolder(), "data/entities.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        cfg.set("nerfed", null);

        for(String uuid : cfg.getConfigurationSection("").getKeys(false)) {
            if(!uuid.equals("spawn-reasons")) {
                CACHED_AMOUNT_ENTITIES.put(UUID.fromString(uuid), cfg.getInt(uuid));
            }
        }

        if(cfg.contains("spawn-reasons")){
            for(String uuid : cfg.getConfigurationSection("spawn-reasons").getKeys(false)){
                CACHED_SPAWN_REASON_ENTITIES.put(UUID.fromString(uuid), CreatureSpawnEvent.SpawnReason.valueOf(cfg.getString("spawn-reasons." + uuid)));
            }
        }

        file.delete();
    }

    private void saveCachedItems(){
        File file = new File(plugin.getDataFolder(), "data/items.yml");
        YamlConfiguration cfg = new YamlConfiguration();

        for(UUID uuid : CACHED_AMOUNT_ITEMS.keySet()) {
            if (CACHED_AMOUNT_ITEMS.get(uuid) > 1) {
                cfg.set(uuid.toString(), CACHED_AMOUNT_ITEMS.get(uuid));
            }
        }

        try {
            cfg.save(file);
        } catch(IOException e){
            e.printStackTrace();
        }

        file.delete();
    }

    private void loadCachedItems(){
        File file = new File(plugin.getDataFolder(), "data/items.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        for(String uuid : cfg.getConfigurationSection("").getKeys(false)) {
            CACHED_AMOUNT_ITEMS.put(UUID.fromString(uuid), cfg.getInt(uuid));
        }

        file.delete();
    }

    private void loadCachedSpawners(){
        File file = new File(plugin.getDataFolder(), "data/spawners.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        for(String location : cfg.getConfigurationSection("").getKeys(false)) {
            String[] sections = location.split(",");
            Location realLocation = new Location(Bukkit.getWorld(sections[0]), Integer.valueOf(sections[1]), Integer.valueOf(sections[2]), Integer.valueOf(sections[3]));
            if(realLocation.getBlock().getState() instanceof CreatureSpawner) {
                StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) realLocation.getBlock().getState());
                stackedSpawner.setStackAmount(cfg.getInt(location), false);
                CACHED_SPAWNERS.put(realLocation.getChunk(), realLocation, stackedSpawner);
                stackedSpawner.updateName();
            }
        }

        file.delete();
    }

    private void loadCachedBarrels(){
        File file = new File(plugin.getDataFolder(), "data/barrels.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        for(String location : cfg.getConfigurationSection("").getKeys(false)) {
            String[] sections = location.split(",");
            Location realLocation = new Location(Bukkit.getWorld(sections[0]), Integer.valueOf(sections[1]), Integer.valueOf(sections[2]), Integer.valueOf(sections[3]));
            StackedBarrel stackedBarrel = new WStackedBarrel(realLocation.getBlock(), cfg.getItemStack(location + ".item"));
            stackedBarrel.setStackAmount(cfg.getInt(location + ".amount"), false);
            CACHED_BARRELS.put(realLocation.getChunk(), realLocation, stackedBarrel);
            stackedBarrel.updateName();
            stackedBarrel.createDisplayBlock();
        }

        file.delete();
    }


}
