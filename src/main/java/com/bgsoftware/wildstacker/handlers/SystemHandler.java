package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.api.loot.LootTable;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedBarrel;
import com.bgsoftware.wildstacker.api.objects.UnloadedStackedSpawner;
import com.bgsoftware.wildstacker.api.spawning.SpawnCondition;
import com.bgsoftware.wildstacker.hooks.DataSerializer_Default;
import com.bgsoftware.wildstacker.hooks.IDataSerializer;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.objects.WStackedSnapshot;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedBarrel;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedSpawner;
import com.bgsoftware.wildstacker.tasks.ItemsMerger;
import com.bgsoftware.wildstacker.tasks.KillTask;
import com.bgsoftware.wildstacker.tasks.StackTask;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.data.DataSerializer;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumMap;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SystemHandler implements SystemManager {

    private final WildStackerPlugin plugin;
    private final DataHandler dataHandler;

    private final Set<UUID> itemsDisabledNames = new HashSet<>();
    private final Set<UUID> entitiesDisabledNames = new HashSet<>();

    private final FastEnumMap<EntityType, Set<SpawnCondition>> spawnConditions = new FastEnumMap<>(EntityType.class);
    private final Map<String, SpawnCondition> spawnConditionsIds = new HashMap<>();

    private IDataSerializer dataSerializer;

    public SystemHandler(WildStackerPlugin plugin){
        this.plugin = plugin;
        this.dataHandler = plugin.getDataHandler();
        this.dataSerializer = new DataSerializer_Default(plugin);

        //Start all required tasks
        Executor.sync(() -> {
            KillTask.start();
            StackTask.start();
            ItemsMerger.start();
        }, 1L);

        //Start the auto-clear
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::performCacheClear, 100L, 100L);
        //Start the auto-save
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> Executor.data(this::performCacheSave), 6000L, 6000L);
    }

    /*
     * StackedObject's methods
     */

    @Override
    public void removeStackObject(StackedObject stackedObject) {
        if(stackedObject instanceof StackedEntity)
            dataHandler.CACHED_ENTITIES.remove(((StackedEntity) stackedObject).getUniqueId());
        else if(stackedObject instanceof StackedItem)
            dataHandler.CACHED_ITEMS.remove(((StackedItem) stackedObject).getUniqueId());
        else if(stackedObject instanceof StackedSpawner)
            dataHandler.removeStackedSpawner((StackedSpawner) stackedObject);
        else if(stackedObject instanceof StackedBarrel)
            dataHandler.removeStackedBarrel((StackedBarrel) stackedObject);
    }

    @Override
    public StackedEntity getStackedEntity(LivingEntity livingEntity) {
        StackedEntity stackedEntity = dataHandler.CACHED_ENTITIES.get(livingEntity.getUniqueId());

        if(stackedEntity != null && stackedEntity.getLivingEntity() != null)
            return stackedEntity;

        if(!EntityUtils.isStackable(livingEntity))
            throw new IllegalArgumentException("Cannot convert " + livingEntity.getType() + " into a stacked entity.");

        //Entity wasn't found, creating a new object
        if(EntityStorage.hasMetadata(livingEntity, "spawn-cause"))
            stackedEntity = new WStackedEntity(livingEntity, 1, EntityStorage.getMetadata(livingEntity, "spawn-cause", SpawnCause.class));
        else
            stackedEntity = new WStackedEntity(livingEntity);

        //Checks if the entity still exists after a few ticks
        Executor.sync(() -> {
            if(livingEntity.isDead())
                dataHandler.CACHED_ENTITIES.remove(livingEntity.getUniqueId());
        }, 10L);

        Pair<Integer, SpawnCause> entityData = dataHandler.CACHED_ENTITIES_RAW.remove(livingEntity.getUniqueId());
        if(entityData != null) {
            stackedEntity.setStackAmount(entityData.getKey(), false);
            stackedEntity.setSpawnCause(entityData.getValue());
        }
        else{
            String cachedData = DataSerializer.deserializeData(stackedEntity.getCustomName());

            try {
                ((WStackedEntity) stackedEntity).setSaveEntity(false);
                if (!cachedData.isEmpty()) {
                    String[] dataSections = cachedData.split("-");
                    try {
                        stackedEntity.setStackAmount(Integer.parseInt(dataSections[0]), false);
                    } catch (Exception ignored) {
                    }
                    try {
                        stackedEntity.setSpawnCause(SpawnCause.valueOf(Integer.parseInt(dataSections[1])));
                    } catch (Exception ignored) {
                    }
                    try {
                        if (dataSections[2].equals("1"))
                            ((WStackedEntity) stackedEntity).setNameTag();
                    } catch (Exception ignored) {
                    }

                    stackedEntity.setCustomName(DataSerializer.stripData(stackedEntity.getCustomName()));
                } else {
                    loadEntity(stackedEntity);
                }
            }finally {
                ((WStackedEntity) stackedEntity).setSaveEntity(true);
            }
        }

        boolean shouldBeCached = stackedEntity.isCached();

        //A new entity was created. Let's see if we need to add him
        if(shouldBeCached)
            dataHandler.CACHED_ENTITIES.put(stackedEntity.getUniqueId(), stackedEntity);

        boolean deadFlag = shouldBeCached ? dataHandler.CACHED_DEAD_ENTITIES.remove(livingEntity.getUniqueId()) :
                dataHandler.CACHED_DEAD_ENTITIES.contains(livingEntity.getUniqueId());

        if(deadFlag)
            ((WStackedEntity) stackedEntity).setDeadFlag(true);

        return stackedEntity;
    }

    @Override
    public StackedItem getStackedItem(Item item) {
        StackedItem stackedItem = dataHandler.CACHED_ITEMS.get(item.getUniqueId());

        if(stackedItem != null && stackedItem.getItem() != null)
            return stackedItem;

        //Item wasn't found, creating a new object.
        stackedItem = new WStackedItem(item);

        //Checks if the item still exists after a few ticks
        Executor.sync(() -> {
            if(item.isDead())
                dataHandler.CACHED_ITEMS.remove(item.getUniqueId());
        }, 10L);

        //A new item was created. Let's see if we need to add him
        if(stackedItem.isCached())
            dataHandler.CACHED_ITEMS.put(stackedItem.getUniqueId(), stackedItem);

        Integer entityData = dataHandler.CACHED_ITEMS_RAW.remove(item.getUniqueId());
        if(entityData != null) {
            stackedItem.setStackAmount(entityData, false);
        }
        else{
            String cachedData = DataSerializer.deserializeData(stackedItem.getCustomName());

            try {
                ((WStackedItem) stackedItem).setSaveItem(false);
                if (!cachedData.isEmpty()) {
                    try {
                        stackedItem.setStackAmount(Integer.parseInt(cachedData), false);
                    } catch (Exception ignored) {
                    }

                    stackedItem.setCustomName(DataSerializer.stripData(stackedItem.getCustomName()));
                } else {
                    loadItem(stackedItem);
                }

                // We want to update the item's size if it's above max stack size.
                // We do it here so item will not be saved.
                if(stackedItem.getStackAmount() > stackedItem.getItemStack().getMaxStackSize())
                    stackedItem.setStackAmount(stackedItem.getStackAmount(), false);
            }finally {
                ((WStackedItem) stackedItem).setSaveItem(true);
            }
        }

        return stackedItem;
    }

    @Override
    public StackedSpawner getStackedSpawner(CreatureSpawner spawner) {
        return getStackedSpawner(spawner.getLocation());
    }

    @Override
    public StackedSpawner getStackedSpawner(Location location) {
        StackedSpawner stackedSpawner = dataHandler.CACHED_SPAWNERS.get(location);

        if(stackedSpawner != null)
            return stackedSpawner;

        //Spawner wasn't found, creating a new object
        stackedSpawner = new WStackedSpawner((CreatureSpawner) location.getBlock().getState());

        //A new spawner was created. Let's see if we need to add him
        if(stackedSpawner.isCached())
            dataHandler.addStackedSpawner(stackedSpawner);

        return stackedSpawner;
    }

    @Override
    public StackedBarrel getStackedBarrel(Block block) {
        return getStackedBarrel(block == null ? null : block.getLocation());
    }

    @Override
    public StackedBarrel getStackedBarrel(Location location) {
        StackedBarrel stackedBarrel = dataHandler.CACHED_BARRELS.get(location);

        if(stackedBarrel != null)
            return stackedBarrel;

        //Barrel wasn't found, creating a new object
        stackedBarrel = new WStackedBarrel(location.getBlock(), ItemUtils.getFromBlock(location.getBlock()));

        //A new barrel was created. Let's see if we need to add him
        if(stackedBarrel.isCached())
            dataHandler.addStackedBarrel(stackedBarrel);

        return stackedBarrel;
    }

    @Override
    public List<StackedEntity> getStackedEntities() {
        return new ArrayList<>(dataHandler.CACHED_ENTITIES.values());
    }

    @Override
    public List<StackedItem> getStackedItems() {
        return new ArrayList<>(dataHandler.CACHED_ITEMS.values());
    }

    @Override
    public List<StackedSpawner> getStackedSpawners(){
        return new ArrayList<>(dataHandler.CACHED_SPAWNERS.values());
    }

    @Override
    public List<StackedSpawner> getStackedSpawners(Chunk chunk) {
        return getStackedSpawners(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    @Override
    public List<StackedSpawner> getStackedSpawners(World world, int chunkX, int chunkZ) {
        Set<StackedSpawner> chunkSpawners = dataHandler.CACHED_SPAWNERS_BY_CHUNKS.get(new ChunkPosition(world.getName(), chunkX, chunkZ));
        return chunkSpawners == null ? new ArrayList<>() : new ArrayList<>(chunkSpawners);
    }

    @Override
    public List<UnloadedStackedSpawner> getAllStackedSpawners() {
        List<UnloadedStackedSpawner> spawners = new ArrayList<>();

        dataHandler.CACHED_SPAWNERS.values().forEach(stackedSpawner -> spawners.add(new WUnloadedStackedSpawner(stackedSpawner)));
        dataHandler.CACHED_SPAWNERS_RAW.values().forEach(map -> spawners.addAll(map.values()));

        return spawners;
    }

    @Override
    public List<StackedBarrel> getStackedBarrels(){
        return new ArrayList<>(dataHandler.CACHED_BARRELS.values());
    }

    @Override
    public List<StackedBarrel> getStackedBarrels(Chunk chunk) {
        return getStackedBarrels(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    @Override
    public List<StackedBarrel> getStackedBarrels(World world, int chunkX, int chunkZ) {
        Set<StackedBarrel> chunkBarrels = dataHandler.CACHED_BARRELS_BY_CHUNKS.get(new ChunkPosition(world.getName(), chunkX, chunkZ));
        return chunkBarrels == null ? new ArrayList<>() : new ArrayList<>(chunkBarrels);
    }

    @Override
    public List<UnloadedStackedBarrel> getAllStackedBarrels() {
        List<UnloadedStackedBarrel> barrels = new ArrayList<>();

        dataHandler.CACHED_BARRELS.values().forEach(stackedBarrel -> barrels.add(new WUnloadedStackedBarrel(stackedBarrel)));
        dataHandler.CACHED_BARRELS_RAW.values().forEach(map -> barrels.addAll(map.values()));

        return barrels;
    }

    @Override
    public boolean isStackedSpawner(Block block) {
        return block != null && block.getType() == Materials.SPAWNER.toBukkitType() && isStackedSpawner(block.getLocation());
    }

    @Override
    public boolean isStackedSpawner(Location location) {
        return location != null && dataHandler.CACHED_SPAWNERS.containsKey(location);
    }

    @Override
    public boolean isStackedBarrel(Block block) {
        return block != null && block.getType() == Material.CAULDRON && isStackedBarrel(block.getLocation());
    }

    @Override
    public boolean isStackedBarrel(Location location) {
        return dataHandler.CACHED_BARRELS.containsKey(location);
    }

    public boolean isBarrelBlock(Material blockType, World world){
        return (plugin.getSettings().whitelistedBarrels.size() == 0 ||
                plugin.getSettings().whitelistedBarrels.contains(blockType)) &&
                !plugin.getSettings().blacklistedBarrels.contains(blockType) &&
                !plugin.getSettings().barrelsDisabledWorlds.contains(world.getName());
    }

    @Override
    public void performCacheClear() {
        List<StackedObject> stackedObjects = dataHandler.getStackedObjects();

        for(StackedObject stackedObject : stackedObjects){
            if(stackedObject instanceof StackedItem){
                StackedItem stackedItem = (StackedItem) stackedObject;
                if(stackedItem.getItem() == null || (GeneralUtils.isChunkLoaded(stackedItem.getItem().getLocation()) && stackedItem.getItem().isDead()))
                    removeStackObject(stackedObject);
            }

            else if(stackedObject instanceof StackedEntity){
                StackedEntity stackedEntity = (StackedEntity) stackedObject;
                if(stackedEntity.getLivingEntity() == null || (GeneralUtils.isChunkLoaded(stackedEntity.getLivingEntity().getLocation()) &&
                        stackedEntity.getLivingEntity().isDead() && !((WStackedEntity) stackedEntity).hasDeadFlag()))
                    removeStackObject(stackedObject);
                else
                    stackedEntity.updateNerfed();
            }

            else if(stackedObject instanceof StackedSpawner){
                StackedSpawner stackedSpawner = (StackedSpawner) stackedObject;
                if(GeneralUtils.isChunkLoaded(stackedSpawner.getLocation()) && !isStackedSpawner(stackedSpawner.getSpawner().getBlock())) {
                    removeStackObject(stackedObject);
                }
            }

            else if(stackedObject instanceof StackedBarrel){
                StackedBarrel stackedBarrel = (StackedBarrel) stackedObject;
                if(GeneralUtils.isChunkLoaded(stackedBarrel.getLocation()) && !isStackedBarrel(stackedBarrel.getBlock())) {
                    removeStackObject(stackedObject);
                    stackedBarrel.removeDisplayBlock();
                }
            }
        }
    }

    @Override
    public void performCacheSave() {
    }

    @Override
    public void updateLinkedEntity(LivingEntity livingEntity, LivingEntity newLivingEntity) {
        for(StackedSpawner stackedSpawner : getStackedSpawners()){
            LivingEntity linkedEntity = ((WStackedSpawner) stackedSpawner).getRawLinkedEntity();
            if(linkedEntity != null && linkedEntity.equals(livingEntity))
                stackedSpawner.setLinkedEntity(newLivingEntity);
        }
    }

    public void loadSpawners(Chunk chunk){
        ChunkPosition chunkPosition = new ChunkPosition(chunk);
        Map<Location, UnloadedStackedSpawner> spawnersToLoad = dataHandler.CACHED_SPAWNERS_RAW.remove(chunkPosition);

        if(spawnersToLoad != null){
            for (UnloadedStackedSpawner unloadedStackedSpawner : spawnersToLoad.values()) {
                Location location = unloadedStackedSpawner.getLocation();
                if (GeneralUtils.isSameChunk(location, chunk)) {
                    Block block = location.getBlock();

                    if (block.getType() == Materials.SPAWNER.toBukkitType()) {
                        StackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) block.getState());
                        stackedSpawner.setStackAmount(unloadedStackedSpawner.getStackAmount(), true);
                        dataHandler.addStackedSpawner(stackedSpawner);
                    }
                }
            }

            spawnersToLoad.clear();
        }

        if(plugin.getSettings().spawnersOverrideEnabled){
            Arrays.stream(chunk.getTileEntities()).filter(blockState -> blockState instanceof CreatureSpawner)
                    .forEach(blockState -> plugin.getNMSSpawners().updateStackedSpawner(WStackedSpawner.of(blockState.getBlock())));
        }
    }

    public void loadBarrels(Chunk chunk){
        ChunkPosition chunkPosition = new ChunkPosition(chunk);
        Map<Location, UnloadedStackedBarrel> barrelsToLoad = dataHandler.CACHED_BARRELS_RAW.remove(chunkPosition);

        if(barrelsToLoad != null){
            for (UnloadedStackedBarrel unloadedStackedBarrel : barrelsToLoad.values()) {
                Location location = unloadedStackedBarrel.getLocation();
                if(GeneralUtils.isSameChunk(location, chunk)){
                    Block block = location.getBlock();

                    if(block.getType() == Material.CAULDRON){
                        StackedBarrel stackedBarrel = new WStackedBarrel(block, unloadedStackedBarrel.getBarrelItem(1));
                        stackedBarrel.setStackAmount(unloadedStackedBarrel.getStackAmount(), true);
                        stackedBarrel.createDisplayBlock();
                        dataHandler.addStackedBarrel(stackedBarrel);
                    }
                }
            }

            barrelsToLoad.clear();
        }
    }

    public void handleChunkLoad(Chunk chunk){
        Entity[] entities = chunk.getEntities();

        // We substring names if necessary
        Arrays.stream(entities).filter(entity -> {
            String customName = plugin.getNMSAdapter().getCustomName(entity);
            return customName != null && customName.length() > 256;
        }).forEach(entity -> {
            String customName = plugin.getNMSAdapter().getCustomName(entity);
            plugin.getNMSAdapter().setCustomName(entity, customName.substring(0, 256));
        });

        if(ServerVersion.isAtLeast(ServerVersion.v1_8)) {
            //Trying to remove all the corrupted stacked blocks
            Executor.async(() -> {
                Stream<Entity> entityStream = Arrays.stream(entities).filter(entity -> {
                    String customName = plugin.getNMSAdapter().getCustomName(entity);
                    return entity instanceof ArmorStand && customName != null && customName.equals("BlockDisplay") &&
                            !isStackedBarrel(entity.getLocation().getBlock());
                });
                Executor.sync(() -> entityStream.forEach(entity -> {
                    Block block = entity.getLocation().getBlock();
                    if (block.getType() == Material.CAULDRON)
                        block.setType(Material.AIR);
                    entity.remove();
                }));
            });

            loadBarrels(chunk);
        }

        loadSpawners(chunk);

        //Update nerf status & names to all entities
        Executor.async(() -> Arrays.stream(entities).filter(EntityUtils::isStackable).forEach(entity -> {
            StackedEntity stackedEntity = WStackedEntity.of(entity);
            stackedEntity.updateNerfed();
            stackedEntity.updateName();
        }));
    }

    public void handleChunkUnload(Chunk chunk){
        Entity[] entities = chunk.getEntities();

        Executor.async(() -> {
            for(Entity entity : entities){
                if(EntityUtils.isStackable(entity)){
                    dataHandler.CACHED_ENTITIES.remove(entity.getUniqueId());
                }
                else if(entity instanceof Item){
                    dataHandler.CACHED_ITEMS.remove(entity.getUniqueId());
                }
            }
        });

        for(StackedSpawner stackedSpawner : getStackedSpawners(chunk)){
            dataHandler.removeStackedSpawner(stackedSpawner);
            if(stackedSpawner.getStackAmount() > 1) {
                dataHandler.CACHED_SPAWNERS_RAW.computeIfAbsent(new ChunkPosition(stackedSpawner.getLocation()), s -> Maps.newConcurrentMap())
                        .put(stackedSpawner.getLocation(), new WUnloadedStackedSpawner(stackedSpawner));
            }
        }

        for(StackedBarrel stackedBarrel : getStackedBarrels(chunk)){
            dataHandler.removeStackedBarrel(stackedBarrel);
            dataHandler.CACHED_BARRELS_RAW.computeIfAbsent(new ChunkPosition(stackedBarrel.getLocation()), s -> Maps.newConcurrentMap())
                    .put(stackedBarrel.getLocation(), new WUnloadedStackedBarrel(stackedBarrel));
            stackedBarrel.removeDisplayBlock();
        }
    }

    /*
     * General methods
     */

    @Override
    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type){
        return spawnEntityWithoutStacking(location, type, SpawnCause.SPAWNER);
    }

    @Override
    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, CreatureSpawnEvent.SpawnReason spawnReason) {
        return spawnEntityWithoutStacking(location, type, SpawnCause.valueOf(spawnReason));
    }

    @Override
    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, SpawnCause spawnCause) {
        return spawnEntityWithoutStacking(location, type, spawnCause, null, null);
    }

    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer){
        return plugin.getNMSAdapter().createEntity(location, type, spawnCause, entity -> {
            EntitiesListener.noStackEntities.add(entity.getUniqueId());
            if(beforeSpawnConsumer != null)
                beforeSpawnConsumer.accept(entity);
        }, afterSpawnConsumer);
    }

    @Override
    public StackedItem spawnItemWithAmount(Location location, ItemStack itemStack) {
        return spawnItemWithAmount(location, itemStack, itemStack.getAmount());
    }

    @Override
    public StackedItem spawnItemWithAmount(Location location, ItemStack itemStack, int amount) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        location = location.clone();

        location.setX(location.getX() + (random.nextFloat() * 0.5F) + 0.25D);
        location.setY(location.getY() + (random.nextFloat() * 0.5F) + 0.25D);
        location.setZ(location.getZ() + (random.nextFloat() * 0.5F) + 0.25D);

        int limit = plugin.getSettings().itemsLimits.getOrDefault(itemStack.getType(), Integer.MAX_VALUE);
        limit = limit < 1 ? Integer.MAX_VALUE : limit;

        int amountOfItems = amount / limit;

        int itemLimit = limit;

        StackedItem lastDroppedItem = null;

        for(int i = 0; i < amountOfItems; i++) {
            itemStack = itemStack.clone();
            itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), itemLimit));
            lastDroppedItem = plugin.getNMSAdapter().createItem(location, itemStack, SpawnCause.CUSTOM,
                    stackedItem -> stackedItem.setStackAmount(itemLimit, stackedItem.isCached()));
        }

        int leftOvers = amount % limit;

        if(leftOvers > 0) {
            itemStack = itemStack.clone();
            itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), leftOvers));
            lastDroppedItem = plugin.getNMSAdapter().createItem(location, itemStack, SpawnCause.CUSTOM,
                    stackedItem -> stackedItem.setStackAmount(leftOvers, stackedItem.isCached()));
        }

        return lastDroppedItem;
    }

    @Override
    public void spawnCorpse(StackedEntity stackedEntity) {
        Class<? extends Entity> entityClass = stackedEntity.getType().getEntityClass();

        if(entityClass == null)
            return;

        LivingEntity livingEntity = (LivingEntity) spawnEntityWithoutStacking(stackedEntity.getLocation(), entityClass, SpawnCause.CUSTOM, entity -> {
            // Marking the entity as a corpse before the actual spawning
            EntityStorage.setMetadata(entity, "corpse", null);
        }, entity -> {
            // Updating the entity values after the actual spawning
            plugin.getNMSAdapter().updateEntity(stackedEntity.getLivingEntity(), (LivingEntity) entity);
        });

        if(livingEntity != null){
            Executor.sync(() -> {
                plugin.getNMSAdapter().playDeathSound(livingEntity);
                livingEntity.setHealth(0);
            }, 2L);
        }
    }

    @Override
    public void performKillAll(){
        performKillAll(false);
    }

    @Override
    public void performKillAll(boolean applyTaskFilter) {
        performKillAll(entity -> true, item -> true, applyTaskFilter);
    }

    @Override
    public void performKillAll(Predicate<Entity> entityPredicate, Predicate<Item> itemPredicate) {
        performKillAll(entityPredicate, itemPredicate, false);
    }

    @Override
    public void performKillAll(Predicate<Entity> entityPredicate, Predicate<Item> itemPredicate, boolean applyTaskFilter) {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> performKillAll(entityPredicate, itemPredicate, applyTaskFilter));
            return;
        }

        List<Entity> entityList = new ArrayList<>();

        for(World world : Bukkit.getWorlds()){
            if(!applyTaskFilter || plugin.getSettings().killTaskEntitiesWorlds.isEmpty() ||
                    plugin.getSettings().killTaskEntitiesWorlds.contains(world.getName())) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    entityList.addAll(Arrays.stream(chunk.getEntities())
                            .filter(entity -> entity instanceof LivingEntity).collect(Collectors.toList()));
                }
            }
            if(!applyTaskFilter || plugin.getSettings().killTaskItemsWorlds.isEmpty() ||
                    plugin.getSettings().killTaskItemsWorlds.contains(world.getName())) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    entityList.addAll(Arrays.stream(chunk.getEntities())
                            .filter(entity -> entity instanceof Item).collect(Collectors.toList()));
                }
            }
        }

        Executor.async(() -> {
            entityList.stream()
                    .filter(entity -> EntityUtils.isStackable(entity) && entityPredicate.test(entity) &&
                            (!applyTaskFilter || (GeneralUtils.containsOrEmpty(plugin.getSettings().killTaskEntitiesWhitelist, WStackedEntity.of(entity)) &&
                                    !GeneralUtils.contains(plugin.getSettings().killTaskEntitiesBlacklist, WStackedEntity.of(entity)))))
                    .forEach(entity -> {
                        StackedEntity stackedEntity = WStackedEntity.of(entity);
                        if(!applyTaskFilter || (((plugin.getSettings().killTaskStackedEntities && stackedEntity.getStackAmount() > 1) ||
                                (plugin.getSettings().killTaskUnstackedEntities && stackedEntity.getStackAmount() <= 1)) && !stackedEntity.hasNameTag()))
                            stackedEntity.remove();
                    });

            if(plugin.getSettings().killTaskStackedItems) {
                entityList.stream()
                        .filter(entity -> ItemUtils.isStackable(entity) && ItemUtils.canPickup((Item) entity) && itemPredicate.test((Item) entity) &&
                                (!applyTaskFilter || (GeneralUtils.containsOrEmpty(plugin.getSettings().killTaskItemsWhitelist, ((Item) entity).getItemStack().getType()) &&
                                        !plugin.getSettings().killTaskItemsBlacklist.contains(((Item) entity).getItemStack().getType()))))
                        .forEach(entity -> {
                            StackedItem stackedItem = WStackedItem.of(entity);
                            int maxStackSize = ((Item) entity).getItemStack().getMaxStackSize();
                            if(!applyTaskFilter || stackedItem.getStackAmount() > maxStackSize ||
                                    (plugin.getSettings().killTaskUnstackedItems && stackedItem.getStackAmount() <= maxStackSize))
                                stackedItem.remove();
                        });
            }

            for(Player pl : Bukkit.getOnlinePlayers()) {
                if (pl.isOp())
                    Locale.KILL_ALL_OPS.send(pl);
            }
        });
    }

    @Override
    public StackedSnapshot getStackedSnapshot(Chunk chunk, boolean loadData) {
        return getStackedSnapshot(chunk);
    }

    @Override
    public StackedSnapshot getStackedSnapshot(Chunk chunk) {
        chunk.load(false);
        return new WStackedSnapshot(chunk);
    }

    /*
     * Loot loot methods
     */

    @Override
    public LootTable getLootTable(LivingEntity livingEntity) {
        return plugin.getLootHandler().getLootTable(livingEntity);
    }

    /*
     * Names toggle methods
     */

    @Override
    public boolean hasItemNamesToggledOff(Player player) {
        return itemsDisabledNames.contains(player.getUniqueId());
    }

    @Override
    public void toggleItemNames(Player player) {
        // If the set contained the uuid (aka, the player had items toggled off), he will be removed and nothing else.
        // If the set didn't contain the uuid, then the player will be added to it.
        if(!itemsDisabledNames.remove(player.getUniqueId()))
            itemsDisabledNames.add(player.getUniqueId());
    }

    @Override
    public boolean hasEntityNamesToggledOff(Player player) {
        return entitiesDisabledNames.contains(player.getUniqueId());
    }

    @Override
    public void toggleEntityNames(Player player) {
        // If the set contained the uuid (aka, the player had entities toggled off), he will be removed and nothing else.
        // If the set didn't contain the uuid, then the player will be added to it.
        if(!entitiesDisabledNames.remove(player.getUniqueId()))
            entitiesDisabledNames.add(player.getUniqueId());
    }

    /*
     * Spawn condition methods
     */

    @Override
    public void addSpawnCondition(SpawnCondition spawnCondition, EntityType... entityTypes) {
        for(EntityType entityType : entityTypes)
            spawnConditions.computeIfAbsent(entityType, new HashSet<>(1)).add(spawnCondition);
    }

    @Override
    public Collection<SpawnCondition> getSpawnConditions(EntityType entityType) {
        return Collections.unmodifiableSet(spawnConditions.getOrDefault(entityType, new HashSet<>()));
    }

    @Override
    public void removeSpawnCondition(EntityType entityType, SpawnCondition spawnCondition) {
        Set<SpawnCondition> spawnConditionSet = spawnConditions.get(entityType);
        if(spawnConditionSet != null)
            spawnConditionSet.remove(spawnCondition);
    }

    @Override
    public void clearSpawnConditions(EntityType entityType) {
        spawnConditions.remove(entityType);
    }

    @Override
    public Optional<SpawnCondition> getSpawnCondition(String id) {
        return Optional.ofNullable(spawnConditionsIds.get(id.toLowerCase()));
    }

    @Override
    public SpawnCondition registerSpawnCondition(SpawnCondition spawnCondition) {
        spawnConditionsIds.put(spawnCondition.getId().toLowerCase(), spawnCondition);
        return spawnCondition;
    }

    /*
     * Data serialization methods
     */

    public void setDataSerializer(IDataSerializer dataSerializer){
        this.dataSerializer = dataSerializer;
    }

    public void saveEntity(StackedEntity stackedEntity){
        dataSerializer.saveEntity(stackedEntity);
    }

    public void loadEntity(StackedEntity stackedEntity){
        dataSerializer.loadEntity(stackedEntity);
    }

    public void saveItem(StackedItem stackedItem){
        dataSerializer.saveItem(stackedItem);
    }

    public void loadItem(StackedItem stackedItem){
        dataSerializer.loadItem(stackedItem);
    }

}
