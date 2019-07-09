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
import com.bgsoftware.wildstacker.database.SQLHelper;
import com.bgsoftware.wildstacker.listeners.EntitiesListener;
import com.bgsoftware.wildstacker.objects.WStackedBarrel;
import com.bgsoftware.wildstacker.objects.WStackedEntity;
import com.bgsoftware.wildstacker.objects.WStackedItem;
import com.bgsoftware.wildstacker.objects.WStackedSnapshot;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.tasks.KillTask;
import com.bgsoftware.wildstacker.tasks.StackTask;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.ItemUtil;
import com.bgsoftware.wildstacker.utils.ReflectionUtil;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
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
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::performCacheClear, 300L, 300L);
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
            dataHandler.CACHED_OBJECTS.remove(((StackedSpawner) stackedObject).getLocation());
        else if(stackedObject instanceof StackedBarrel)
            dataHandler.CACHED_OBJECTS.remove(((StackedBarrel) stackedObject).getLocation());
    }

    @Override
    public StackedEntity getStackedEntity(LivingEntity livingEntity) {
        StackedEntity stackedEntity = (StackedEntity) dataHandler.CACHED_OBJECTS.get(livingEntity.getUniqueId());

        if(stackedEntity != null && stackedEntity.getLivingEntity() != null)
            return stackedEntity;

        //Entity wasn't found, creating a new object
        stackedEntity = new WStackedEntity(livingEntity);

        //Checks if the entity still exists after a few ticks
        Executor.sync(() -> {
            if(livingEntity.isDead())
                dataHandler.CACHED_OBJECTS.remove(livingEntity.getUniqueId());
        }, 10L);

        //A new entity was created. Let's see if we need to add him
        if(!(livingEntity instanceof Player) && !(livingEntity instanceof ArmorStand) &&
                plugin.getSettings().entitiesStackingEnabled && stackedEntity.isWhitelisted() && !stackedEntity.isBlacklisted() && !stackedEntity.isWorldDisabled())
            dataHandler.CACHED_OBJECTS.put(stackedEntity.getUniqueId(), stackedEntity);

        if(dataHandler.CACHED_AMOUNT_ENTITIES.containsKey(livingEntity.getUniqueId())){
            stackedEntity.setStackAmount(dataHandler.CACHED_AMOUNT_ENTITIES.get(livingEntity.getUniqueId()), true);
            dataHandler.CACHED_AMOUNT_ENTITIES.remove(stackedEntity.getUniqueId());
        }

        if(dataHandler.CACHED_SPAWN_CAUSE_ENTITIES.containsKey(livingEntity.getUniqueId())){
            stackedEntity.setSpawnCause(dataHandler.CACHED_SPAWN_CAUSE_ENTITIES.get(livingEntity.getUniqueId()));
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
        stackedBarrel = new WStackedBarrel(location.getBlock(), ItemUtil.getFromBlock(location.getBlock()));

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
                .map(stackedObject -> (StackedEntity) stackedObject)
                .collect(Collectors.toList());
    }

    @Override
    public List<StackedItem> getStackedItems() {
        return dataHandler.CACHED_OBJECTS.values().stream()
                .filter(stackedObject -> stackedObject instanceof StackedItem)
                .map(stackedObject -> (StackedItem) stackedObject)
                .collect(Collectors.toList());
    }

    @Override
    public List<StackedSpawner> getStackedSpawners(){
        return dataHandler.CACHED_OBJECTS.values().stream()
                .filter(stackedObject -> stackedObject instanceof StackedSpawner)
                .map(stackedObject -> (StackedSpawner) stackedObject)
                .collect(Collectors.toList());
    }

    public List<StackedSpawner> getStackedSpawners(Chunk chunk) {
        return getStackedSpawners().stream()
                .filter(stackedSpawner -> stackedSpawner.getLocation().getChunk().equals(chunk))
                .collect(Collectors.toList());
    }

    @Override
    public List<StackedBarrel> getStackedBarrels(){
        return dataHandler.CACHED_OBJECTS.values().stream()
                .filter(stackedObject -> stackedObject instanceof StackedBarrel)
                .map(stackedObject -> (StackedBarrel) stackedObject)
                .collect(Collectors.toList());
    }

    public List<StackedBarrel> getStackedBarrels(Chunk chunk) {
        return getStackedBarrels().stream()
                .filter(stackedBarrel -> stackedBarrel.getLocation().getChunk().equals(chunk))
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
                if(stackedItem.getItem() == null || (isChunkLoaded(stackedItem.getItem().getLocation()) && stackedItem.getItem().isDead()))
                    removeStackObject(stackedObject);
            }

            else if(stackedObject instanceof StackedEntity){
                StackedEntity stackedEntity = (StackedEntity) stackedObject;
                if(stackedEntity.getLivingEntity() == null || (isChunkLoaded(stackedEntity.getLivingEntity().getLocation()) && stackedEntity.getLivingEntity().isDead()))
                    removeStackObject(stackedObject);
            }

            else if(stackedObject instanceof StackedSpawner){
                StackedSpawner stackedSpawner = (StackedSpawner) stackedObject;
                if(isChunkLoaded(stackedSpawner.getLocation()) && !isStackedSpawner(stackedSpawner.getSpawner().getBlock()))
                    removeStackObject(stackedObject);
            }

            else if(stackedObject instanceof StackedBarrel){
                StackedBarrel stackedBarrel = (StackedBarrel) stackedObject;
                if(isChunkLoaded(stackedBarrel.getLocation()) && !isStackedBarrel(stackedBarrel.getBlock()))
                    removeStackObject(stackedObject);
            }
        }
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

        StringBuilder entityStatement = new StringBuilder("INSERT INTO entities VALUES ");
        entityAmounts.forEach((uuid, stackAmount) -> {
            SpawnCause spawnCause = entitySpawnCauses.getOrDefault(uuid, SpawnCause.CHUNK_GEN);
            entityStatement.append("('").append(uuid.toString()).append("', ").append(stackAmount).append(", '").append(spawnCause.name()).append("'),");
        });
        entityStatement.setCharAt(entityStatement.length() - 1, ';');
        SQLHelper.executeUpdate(entityStatement.toString());

        StringBuilder itemStatement = new StringBuilder("INSERT INTO items VALUES ");
        itemAmounts.forEach((uuid, stackAmount) ->
                itemStatement.append("('").append(uuid.toString()).append("', ").append(stackAmount).append("),"));
        itemStatement.setCharAt(itemStatement.length() - 1, ';');
        SQLHelper.executeUpdate(itemStatement.toString());
    }

    private boolean hasValidSpawnCause(SpawnCause spawnCause){
        return spawnCause != SpawnCause.CHUNK_GEN && spawnCause != SpawnCause.NATURAL;
    }

    private boolean isChunkLoaded(Location location){
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
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
    @SuppressWarnings("unchecked")
    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type, SpawnCause spawnCause) {
        try{
            World world = location.getWorld();

            Class craftWorldClass = ReflectionUtil.getBukkitClass("CraftWorld");
            Object craftWorld = craftWorldClass.cast(world);

            Class entityClass = ReflectionUtil.getNMSClass("Entity");

            Object entity = craftWorldClass.getMethod("createEntity", Location.class, Class.class).invoke(craftWorld, location, type);

            Entity bukkitEntity = (Entity) entity.getClass().getMethod("getBukkitEntity").invoke(entity);

            EntitiesListener.noStackEntities.add(bukkitEntity.getUniqueId());

            craftWorldClass.getMethod("addEntity", entityClass, CreatureSpawnEvent.SpawnReason.class).invoke(craftWorld, entity, spawnCause.toSpawnReason());

            WStackedEntity.of(bukkitEntity).setSpawnCause(spawnCause);

            return type.cast(bukkitEntity);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void spawnCorpse(StackedEntity stackedEntity) {
        LivingEntity livingEntity = (LivingEntity) spawnEntityWithoutStacking(stackedEntity.getLivingEntity().getLocation(),
                stackedEntity.getType().getEntityClass(), SpawnCause.CUSTOM);
        if(livingEntity != null) {
            livingEntity.setMetadata("corpse", new FixedMetadataValue(plugin, ""));
            if(stackedEntity.getLivingEntity() instanceof Slime){
                ((Slime) livingEntity).setSize(((Slime) stackedEntity.getLivingEntity()).getSize());
            }
            livingEntity.setHealth(0);
        }
    }

    @Override
    public void performKillAll(){
        Executor.async(() -> {
            for(StackedEntity stackedEntity : getStackedEntities()) {
                if (stackedEntity.getStackAmount() > 1)
                    stackedEntity.remove();
            }

            if(plugin.getSettings().itemsKillAll) {
                for (StackedItem stackedItem : getStackedItems()) {
                    if (stackedItem.getStackAmount() > 1) {
                        stackedItem.remove();
                    }
                }
            }

            for(Player pl : Bukkit.getOnlinePlayers()) {
                if (pl.isOp())
                    Locale.KILL_ALL_OPS.send(pl);
            }
        });
    }

    @Override
    public StackedSnapshot getStackedSnapshot(Chunk chunk, boolean loadData) {
        return new WStackedSnapshot(chunk, loadData);
    }

    /*
     * Loot loot methods
     */

    @Override
    public LootTable getLootTable(LivingEntity livingEntity) {
        return plugin.getLootHandler().getLootTable(livingEntity);
    }
}
