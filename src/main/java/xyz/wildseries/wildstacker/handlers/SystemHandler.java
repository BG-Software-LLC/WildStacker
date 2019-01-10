package xyz.wildseries.wildstacker.handlers;

import org.bukkit.Bukkit;
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
import xyz.wildseries.wildstacker.Locale;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.api.handlers.SystemManager;
import xyz.wildseries.wildstacker.api.loot.LootTable;
import xyz.wildseries.wildstacker.api.objects.StackedBarrel;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.api.objects.StackedItem;
import xyz.wildseries.wildstacker.api.objects.StackedObject;
import xyz.wildseries.wildstacker.api.objects.StackedSpawner;
import xyz.wildseries.wildstacker.listeners.EntitiesListener;
import xyz.wildseries.wildstacker.objects.WStackedBarrel;
import xyz.wildseries.wildstacker.objects.WStackedEntity;
import xyz.wildseries.wildstacker.objects.WStackedItem;
import xyz.wildseries.wildstacker.objects.WStackedSpawner;
import xyz.wildseries.wildstacker.tasks.KillTask;
import xyz.wildseries.wildstacker.tasks.SaveTask;
import xyz.wildseries.wildstacker.tasks.StackTask;
import xyz.wildseries.wildstacker.utils.EntityUtil;
import xyz.wildseries.wildstacker.utils.ReflectionUtil;
import xyz.wildseries.wildstacker.utils.legacy.Materials;

import java.util.ArrayList;
import java.util.List;

public final class SystemHandler implements SystemManager {

    private WildStackerPlugin instance;
    private DataHandler dataHandler;

