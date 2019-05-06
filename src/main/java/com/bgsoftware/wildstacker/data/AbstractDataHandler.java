package com.bgsoftware.wildstacker.data;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractDataHandler {

    protected final WildStackerPlugin plugin;

    public final Chunk DEFAULT_CHUNK = Bukkit.getWorlds().get(0).getChunkAt(0, 0);

    //public final Map<Object, StackedObject> CACHED_OBJECTS = new ConcurrentHashMap<>();
    public final StackedRegistry<UUID, StackedEntity> CACHED_ENTITIES = new StackedRegistry<>();
    public final StackedRegistry<UUID, StackedItem> CACHED_ITEMS = new StackedRegistry<>();
    public final StackedRegistry<Location, StackedSpawner> CACHED_SPAWNERS = new StackedRegistry<>();
    public final StackedRegistry<Location, StackedBarrel> CACHED_BARRELS = new StackedRegistry<>();

    public AbstractDataHandler(WildStackerPlugin plugin){
        this.plugin = plugin;
    }

    public abstract void loadChunkData(Chunk chunk);

    public abstract void saveChunkData(Chunk chunk, boolean remove, boolean async);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void clearDatabase(){
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if(dataFolder.exists()) {
            for (File worldFolder : Objects.requireNonNull(dataFolder.listFiles())) {
                if(worldFolder.isDirectory()) {
                    for (File chunkFile : Objects.requireNonNull(worldFolder.listFiles())) {
                        chunkFile.delete();
                    }
                }else{
                    worldFolder.delete();
                }
            }
        }
    }

    public void saveChunkData(boolean remove, boolean async){
        Set<Chunk> chunks = new HashSet<>();

        asList(CACHED_ENTITIES.iterator()).forEach(stackedEntity -> chunks.add(((WStackedEntity) stackedEntity).getChunk()));
        chunks.addAll(CACHED_ITEMS.getChunks());
        chunks.addAll(CACHED_SPAWNERS.getChunks());
        chunks.addAll(CACHED_BARRELS.getChunks());

        chunks.forEach(chunk -> saveChunkData(chunk, remove, async));
    }

    protected String getChunkLocation(Location location){
        return (location.getBlockX() & 15) + "," + location.getBlockY() + "," + (location.getBlockZ() & 15);
    }

    protected boolean isSameChunk(Chunk chunk, Location location){
        return chunk.getX() == (location.getBlockX() >> 4) && chunk.getZ() == (location.getBlockZ() >> 4);
    }

    protected List<StackedEntity> asList(Iterator<StackedEntity> iterator){
        return Lists.newArrayList(iterator).stream()
                .filter(stackedEntity -> stackedEntity.getLivingEntity() != null).collect(Collectors.toList());
    }

    protected class ChunkRegistry {

        private Map<UUID, LivingEntity> livingEntityMap = new HashMap<>();
        private Map<UUID, Item> itemMap = new HashMap<>();
        private Chunk chunk;

        public ChunkRegistry(Chunk chunk){
            this.chunk = chunk;
            Arrays.stream(chunk.getEntities())
                    .filter(entity -> entity instanceof LivingEntity)
                    .forEach(entity -> livingEntityMap.put(entity.getUniqueId(), (LivingEntity) entity));
            Arrays.stream(chunk.getEntities())
                    .filter(entity -> entity instanceof Item)
                    .forEach(entity -> itemMap.put(entity.getUniqueId(), (Item) entity));
        }

        public Block getBlock(int x, int y, int z){
            return chunk.getBlock(x, y, z);
        }

        @Nullable
        public LivingEntity getLivingEntity(UUID uuid){
            return livingEntityMap.get(uuid);
        }

        @Nullable
        public Item getItem(UUID uuid){
            return itemMap.get(uuid);
        }

        public File getFile(){
            return new File(plugin.getDataFolder(), "data/" + chunk.getWorld().getName() + "/" + chunk.getX() + "," + chunk.getZ() + ".yml");
        }

    }

}
