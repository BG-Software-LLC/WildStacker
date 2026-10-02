package com.bgsoftware.wildstacker.handlers;

import com.bgsoftware.common.databasebridge.sql.transaction.SQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.transaction.IDatabaseTransaction;
import com.bgsoftware.wildstacker.Locale;
import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.api.loot.LootEntityAttributes;
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
import com.bgsoftware.wildstacker.database.DBSession;
import com.bgsoftware.wildstacker.hooks.DataSerializer_Default;
import com.bgsoftware.wildstacker.hooks.IDataSerializer;
import com.bgsoftware.wildstacker.loot.entity.EntityLootDataBuilder;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.objects.WStackedObject;
import com.bgsoftware.wildstacker.objects.WStackedSnapshot;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedBarrel;
import com.bgsoftware.wildstacker.objects.WUnloadedStackedSpawner;
import com.bgsoftware.wildstacker.tasks.ItemsMerger;
import com.bgsoftware.wildstacker.tasks.KillTask;
import com.bgsoftware.wildstacker.tasks.StackTask;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.ServerVersion;
import com.bgsoftware.wildstacker.utils.data.structures.FastEnumMap;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.google.common.base.Preconditions;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class SystemHandler implements SystemManager {

    public static final int ENTITIES_STAGE = (1 << 0);
    public static final int CHUNK_STAGE = (1 << 1);
    public static final int CHUNK_FULL_STAGE = ENTITIES_STAGE | CHUNK_STAGE;

    @Nullable
    private static final Material WATER_CAULDRON = Materials.getMaterialOrNull("WATER_CAULDRON");

    private final WildStackerPlugin plugin;
    private final DataHandler dataHandler;

    private final Set<UUID> itemsDisabledNames = new HashSet<>();
    private final Set<UUID> entitiesDisabledNames = new HashSet<>();

    private final FastEnumMap<EntityType, Set<SpawnCondition>> spawnConditions = new FastEnumMap<>(EntityType.class);
    private final Map<String, SpawnCondition> spawnConditionsIds = new HashMap<>();
    private boolean loadedData = false;

    private IDataSerializer dataSerializer;

    public SystemHandler(WildStackerPlugin plugin) {
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
        Bukkit.getScheduler().runTaskTimer(plugin, this::performCacheSave, 300L, 300L);
    }

    public IDataSerializer getDataSerializer() {
        return dataSerializer;
    }

    /*
     * StackedObject's methods
     */

    @Override
    public void removeStackObject(StackedObject stackedObject) {
        if (stackedObject instanceof StackedEntity) {
            dataHandler.stackedEntityStore.remove(((StackedEntity) stackedObject).getLivingEntity().getEntityId());
            ((StackedEntity) stackedObject).clearFlags();
        } else if (stackedObject instanceof StackedItem)
            dataHandler.stackedItemStore.remove(((StackedItem) stackedObject).getItem().getEntityId());
        else if (stackedObject instanceof StackedSpawner)
            dataHandler.removeStackedSpawner((StackedSpawner) stackedObject);
        else if (stackedObject instanceof StackedBarrel)
            dataHandler.removeStackedBarrel((StackedBarrel) stackedObject);

        markToBeUnsaved(stackedObject);
    }

    @Override
    public StackedEntity getStackedEntity(LivingEntity livingEntity) {
        StackedEntity stackedEntity = dataHandler.stackedEntityStore.get(livingEntity.getEntityId());
        if (stackedEntity != null)
            return stackedEntity;

        if (!EntityUtils.isStackable(livingEntity))
            throw new IllegalArgumentException("Cannot convert " + livingEntity.getType() + " into a stacked entity.");

        //Entity wasn't found, creating a new object
        stackedEntity = new WStackedEntity(livingEntity);

        dataHandler.stackedEntityStore.loadUnloaded(livingEntity.getUniqueId(), stackedEntity);

        boolean shouldBeCached = stackedEntity.isCached() || stackedEntity.getStackAmount() > 1 ||
                !stackedEntity.isDefaultUpgrade();

        //A new entity was created. Let's see if we need to add him
        if (shouldBeCached)
            dataHandler.stackedEntityStore.store(livingEntity.getEntityId(), stackedEntity);

        boolean isDead = dataHandler.stackedEntityStore.isDead(livingEntity.getUniqueId(), shouldBeCached);
        if (isDead)
            ((WStackedEntity) stackedEntity).setDeadFlag(true);

        return stackedEntity;
    }

    @Override
    public StackedItem getStackedItem(Item item) {
        StackedItem stackedItem = dataHandler.stackedItemStore.get(item.getEntityId());
        if (stackedItem != null) {
            return stackedItem;
        }

        //Item wasn't found, creating a new object.
        stackedItem = new WStackedItem(item);

        //A new item was created. Let's see if we need to add him
        if (stackedItem.isCached()) {
            dataHandler.stackedItemStore.store(item.getEntityId(), stackedItem);

            //Checks if the item still exists after a few ticks
            Executor.sync(() -> {
                if (item.isDead())
                    dataHandler.stackedItemStore.remove(item.getEntityId());
            }, 10L);
        }

        dataHandler.stackedItemStore.loadUnloaded(item.getUniqueId(), stackedItem);

        return stackedItem;
    }

    @Override
    public StackedSpawner getStackedSpawner(CreatureSpawner spawner) {
        return getStackedSpawner(spawner.getLocation());
    }

    @Override
    public StackedSpawner getStackedSpawner(Location location) {
        StackedSpawner stackedSpawner = dataHandler.stackedSpawnerStore.get(location);
        if (stackedSpawner != null)
            return stackedSpawner;

        //Spawner wasn't found, creating a new object
        stackedSpawner = new WStackedSpawner((CreatureSpawner) location.getBlock().getState());

        //A new spawner was created. Let's see if we need to add him
        if (stackedSpawner.isCached())
            dataHandler.addStackedSpawner(stackedSpawner);

        return stackedSpawner;
    }

    @Override
    public StackedBarrel getStackedBarrel(Block block) {
        return getStackedBarrel(block == null ? null : block.getLocation());
    }

    @Override
    public StackedBarrel getStackedBarrel(Location location) {
        StackedBarrel stackedBarrel = dataHandler.stackedBarrelStore.get(location);
        if (stackedBarrel != null)
            return stackedBarrel;

        //Barrel wasn't found, creating a new object
        stackedBarrel = new WStackedBarrel(location.getBlock(), ItemUtils.getFromBlock(location.getBlock()));

        //A new barrel was created. Let's see if we need to add him
        if (stackedBarrel.isCached())
            dataHandler.addStackedBarrel(stackedBarrel);

        return stackedBarrel;
    }

    @Override
    public List<StackedEntity> getStackedEntities() {
        return dataHandler.stackedEntityStore.getAll();
    }

    @Override
    public List<StackedItem> getStackedItems() {
        return dataHandler.stackedItemStore.getAll();
    }

    @Override
    public List<StackedSpawner> getStackedSpawners() {
        return dataHandler.stackedSpawnerStore.getAll();
    }

    @Override
    public List<StackedSpawner> getStackedSpawners(Chunk chunk) {
        return getStackedSpawners(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    @Override
    public List<StackedSpawner> getStackedSpawners(World world, int chunkX, int chunkZ) {
        return dataHandler.stackedSpawnerStore.getAllFromChunk(world.getName(), chunkX, chunkZ);
    }

    @Override
    public List<UnloadedStackedSpawner> getAllStackedSpawners() {
        if (dataHandler.stackedSpawnerStore.size() == 0 && dataHandler.stackedSpawnerStore.sizeUnloaded() == 0)
            return Collections.emptyList();

        List<UnloadedStackedSpawner> spawners = new LinkedList<>();

        dataHandler.stackedSpawnerStore.iterateUnloaded(spawners::add);
        dataHandler.stackedSpawnerStore.getAll().forEach(stackedSpawner ->
                spawners.add(new WUnloadedStackedSpawner(stackedSpawner)));

        return spawners.isEmpty() ? Collections.emptyList() : spawners;
    }

    @Override
    public List<StackedBarrel> getStackedBarrels() {
        return dataHandler.stackedBarrelStore.getAll();
    }

    @Override
    public List<StackedBarrel> getStackedBarrels(Chunk chunk) {
        return getStackedBarrels(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    @Override
    public List<StackedBarrel> getStackedBarrels(World world, int chunkX, int chunkZ) {
        return dataHandler.stackedBarrelStore.getAllFromChunk(world.getName(), chunkX, chunkZ);
    }

    @Override
    public List<UnloadedStackedBarrel> getAllStackedBarrels() {
        if (dataHandler.stackedBarrelStore.size() == 0 && dataHandler.stackedBarrelStore.sizeUnloaded() == 0)
            return Collections.emptyList();

        List<UnloadedStackedBarrel> barrels = new LinkedList<>();

        dataHandler.stackedBarrelStore.iterateUnloaded(barrels::add);
        dataHandler.stackedBarrelStore.getAll().forEach(stackedBarrel ->
                barrels.add(new WUnloadedStackedBarrel(stackedBarrel)));

        return barrels.isEmpty() ? Collections.emptyList() : barrels;
    }

    @Override
    public boolean isStackedSpawner(Block block) {
        return block != null && block.getType() == Materials.SPAWNER.toBukkitType() && isStackedSpawner(block.getLocation());
    }

    @Override
    public boolean isStackedSpawner(Location location) {
        return location != null && dataHandler.stackedSpawnerStore.get(location) != null;
    }

    @Override
    public boolean isStackedBarrel(Block block) {
        return block != null && block.getType() == Material.CAULDRON && isStackedBarrel(block.getLocation());
    }

    @Override
    public boolean isStackedBarrel(Location location) {
        return dataHandler.stackedBarrelStore.get(location) != null;
    }

    @Override
    public void performCacheClear() {
        List<StackedObject> stackedObjects = dataHandler.getStackedObjects();

        for (StackedObject stackedObject : stackedObjects) {
            if (!((WStackedObject<?>) stackedObject).isReady()) {
                // In the case the object was marked as not ready, ignore it.
                continue;
            }

            if (stackedObject instanceof StackedItem) {
                StackedItem stackedItem = (StackedItem) stackedObject;
                if (stackedItem.getItem() == null || (GeneralUtils.isChunkLoaded(stackedItem.getItem().getLocation()) && stackedItem.getItem().isDead()))
                    removeStackObject(stackedObject);
            } else if (stackedObject instanceof StackedEntity) {
                StackedEntity stackedEntity = (StackedEntity) stackedObject;
                if (stackedEntity.getLivingEntity() == null || (
                        GeneralUtils.isChunkLoaded(stackedEntity.getLivingEntity().getLocation()) &&
                                (stackedEntity.getLivingEntity().isDead() && !hasImportantFlags(stackedEntity))) ||
                        !EntityUtils.isStackable(stackedEntity.getLivingEntity())) {
                    removeStackObject(stackedObject);
                } else {
                    stackedEntity.updateNerfed();
                }
            } else if (stackedObject instanceof StackedSpawner) {
                StackedSpawner stackedSpawner = (StackedSpawner) stackedObject;
                if (GeneralUtils.isChunkLoaded(stackedSpawner.getLocation()) && !isStackedSpawner(stackedSpawner.getSpawner().getBlock())) {
                    removeStackObject(stackedObject);
                }
            } else if (stackedObject instanceof StackedBarrel) {
                StackedBarrel stackedBarrel = (StackedBarrel) stackedObject;
                Block block = stackedBarrel.getBlock();
                if (GeneralUtils.isChunkLoaded(stackedBarrel.getLocation()) && !isStackedBarrel(block)) {
                    // In some versions, cauldron material can be WATER_CAULDRON.
                    // Instead of removing the barrel, we just want to set it to CAULDRON.
                    if (WATER_CAULDRON != null && block.getType() == WATER_CAULDRON) {
                        Executor.sync(() -> block.setType(Material.CAULDRON));
                    } else {
                        removeStackObject(stackedObject);
                        stackedBarrel.removeDisplayBlock();
                    }
                }
            }
        }
    }

    @Override
    public void performCacheSave() {
        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(this::performCacheSave);
            return;
        }

        Set<StackedObject> savedObjects = new HashSet<>(dataHandler.OBJECTS_TO_SAVE);
        dataHandler.OBJECTS_TO_SAVE.clear();

        SQLDatabaseTransaction<?> spawnersInsertTransaction = null;
        SQLDatabaseTransaction<?> barrelsInsertTransaction = null;

        for (StackedObject<?> stackedObject : savedObjects) {
            if (stackedObject instanceof StackedEntity) {
                dataSerializer.saveEntity((StackedEntity) stackedObject);
            } else if (stackedObject instanceof StackedItem) {
                dataSerializer.saveItem((StackedItem) stackedObject);
            } else if (stackedObject instanceof StackedSpawner) {
                spawnersInsertTransaction = plugin.getDataHandler()
                        .insertSpawner(((WStackedSpawner) stackedObject), spawnersInsertTransaction);
            } else if (stackedObject instanceof StackedBarrel) {
                barrelsInsertTransaction = plugin.getDataHandler()
                        .insertBarrel(((StackedBarrel) stackedObject), barrelsInsertTransaction);
            }
        }

        List<IDatabaseTransaction> transactionsToExecute = new LinkedList<>();
        if (spawnersInsertTransaction != null)
            transactionsToExecute.add(spawnersInsertTransaction);
        if (barrelsInsertTransaction != null)
            transactionsToExecute.add(barrelsInsertTransaction);
        if (!transactionsToExecute.isEmpty())
            DBSession.execute(transactionsToExecute);
    }

    @Override
    public void updateLinkedEntity(LivingEntity livingEntity, LivingEntity newLivingEntity) {
        for (StackedSpawner stackedSpawner : getStackedSpawners()) {
            LivingEntity linkedEntity = ((WStackedSpawner) stackedSpawner).getRawLinkedEntity();
            if (linkedEntity != null && linkedEntity.equals(livingEntity))
                stackedSpawner.setLinkedEntity(newLivingEntity);
        }
    }

    @Override
    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type) {
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

    @Override
    public StackedItem spawnItemWithAmount(Location location, ItemStack itemStack) {
        return spawnItemWithAmount(location, itemStack, itemStack.getAmount());
    }

    @Override
    public StackedItem spawnItemWithAmount(Location location, ItemStack itemStack, int amount) {
        int limit = ItemUtils.canBeStacked(itemStack, location.getWorld()) ?
                plugin.getSettings().itemsLimits.getOrDefault(itemStack.getType(), Integer.MAX_VALUE) :
                itemStack.getMaxStackSize();

        limit = limit < 1 ? Integer.MAX_VALUE : limit;

        int amountOfItems = amount / limit;

        int itemLimit = limit;

        StackedItem lastDroppedItem = null;

        for (int i = 0; i < amountOfItems; i++) {
            itemStack = itemStack.clone();
            itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), itemLimit));
            lastDroppedItem = plugin.getNMSEntities().createItem(location, itemStack, SpawnCause.CUSTOM, stackedItem -> {
                if (plugin.getSettings().itemsStackingEnabled)
                    stackedItem.setStackAmount(itemLimit, stackedItem.isCached());
            });
        }

        int leftOvers = amount % limit;

        if (leftOvers > 0) {
            itemStack = itemStack.clone();
            itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), leftOvers));
            lastDroppedItem = plugin.getNMSEntities().createItem(location, itemStack, SpawnCause.CUSTOM, stackedItem -> {
                if (plugin.getSettings().itemsStackingEnabled)
                    stackedItem.setStackAmount(leftOvers, stackedItem.isCached());
            });
        }

        return lastDroppedItem;
    }

    @Override
    public void spawnCorpse(StackedEntity stackedEntity) {
        Class<? extends Entity> entityClass = stackedEntity.getType().getEntityClass();

        if (entityClass == null)
            return;

        LivingEntity livingEntity = (LivingEntity) spawnEntityWithoutStacking(stackedEntity.getLocation(), entityClass, SpawnCause.CUSTOM, entity -> {
            // Marking the entity as a corpse before the actual spawning
            EntityStorage.setMetadata(entity, EntityFlag.CORPSE, true);
            return true;
        }, entity -> {
            // Updating the entity values after the actual spawning
            plugin.getNMSAdapter().updateEntity(stackedEntity.getLivingEntity(), (LivingEntity) entity);
        });

        if (livingEntity != null) {
            Executor.sync(() -> {
                plugin.getNMSEntities().playDeathSound(livingEntity);
                livingEntity.setHealth(0);
                Executor.sync(() -> EntityStorage.clearMetadata(livingEntity), 1L);
            }, 2L);
        }
    }

    @Override
    public void performKillAll() {
        performKillAll(false);
    }

    /*
     * General methods
     */

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
        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(() -> performKillAll(entityPredicate, itemPredicate, applyTaskFilter));
            return;
        }

        List<Entity> entityList = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            if (!applyTaskFilter || plugin.getSettings().killTaskEntitiesWorlds.isEmpty() ||
                    plugin.getSettings().killTaskEntitiesWorlds.contains(world.getName())) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    entityList.addAll(Arrays.stream(chunk.getEntities())
                            .filter(entity -> entity instanceof LivingEntity).collect(Collectors.toList()));
                }
            }
            if (!applyTaskFilter || plugin.getSettings().killTaskItemsWorlds.isEmpty() ||
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
                        if (!applyTaskFilter || (((plugin.getSettings().killTaskStackedEntities && stackedEntity.getStackAmount() > 1) ||
                                (plugin.getSettings().killTaskUnstackedEntities && stackedEntity.getStackAmount() <= 1)) && !stackedEntity.hasNameTag()))
                            stackedEntity.remove();
                    });

            if (plugin.getSettings().killTaskStackedItems) {
                entityList.stream()
                        .filter(entity -> ItemUtils.isStackable(entity) && ItemUtils.canPickup((Item) entity) && itemPredicate.test((Item) entity) &&
                                (!applyTaskFilter || (GeneralUtils.containsOrEmpty(plugin.getSettings().killTaskItemsWhitelist, ((Item) entity).getItemStack().getType()) &&
                                        !plugin.getSettings().killTaskItemsBlacklist.contains(((Item) entity).getItemStack().getType()))))
                        .forEach(entity -> {
                            StackedItem stackedItem = WStackedItem.of(entity);
                            int maxStackSize = ((Item) entity).getItemStack().getMaxStackSize();
                            if (!applyTaskFilter || stackedItem.getStackAmount() > maxStackSize ||
                                    (plugin.getSettings().killTaskUnstackedItems && stackedItem.getStackAmount() <= maxStackSize))
                                stackedItem.remove();
                        });
            }

            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (pl.isOp())
                    Locale.KILL_ALL_OPS.send(pl);
            }
        });
    }

    @Override
    public LootTable getLootTable(LivingEntity livingEntity) {
        return plugin.getLootHandler().getLootTable(livingEntity);
    }

    @Override
    public LootEntityAttributes.Builder createLootContextBuilder(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return new EntityLootDataBuilder(entityType);
    }

    @Override
    public LootEntityAttributes.Builder createLootContextBuilder(StackedEntity stackedEntity) {
        Preconditions.checkNotNull(stackedEntity, "stackedEntity parameter cannot be null.");
        return new EntityLootDataBuilder(stackedEntity.getLivingEntity());
    }

    @Override
    public LootEntityAttributes.Builder createLootContextBuilder(LivingEntity livingEntity) {
        Preconditions.checkNotNull(livingEntity, "livingEntity parameter cannot be null.");
        return new EntityLootDataBuilder(livingEntity);
    }

    @Override
    public LootEntityAttributes.Builder createLootContextBuilder(Entity entity) {
        Preconditions.checkNotNull(entity, "entity parameter cannot be null.");
        return new EntityLootDataBuilder(entity);
    }

    @Override
    public StackedSnapshot getStackedSnapshot(Chunk chunk, boolean loadData) {
        return getStackedSnapshot(chunk);
    }

    @Override
    public StackedSnapshot getStackedSnapshot(Chunk chunk) {
        return getStackedSnapshot(chunk, StackedSnapshot.SnapshotOptions.LOAD_BARRELS,
                StackedSnapshot.SnapshotOptions.LOAD_SPAWNERS);
    }

    @Override
    public StackedSnapshot getStackedSnapshot(Chunk chunk, StackedSnapshot.SnapshotOptions... snapshotOptions) {
        chunk.load(false);
        return new WStackedSnapshot(chunk, snapshotOptions);
    }

    @Override
    public boolean hasItemNamesToggledOff(Player player) {
        return itemsDisabledNames.contains(player.getUniqueId());
    }

    @Override
    public void toggleItemNames(Player player) {
        // If the set contained the uuid (aka, the player had items toggled off), he will be removed and nothing else.
        // If the set didn't contain the uuid, then the player will be added to it.
        if (!itemsDisabledNames.remove(player.getUniqueId()))
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
        if (!entitiesDisabledNames.remove(player.getUniqueId()))
            entitiesDisabledNames.add(player.getUniqueId());
    }

    @Override
    public void addSpawnCondition(SpawnCondition spawnCondition, EntityType... entityTypes) {
        for (EntityType entityType : entityTypes)
            spawnConditions.computeIfAbsent(entityType, new HashSet<>(1)).add(spawnCondition);
    }

    @Override
    public Collection<SpawnCondition> getSpawnConditions(EntityType entityType) {
        return Collections.unmodifiableSet(spawnConditions.getOrDefault(entityType, new HashSet<>()));
    }

    @Override
    public void removeSpawnCondition(EntityType entityType, SpawnCondition spawnCondition) {
        Set<SpawnCondition> spawnConditionSet = spawnConditions.get(entityType);
        if (spawnConditionSet != null)
            spawnConditionSet.remove(spawnCondition);
    }

    /*
     * Loot loot methods
     */

    @Override
    public void clearSpawnConditions(EntityType entityType) {
        spawnConditions.remove(entityType);
    }

    /*
     * Names toggle methods
     */

    @Override
    public Optional<SpawnCondition> getSpawnCondition(String id) {
        return Optional.ofNullable(spawnConditionsIds.get(id.toLowerCase()));
    }

    @Override
    public SpawnCondition registerSpawnCondition(SpawnCondition spawnCondition) {
        spawnConditionsIds.put(spawnCondition.getId().toLowerCase(), spawnCondition);
        return spawnCondition;
    }

    public boolean isBarrelBlock(Material blockType, World world) {
        return (plugin.getSettings().whitelistedBarrels.size() == 0 ||
                plugin.getSettings().whitelistedBarrels.contains(blockType)) &&
                !plugin.getSettings().blacklistedBarrels.contains(blockType) &&
                !plugin.getSettings().barrelsDisabledWorlds.contains(world.getName());
    }

    public void markToBeSaved(StackedObject stackedObject) {
        dataHandler.OBJECTS_TO_SAVE.add(stackedObject);
    }

    /*
     * Spawn condition methods
     */

    public void markToBeUnsaved(StackedObject stackedObject) {
        dataHandler.OBJECTS_TO_SAVE.remove(stackedObject);
    }

    public void loadSpawners(Chunk chunk) {
        World world = chunk.getWorld();
        dataHandler.stackedSpawnerStore.loadUnloaded(world.getName(), chunk.getX(), chunk.getZ(),
                unloadedStackedSpawner -> {
                    Location spawnerLocation = new Location(world, unloadedStackedSpawner.getX(),
                            unloadedStackedSpawner.getY(), unloadedStackedSpawner.getZ());
                    Block block = spawnerLocation.getBlock();

                    if (block.getType() == Materials.SPAWNER.toBukkitType()) {
                        WStackedSpawner stackedSpawner = new WStackedSpawner((CreatureSpawner) block.getState());
                        try {
                            stackedSpawner.setSaveData(false);
                            stackedSpawner.setUpgradeId(((WUnloadedStackedSpawner) unloadedStackedSpawner).getUpgradeId(), null, false);
                            stackedSpawner.setStackAmount(unloadedStackedSpawner.getStackAmount(), true);
                            dataHandler.addStackedSpawner(stackedSpawner);
                        } finally {
                            stackedSpawner.setSaveData(true);
                        }
                    }
                });

        if (plugin.getSettings().spawnersOverrideEnabled) {
            plugin.getNMSSpawners().updateStackedSpawners(chunk);
        }
    }

    public void loadBarrels(Chunk chunk) {
        World world = chunk.getWorld();
        dataHandler.stackedBarrelStore.loadUnloaded(world.getName(), chunk.getX(), chunk.getZ(), unloadedStackedBarrel -> {
            Location barrelLocation = new Location(world, unloadedStackedBarrel.getX(),
                    unloadedStackedBarrel.getY(), unloadedStackedBarrel.getZ());
            Block block = barrelLocation.getBlock();

            if (block.getType() == Material.CAULDRON) {
                WStackedBarrel stackedBarrel = new WStackedBarrel(block, unloadedStackedBarrel.getBarrelItem(1));
                try {
                    stackedBarrel.setSaveData(false);
                    stackedBarrel.setStackAmount(unloadedStackedBarrel.getStackAmount(), true);
                    stackedBarrel.createDisplayBlock();
                    dataHandler.addStackedBarrel(stackedBarrel);
                } finally {
                    stackedBarrel.setSaveData(true);
                }
            }
        });
    }

    public void handleChunkLoad(Chunk chunk, int unloadStage) {
        this.handleChunkLoad(chunk, unloadStage, Arrays.asList(chunk.getEntities()));
    }

    public void handleChunkLoad(Chunk chunk, int loadStage, List<Entity> loadedEntities) {
        if (!this.loadedData)
            return;

        boolean isChunkLoad = (loadStage & CHUNK_STAGE) != 0;
        boolean isEntitiesLoad = (loadStage & ENTITIES_STAGE) != 0;

        boolean atLeast18 = ServerVersion.isAtLeast(ServerVersion.v1_8);

        if (isChunkLoad) {
            loadSpawners(chunk);
            if (atLeast18)
                loadBarrels(chunk);
        }

        if (isEntitiesLoad) {
            for (Entity entity : loadedEntities) {
                String customName = plugin.getNMSEntities().getCustomName(entity);

                // Checking for too long names
                if (customName != null && customName.length() > 256)
                    plugin.getNMSEntities().setCustomName(entity, customName.substring(0, 256));

                // Remove display blocks of invalid barrels
                if (atLeast18 && entity instanceof ArmorStand && customName != null &&
                        customName.equals("BlockDisplay") && !isStackedBarrel(entity.getLocation().getBlock())) {
                    Block block = entity.getLocation().getBlock();
                    if (block.getType() == Material.CAULDRON)
                        block.setType(Material.AIR);
                    entity.remove();
                }

                if (EntityUtils.isStackable(entity)) {
                    StackedEntity stackedEntity = WStackedEntity.of(entity);
                    stackedEntity.updateNerfed();
                    stackedEntity.updateName();
                }
            }
        }
    }

    public void handleChunkUnload(Chunk chunk, int unloadStage) {
        this.handleChunkUnload(chunk, unloadStage, Arrays.asList(chunk.getEntities()));
    }

    public void saveEntity(StackedEntity stackedEntity) {
        dataSerializer.saveEntity(stackedEntity);
    }

    public void saveItem(StackedItem stackedItem) {
        dataSerializer.saveItem(stackedItem);
    }

    public void handleChunkUnload(Chunk chunk, int unloadStage, List<Entity> unloadedEntities) {
        if (!this.loadedData)
            return;

        boolean isChunkLoad = (unloadStage & CHUNK_STAGE) != 0;
        boolean isEntitiesLoad = (unloadStage & ENTITIES_STAGE) != 0;

        if (isEntitiesLoad) {
            for (Entity entity : unloadedEntities) {
                if (EntityUtils.isStackable(entity)) {
                    StackedEntity stackedEntity = dataHandler.stackedEntityStore.remove(entity.getEntityId());
                    if (stackedEntity != null) {
                        dataSerializer.saveEntity(stackedEntity);
                        stackedEntity.clearFlags();
                    }
                } else if (entity instanceof Item) {
                    StackedItem stackedItem = dataHandler.stackedItemStore.remove(entity.getEntityId());
                    if (stackedItem != null)
                        dataSerializer.saveItem(stackedItem);
                }
            }
        }

        if (isChunkLoad) {
            for (StackedSpawner stackedSpawner : getStackedSpawners(chunk)) {
                dataHandler.removeStackedSpawner(stackedSpawner);
                if (stackedSpawner.getStackAmount() > 1 || !stackedSpawner.isDefaultUpgrade()) {
                    WUnloadedStackedSpawner unloadedStackedSpawner = new WUnloadedStackedSpawner(stackedSpawner);
                    dataHandler.stackedSpawnerStore.storeUnloaded(unloadedStackedSpawner);
                }
            }

            for (StackedBarrel stackedBarrel : getStackedBarrels(chunk)) {
                dataHandler.removeStackedBarrel(stackedBarrel);
                WUnloadedStackedBarrel unloadedStackedBarrel = new WUnloadedStackedBarrel(stackedBarrel);
                dataHandler.stackedBarrelStore.storeUnloaded(unloadedStackedBarrel);
                stackedBarrel.removeDisplayBlock();
            }
        }
    }

    public void setDataLoaded() {
        this.loadedData = true;
    }

    @Nullable
    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, SpawnCause spawnCause,
                                                           Predicate<Entity> beforeSpawnConsumer, Consumer<Entity> afterSpawnConsumer) {
        return plugin.getNMSEntities().createEntity(location, type, spawnCause, entity -> {
            EntityStorage.setMetadata(entity, EntityFlag.BYPASS_STACKING, true);
            return beforeSpawnConsumer == null || beforeSpawnConsumer.test(entity);
        }, afterSpawnConsumer);
    }

    /*
     * Data serialization methods
     */

    public void setDataSerializer(IDataSerializer dataSerializer) {
        this.dataSerializer = dataSerializer;
    }

    private static boolean hasImportantFlags(StackedEntity stackedEntity) {
        return stackedEntity.hasFlag(EntityFlag.DEAD_ENTITY) || stackedEntity.hasFlag(EntityFlag.ORIGINAL_AMOUNT);
    }

}