    public SystemHandler(WildStackerPlugin instance){
        this.instance = instance;
        this.dataHandler = instance.getDataHandler();

        //Start all required tasks
        Bukkit.getScheduler().runTaskLater(instance, () -> {
            SaveTask.start();
            KillTask.start();
            StackTask.start();
        }, 1L);

        //Start the auto-clear
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, this::performCacheClear, 300L, 300L);
    }

    /*
     * StackedObject's methods
     */

    @Override
    public void removeStackObject(StackedObject stackedObject) {
        Object key = null;

        if(stackedObject instanceof StackedEntity)
            key = ((StackedEntity) stackedObject).getUniqueId();
        else if(stackedObject instanceof StackedItem)
            key = ((StackedItem) stackedObject).getUniqueId();
        else if(stackedObject instanceof StackedSpawner)
            key = ((StackedSpawner) stackedObject).getLocation();
        else if(stackedObject instanceof StackedBarrel)
            key = ((StackedBarrel) stackedObject).getLocation();

        if(key != null)
            dataHandler.CACHED_OBJECTS.remove(key);
    }

    @Override
    public StackedEntity getStackedEntity(LivingEntity livingEntity) {
        if(dataHandler.CACHED_OBJECTS.containsKey(livingEntity.getUniqueId())) {
            if(!(livingEntity instanceof Player) && !(livingEntity instanceof ArmorStand))
                return (StackedEntity) dataHandler.CACHED_OBJECTS.get(livingEntity.getUniqueId());
        }

        StackedEntity stackedEntity = new WStackedEntity(livingEntity);

        if(dataHandler.CACHED_AMOUNT_ENTITIES.containsKey(livingEntity.getUniqueId())) {
            stackedEntity.setStackAmount(dataHandler.CACHED_AMOUNT_ENTITIES.get(livingEntity.getUniqueId()), false);
            dataHandler.CACHED_AMOUNT_ENTITIES.remove(livingEntity.getUniqueId());
        }

        if(dataHandler.CACHED_NERFED_ENTITIES.contains(livingEntity.getUniqueId())){
            EntityUtil.nerfEntity(livingEntity);
            dataHandler.CACHED_NERFED_ENTITIES.remove(livingEntity.getUniqueId());
        }

        if(!(livingEntity instanceof Player) && !(livingEntity instanceof ArmorStand))
            dataHandler.CACHED_OBJECTS.put(livingEntity.getUniqueId(), stackedEntity);

        return stackedEntity;
    }

    @Override
    public StackedItem getStackedItem(Item item) {
        if(dataHandler.CACHED_OBJECTS.containsKey(item.getUniqueId()))
            return (StackedItem) dataHandler.CACHED_OBJECTS.get(item.getUniqueId());

        StackedItem stackedItem = new WStackedItem(item);

        if(dataHandler.CACHED_AMOUNT_ITEMS.containsKey(item.getUniqueId())) {
            stackedItem.setStackAmount(dataHandler.CACHED_AMOUNT_ITEMS.get(item.getUniqueId()), false);
            dataHandler.CACHED_AMOUNT_ITEMS.remove(item.getUniqueId());
        }

        dataHandler.CACHED_OBJECTS.put(item.getUniqueId(), stackedItem);

        return stackedItem;
    }

    @Override
    public StackedSpawner getStackedSpawner(CreatureSpawner spawner) {
        if(dataHandler.CACHED_OBJECTS.containsKey(spawner.getLocation()))
            return (StackedSpawner) dataHandler.CACHED_OBJECTS.get(spawner.getLocation());

        StackedSpawner stackedSpawner = new WStackedSpawner(spawner);

        dataHandler.CACHED_OBJECTS.put(spawner.getLocation(), stackedSpawner);

        return stackedSpawner;
    }

    @Override
    public StackedBarrel getStackedBarrel(Block block) {
        if(dataHandler.CACHED_OBJECTS.containsKey(block.getLocation()))
            return (StackedBarrel) dataHandler.CACHED_OBJECTS.get(block.getLocation());

        StackedBarrel stackedBarrel = new WStackedBarrel(block.getLocation(), block.getState().getData().toItemStack(1));
        stackedBarrel.createDisplayBlock();

        dataHandler.CACHED_OBJECTS.put(block.getLocation(), stackedBarrel);

        return stackedBarrel;
    }

    @Override
    public List<StackedEntity> getStackedEntities() {
        List<StackedEntity> stackedEntities = new ArrayList<>();

        for(StackedObject stackedObject : dataHandler.CACHED_OBJECTS.values()){
            if(stackedObject instanceof StackedEntity)
                stackedEntities.add((StackedEntity) stackedObject);
        }

        return stackedEntities;
    }

    @Override
    public List<StackedItem> getStackedItems() {
        List<StackedItem> stackedItems = new ArrayList<>();

        for(StackedObject stackedObject : dataHandler.CACHED_OBJECTS.values()){
            if(stackedObject instanceof StackedItem)
                stackedItems.add((StackedItem) stackedObject);
        }

        return stackedItems;
    }

    @Override
    public List<StackedSpawner> getStackedSpawners(){
        List<StackedSpawner> stackedSpawners = new ArrayList<>();

        for(StackedObject stackedObject : dataHandler.CACHED_OBJECTS.values()){
            if(stackedObject instanceof StackedSpawner)
                stackedSpawners.add((StackedSpawner) stackedObject);
        }

        return stackedSpawners;
    }

    @Override
    public List<StackedBarrel> getStackedBarrels(){
        List<StackedBarrel> stackedBarrels = new ArrayList<>();

        for(StackedObject stackedObject : dataHandler.CACHED_OBJECTS.values()){
            if(stackedObject instanceof StackedBarrel)
                stackedBarrels.add((StackedBarrel) stackedObject);
        }

        return stackedBarrels;
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
        for(StackedEntity stackedEntity : getStackedEntities()) {
            if(stackedEntity.getType() == EntityType.ARMOR_STAND || stackedEntity.getStackAmount() == 1) {
                dataHandler.CACHED_OBJECTS.remove(stackedEntity.getUniqueId());
            }else if(!stackedEntity.getLivingEntity().isValid()) {
                Bukkit.getScheduler().runTask(instance, stackedEntity::remove);
            }
        }

        for(StackedItem stackedItem : getStackedItems()) {
            if(stackedItem.getStackAmount() == 1)
                dataHandler.CACHED_OBJECTS.remove(stackedItem.getUniqueId());
            if(isChunkLoaded(stackedItem.getItem().getLocation()) && (!stackedItem.getItem().isValid() || stackedItem.getItem().isDead())) {
                stackedItem.remove();
            }
        }

        for(StackedSpawner stackedSpawner : getStackedSpawners()) {
            if(stackedSpawner.getStackAmount() == 1)
                dataHandler.CACHED_OBJECTS.remove(stackedSpawner.getLocation());
            if(isChunkLoaded(stackedSpawner.getLocation()) && !isStackedSpawner(stackedSpawner.getSpawner().getBlock())) {
                stackedSpawner.remove();
            }
        }

        for(StackedBarrel stackedBarrel : getStackedBarrels()) {
            if(stackedBarrel.getStackAmount() == 1)
                dataHandler.CACHED_OBJECTS.remove(stackedBarrel.getLocation());
            if(isChunkLoaded(stackedBarrel.getLocation()) && !isStackedBarrel(stackedBarrel.getBlock())) {
                stackedBarrel.remove();
            }
        }
    }

    private boolean isChunkLoaded(Location location){
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public void updateLinkedEntity(LivingEntity livingEntity, LivingEntity newLivingEntity) {
        for(StackedSpawner stackedSpawner : getStackedSpawners()){
            if(stackedSpawner.getLinkedEntity() != null && stackedSpawner.getLinkedEntity().equals(livingEntity))
                stackedSpawner.setLinkedEntity(newLivingEntity);
        }
    }

    /*
     * General methods
     */

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T spawnEntityWithoutStacking(Location location, Class<T> type){
        try{
            World world = location.getWorld();

            Class craftWorldClass = ReflectionUtil.getBukkitClass("CraftWorld");
            Object craftWorld = craftWorldClass.cast(world);

            Class entityClass = ReflectionUtil.getNMSClass("Entity");

            Object entity = craftWorldClass.getMethod("createEntity", Location.class, Class.class).invoke(craftWorld, location, type);

            Entity bukkitEntity = (Entity) entity.getClass().getMethod("getBukkitEntity").invoke(entity);

            EntitiesListener.noStackEntities.add(bukkitEntity.getUniqueId());

            craftWorldClass.getMethod("addEntity", entityClass, CreatureSpawnEvent.SpawnReason.class)
                    .invoke(craftWorld, entity, CreatureSpawnEvent.SpawnReason.SPAWNER);

            return type.cast(bukkitEntity);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void performKillAll(){
        new Thread(() -> {
            for(StackedEntity stackedEntity : getStackedEntities()) {
                if (stackedEntity.getStackAmount() > 1)
                    stackedEntity.remove();
            }

            for(Player pl : Bukkit.getOnlinePlayers()) {
                if (pl.isOp())
                    Locale.KILL_ALL_OPS.send(pl);
            }
        }).start();
    }

    /*
     * Loot table methods
     */

    @Override
    public void registerCustomLootTable(LootTable lootTable) {
        xyz.wildseries.wildstacker.loot.LootTable.registerCustomLootTable(lootTable);
    }

    @Override
    public LootTable getNaturalLootTable(LivingEntity livingEntity) {
        return xyz.wildseries.wildstacker.loot.LootTable.forNaturalEntity(livingEntity);
    }
}
