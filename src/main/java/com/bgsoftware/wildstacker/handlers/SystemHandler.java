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
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.database.SQLHelper;
import com.bgsoftware.wildstacker.database.StatementHolder;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.objects.WStackedSnapshot;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.tasks.KillTask;
import com.bgsoftware.wildstacker.tasks.StackTask;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.reflection.Fields;
import com.bgsoftware.wildstacker.utils.reflection.Methods;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class SystemHandler implements SystemManager {

    private WildStackerPlugin plugin;
    private DataHandler dataHandler;

    public SystemHandler(WildStackerPlugin plugin){
        this.plugin = plugin;
        this.dataHandler = plugin.getDataHandler();

        //Start all required tasks
        Executor.sync(() -> {
            KillTask.start();
            StackTask.start();
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
            dataHandler.CACHED_OBJECTS.remove(((StackedEntity) stackedObject).getUniqueId());
        else if(stackedObject instanceof StackedItem)
            dataHandler.CACHED_OBJECTS.remove(((StackedItem) stackedObject).getUniqueId());
        else if(stackedObject instanceof StackedSpawner)
            dataHandler.CACHED_OBJECTS.remove(stackedObject.getLocation());
        else if(stackedObject instanceof StackedBarrel)
            dataHandler.CACHED_OBJECTS.remove(stackedObject.getLocation());
    }

    @Override
    public StackedEntity getStackedEntity(LivingEntity livingEntity) {
        StackedEntity stackedEntity = (StackedEntity) dataHandler.CACHED_OBJECTS.get(livingEntity.getUniqueId());

        if(stackedEntity != null && stackedEntity.getLivingEntity() != null)
            return stackedEntity;

        //Entity wasn't found, creating a new object
        if(EntityStorage.hasMetadata(livingEntity, "spawn-cause"))
            stackedEntity = new WStackedEntity(livingEntity, 1, EntityStorage.getMetadata(livingEntity, "spawn-cause", SpawnCause.class));
        else
            stackedEntity = new WStackedEntity(livingEntity);

        //Checks if the entity still exists after a few ticks
        Executor.sync(() -> {
            if(livingEntity.isDead())
                dataHandler.CACHED_OBJECTS.remove(livingEntity.getUniqueId());
        }, 10L);

        boolean shouldBeCached = ((WStackedEntity) stackedEntity).isCached();

        //A new entity was created. Let's see if we need to add him
        if(!(livingEntity instanceof Player) && !(livingEntity instanceof ArmorStand) && shouldBeCached)
            dataHandler.CACHED_OBJECTS.put(stackedEntity.getUniqueId(), stackedEntity);

        if(dataHandler.CACHED_AMOUNT_ENTITIES.containsKey(livingEntity.getUniqueId())){
            stackedEntity.setStackAmount(dataHandler.CACHED_AMOUNT_ENTITIES.get(livingEntity.getUniqueId()), true);
            if(shouldBeCached)
                dataHandler.CACHED_AMOUNT_ENTITIES.remove(stackedEntity.getUniqueId());
        }

        if(dataHandler.CACHED_SPAWN_CAUSE_ENTITIES.containsKey(livingEntity.getUniqueId())){
            stackedEntity.setSpawnCause(dataHandler.CACHED_SPAWN_CAUSE_ENTITIES.get(livingEntity.getUniqueId()));
            if(shouldBeCached)
                dataHandler.CACHED_SPAWN_CAUSE_ENTITIES.remove(stackedEntity.getUniqueId());
        }

        return stackedEntity;
    }

    @Override
    public StackedItem getStackedItem(Item item) {
        StackedItem stackedItem = (StackedItem) dataHandler.CACHED_OBJECTS.get(item.getUniqueId());

        if(stackedItem != null && stackedItem.getItem() != null)
            return stackedItem;

        //Item wasn't found, creating a new object.
        stackedItem = new WStackedItem(item);

        //Checks if the item still exists after a few ticks
        Executor.sync(() -> {
            if(item.isDead())
                dataHandler.CACHED_OBJECTS.remove(item.getUniqueId());
        }, 10L);

        //A new item was created. Let's see if we need to add him
        if(plugin.getSettings().itemsStackingEnabled && stackedItem.isWhitelisted() && !stackedItem.isBlacklisted() && !stackedItem.isWorldDisabled())
            dataHandler.CACHED_OBJECTS.put(stackedItem.getUniqueId(), stackedItem);

        if(dataHandler.CACHED_AMOUNT_ITEMS.containsKey(item.getUniqueId())){
            stackedItem.setStackAmount(dataHandler.CACHED_AMOUNT_ITEMS.get(item.getUniqueId()), true);
            dataHandler.CACHED_AMOUNT_ITEMS.remove(item.getUniqueId());
        }

        return stackedItem;
    }

    @Override
    public StackedSpawner getStackedSpawner(CreatureSpawner spawner) {
        return getStackedSpawner(spawner.getLocation());
    }

    @Override
    public StackedSpawner getStackedSpawner(Location location) {
        StackedSpawner stackedSpawner = (StackedSpawner) dataHandler.CACHED_OBJECTS.get(location);

        if(stackedSpawner != null)
            return stackedSpawner;

        //Spawner wasn't found, creating a new object
        stackedSpawner = new WStackedSpawner((CreatureSpawner) location.getBlock().getState());

        //Checks if the spawner still exists after a few ticks
        Executor.sync(() -> {
            if(!isStackedSpawner(location.getBlock()))
                dataHandler.CACHED_OBJECTS.remove(location);
        }, 10L);

        //A new spawner was created. Let's see if we need to add him
        if(plugin.getSettings().spawnersStackingEnabled && stackedSpawner.isWhitelisted() && !stackedSpawner.isBlacklisted() && !stackedSpawner.isWorldDisabled())
            dataHandler.CACHED_OBJECTS.put(stackedSpawner.getLocation(), stackedSpawner);

        return stackedSpawner;
    }

    @Override
    public StackedBarrel getStackedBarrel(Block block) {
        return getStackedBarrel(block == null ? null : block.getLocation());
    }

    @Override
    public StackedBarrel getStackedBarrel(Location location) {
        StackedBarrel stackedBarrel = (StackedBarrel) dataHandler.CACHED_OBJECTS.get(location);

        if(stackedBarrel != null)
            return stackedBarrel;

        //Barrel wasn't found, creating a new object
        stackedBarrel = new WStackedBarrel(location.getBlock(), ItemUtils.getFromBlock(location.getBlock()));

        //Checks if the barrel still exists after a few ticks
        Executor.sync(() -> {
            if(isStackedBarrel(location.getBlock()))
                WStackedBarrel.of(location.getBlock()).createDisplayBlock();
        }, 2L);

        //Checks if the barrel still exists after a few ticks
        Executor.sync(() -> {
            if(!isStackedBarrel(location.getBlock()))
                dataHandler.CACHED_OBJECTS.remove(location);
        }, 10L);

        //A new barrel was created. Let's see if we need to add him
        if(plugin.getSettings().barrelsStackingEnabled && stackedBarrel.isWhitelisted() && !stackedBarrel.isBlacklisted() && !stackedBarrel.isWorldDisabled())
            dataHandler.CACHED_OBJECTS.put(stackedBarrel.getLocation(), stackedBarrel);

        return stackedBarrel;
    }

    @Override
    public List<StackedEntity> getStackedEntities() {
        return dataHandler.CACHED_OBJECTS.values().stream()
                .filter(stackedObject -> stackedObject instanceof StackedEntity)
                .map(stackedObject -> getStackedEntity(((StackedEntity) stackedObject).getLivingEntity()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StackedItem> getStackedItems() {
        return dataHandler.CACHED_OBJECTS.values().stream()
                .filter(stackedObject -> stackedObject instanceof StackedItem)
                .map(stackedObject -> getStackedItem(((StackedItem) stackedObject).getItem()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StackedSpawner> getStackedSpawners(){
        return dataHandler.CACHED_OBJECTS.values().stream()
                .filter(stackedObject -> stackedObject instanceof StackedSpawner)
                .map(stackedObject -> getStackedSpawner(stackedObject.getLocation()))
                .collect(Collectors.toList());
    }

    public List<StackedSpawner> getStackedSpawners(Chunk chunk) {
        return getStackedSpawners().stream()
                .filter(stackedSpawner -> GeneralUtils.isSameChunk(stackedSpawner.getLocation(), chunk))
                .collect(Collectors.toList());
    }

    @Override
    public List<StackedBarrel> getStackedBarrels(){
        return dataHandler.CACHED_OBJECTS.values().stream()
                .filter(stackedObject -> stackedObject instanceof StackedBarrel)
                .map(stackedObject -> getStackedBarrel(stackedObject.getLocation()))
                .collect(Collectors.toList());
    }

    public List<StackedBarrel> getStackedBarrels(Chunk chunk) {
        return getStackedBarrels().stream()
                .filter(stackedBarrel -> GeneralUtils.isSameChunk(stackedBarrel.getLocation(), chunk))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isStackedSpawner(Block block) {
        return block != null && block.getType() == Materials.SPAWNER.toBukkitType() && dataHandler.CACHED_OBJECTS.containsKey(block.getLocation());
    }

    @Override
    public boolean isStackedBarrel(Block block) {
        return block != null && block.getType() == Material.CAULDRON && dataHandler.CACHED_OBJECTS.containsKey(block.getLocation());
    }

    @Override
    public void performCacheClear() {
        List<StackedObject> stackedObjects = new ArrayList<>(dataHandler.CACHED_OBJECTS.values());

        for(StackedObject stackedObject : stackedObjects){
            if(stackedObject instanceof StackedItem){
                StackedItem stackedItem = (StackedItem) stackedObject;
                if(stackedItem.getItem() == null || (GeneralUtils.isChunkLoaded(stackedItem.getItem().getLocation()) && stackedItem.getItem().isDead()))
                    removeStackObject(stackedObject);
            }

            else if(stackedObject instanceof StackedEntity){
                StackedEntity stackedEntity = (StackedEntity) stackedObject;
                if(stackedEntity.getLivingEntity() == null || (GeneralUtils.isChunkLoaded(stackedEntity.getLivingEntity().getLocation()) && stackedEntity.getLivingEntity().isDead()))
                    removeStackObject(stackedObject);
                else
                    stackedEntity.updateNerfed();
            }

            else if(stackedObject instanceof StackedSpawner){
                StackedSpawner stackedSpawner = (StackedSpawner) stackedObject;
                if(GeneralUtils.isChunkLoaded(stackedSpawner.getLocation()) && !isStackedSpawner(stackedSpawner.getSpawner().getBlock())) {
                    removeStackObject(stackedObject);
                    plugin.getProviders().deleteHologram(stackedSpawner);
                }
            }

            else if(stackedObject instanceof StackedBarrel){
                StackedBarrel stackedBarrel = (StackedBarrel) stackedObject;
                if(GeneralUtils.isChunkLoaded(stackedBarrel.getLocation()) && !isStackedBarrel(stackedBarrel.getBlock())) {
                    removeStackObject(stackedObject);
                    stackedBarrel.removeDisplayBlock();
                    plugin.getProviders().deleteHologram(stackedBarrel);
                }
            }
        }

        StackService.clearCache();

        plugin.getProviders().clearHolograms();
    }

    @Override
    public void performCacheSave() {
        Map<UUID, Integer> entityAmounts = new HashMap<>(), itemAmounts = new HashMap<>();
        Map<UUID, SpawnCause> entitySpawnCauses = new HashMap<>();

        SQLHelper.executeUpdate("DELETE FROM entities;");
        SQLHelper.executeUpdate("DELETE FROM items;");

        getStackedEntities().forEach(stackedEntity -> {
            if(stackedEntity.getStackAmount() > 1 || hasValidSpawnCause(stackedEntity.getSpawnCause())){
                entityAmounts.put(stackedEntity.getUniqueId(), stackedEntity.getStackAmount());
                entitySpawnCauses.put(stackedEntity.getUniqueId(), stackedEntity.getSpawnCause());
            }else{
                removeStackObject(stackedEntity);
            }
        });

        new HashMap<>(dataHandler.CACHED_AMOUNT_ENTITIES).keySet().forEach(uuid ->{
            int stackAmount = dataHandler.CACHED_AMOUNT_ENTITIES.get(uuid);
            SpawnCause spawnCause = dataHandler.CACHED_SPAWN_CAUSE_ENTITIES.getOrDefault(uuid, SpawnCause.CHUNK_GEN);
            if(stackAmount > 1 || hasValidSpawnCause(spawnCause)) {
                entityAmounts.put(uuid, Math.max(entityAmounts.getOrDefault(uuid, 1), stackAmount));
                entitySpawnCauses.put(uuid, spawnCause);
            }else{
                dataHandler.CACHED_AMOUNT_ENTITIES.remove(uuid);
                dataHandler.CACHED_SPAWN_CAUSE_ENTITIES.remove(uuid);
            }
        });

        getStackedItems().forEach(stackedItem -> {
          if(stackedItem.getStackAmount() > 1){
              itemAmounts.put(stackedItem.getUniqueId(), stackedItem.getStackAmount());
          }
          else{
              removeStackObject(stackedItem);
          }
        });

        new HashMap<>(dataHandler.CACHED_AMOUNT_ITEMS).keySet().forEach(uuid -> {
            int stackAmount = dataHandler.CACHED_AMOUNT_ITEMS.get(uuid);
            if(stackAmount > 1){
                itemAmounts.put(uuid, stackAmount);
            }
            else{
                dataHandler.CACHED_AMOUNT_ITEMS.remove(uuid);
            }
        });

        if(entityAmounts.size() > 0) {
            StatementHolder entityHolder = Query.ENTITY_INSERT.getStatementHolder();
            entityAmounts.forEach((uuid, stackAmount) -> {
                SpawnCause spawnCause = entitySpawnCauses.getOrDefault(uuid, SpawnCause.CHUNK_GEN);
                entityHolder.setString(uuid.toString()).setInt(stackAmount).setString(spawnCause.name()).addBatch();
            });
            entityHolder.execute(false);
        }

        if(itemAmounts.size() > 0) {
            StatementHolder itemHolder = Query.ITEM_INSERT.getStatementHolder();
            itemAmounts.forEach((uuid, stackAmount) ->
                    itemHolder.setString(uuid.toString()).setInt(stackAmount).addBatch());
            itemHolder.execute(false);
        }
    }

    private boolean hasValidSpawnCause(SpawnCause spawnCause){
        return spawnCause != SpawnCause.CHUNK_GEN && spawnCause != SpawnCause.NATURAL;
    }

    @Override
    public void updateLinkedEntity(LivingEntity livingEntity, LivingEntity newLivingEntity) {
        for(StackedSpawner stackedSpawner : getStackedSpawners()){
            LivingEntity linkedEntity = ((WStackedSpawner) stackedSpawner).getRawLinkedEntity();
            if(linkedEntity != null && linkedEntity.equals(livingEntity))
                stackedSpawner.setLinkedEntity(newLivingEntity);
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
        World world = location.getWorld();

        Object entity = Methods.WORLD_CREATE_ENTITY.invoke(world, location, type);
        Entity bukkitEntity = (Entity) Methods.ENTITY_GET_BUKKIT_ENTITY.invoke(entity);

        //noinspection all
        EntitiesListener.noStackEntities.add(bukkitEntity.getUniqueId());

        Methods.WORLD_ADD_ENTITY.invoke(world, entity, spawnCause.toSpawnReason());

        WStackedEntity.of(bukkitEntity).setSpawnCause(spawnCause);

        return type.cast(bukkitEntity);
    }

    @Override
    public StackedItem spawnItemWithAmount(Location location, ItemStack itemStack) {
        return spawnItemWithAmount(location, itemStack, itemStack.getAmount());
    }

    @Override
    public StackedItem spawnItemWithAmount(Location location, ItemStack itemStack, int amount) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        location = location.clone();
        itemStack = itemStack.clone();

        location.setX(location.getX() + (random.nextFloat() * 0.5F) + 0.25D);
        location.setY(location.getY() + (random.nextFloat() * 0.5F) + 0.25D);
        location.setZ(location.getZ() + (random.nextFloat() * 0.5F) + 0.25D);

        itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), amount));

        Object[] entityObjects = plugin.getNMSAdapter().createItemEntity(location, itemStack);

        Fields.ITEM_PICKUP_DELAY.set(entityObjects[0], 10);

        StackedItem stackedItem = WStackedItem.of((Item) entityObjects[1]);
        stackedItem.setStackAmount(amount, true);

        Methods.WORLD_ADD_ENTITY.invoke(location.getWorld(), entityObjects[0], CreatureSpawnEvent.SpawnReason.CUSTOM);

        return stackedItem;
    }

    @Override
    public void spawnCorpse(StackedEntity stackedEntity) {
        LivingEntity livingEntity = (LivingEntity) spawnEntityWithoutStacking(stackedEntity.getLivingEntity().getLocation(),
                stackedEntity.getType().getEntityClass(), SpawnCause.CUSTOM);
        if(livingEntity != null) {
            EntityStorage.setMetadata(livingEntity, "corpse", null);
            if(stackedEntity.getLivingEntity() instanceof Slime){
                ((Slime) livingEntity).setSize(((Slime) stackedEntity.getLivingEntity()).getSize());
            }
            plugin.getNMSAdapter().playDeathSound(livingEntity);
            livingEntity.setHealth(0);
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
            Executor.sync(() -> performKillAll(entityPredicate, itemPredicate));
            return;
        }

        List<Entity> entityList = new ArrayList<>();

        for(World world : Bukkit.getWorlds()){
            for(Chunk chunk : world.getLoadedChunks()){
                entityList.addAll(Arrays.asList(chunk.getEntities()));
            }
        }

        Executor.async(() -> {
            entityList.stream()
                    .filter(entity -> EntityUtils.isStackable(entity) && entityPredicate.test(entity) &&
                            (!applyTaskFilter || GeneralUtils.containsOrEmpty(plugin.getSettings().killTaskWhitelist, WStackedEntity.of(entity))))
                    .forEach(entity -> {
                        StackedEntity stackedEntity = WStackedEntity.of(entity);
                        if((plugin.getSettings().killTaskStackedEntities && stackedEntity.getStackAmount() > 1) ||
                                (plugin.getSettings().killTaskUnstackedEntities && stackedEntity.getStackAmount() <= 1))
                            stackedEntity.remove();
                    });

            if(plugin.getSettings().killTaskStackedItems) {
                entityList.stream()
                        .filter(entity -> entity instanceof Item && itemPredicate.test((Item) entity))
                        .forEach(entity -> {
                            StackedItem stackedItem = WStackedItem.of(entity);
                            if((plugin.getSettings().killTaskStackedItems && stackedItem.getStackAmount() > 1) ||
                                    (plugin.getSettings().killTaskUnstackedItems && stackedItem.getStackAmount() <= 1))
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
        return new WStackedSnapshot(chunk);
    }

    /*
     * Loot loot methods
     */

    @Override
    public LootTable getLootTable(LivingEntity livingEntity) {
        return plugin.getLootHandler().getLootTable(livingEntity);
    }

}
