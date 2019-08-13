package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.events.SpawnerAmountChangeEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.database.SQLHelper;
import com.bgsoftware.wildstacker.utils.EntityUtil;
import com.bgsoftware.wildstacker.utils.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("RedundantIfStatement")
public class WStackedSpawner extends WStackedObject<CreatureSpawner> implements StackedSpawner {

    private LivingEntity linkedEntity = null;

    public WStackedSpawner(CreatureSpawner creatureSpawner){
        this(creatureSpawner, 1);
    }

    public WStackedSpawner(CreatureSpawner creatureSpawner, int stackAmount){
        super(creatureSpawner, stackAmount);

        if(plugin.getSettings().spawnersStackingEnabled) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (getLocation().getBlock().getState() instanceof CreatureSpawner) {
                    SQLHelper.runIfConditionNotExist("SELECT * FROM spawners WHERE location ='" + SQLHelper.getLocation(getLocation()) + "';", () ->
                            Query.SPAWNER_INSERT.getStatementHolder()
                                    .setLocation(getLocation())
                                    .setInt(getStackAmount())
                                    .execute(true)
                    );
                }
            }, 1L);
        }
    }

    @Override
    public void setStackAmount(int stackAmount, boolean updateName) {
        SpawnerAmountChangeEvent spawnerAmountChangeEvent = new SpawnerAmountChangeEvent(this, stackAmount);
        Bukkit.getPluginManager().callEvent(spawnerAmountChangeEvent);
        super.setStackAmount(stackAmount, updateName);
        Query.SPAWNER_UPDATE_STACK_AMOUNT.getStatementHolder()
                .setInt(getStackAmount())
                .setLocation(getLocation())
                .execute(true);
    }

    @Override
    public CreatureSpawner getSpawner(){
        return object;
    }

    @Override
    public EntityType getSpawnedType(){
        if(object.getSpawnedType() == null)
            object.setSpawnedType(EntityType.PIG);
        return object.getSpawnedType();
    }

    @Override
    public Location getLocation() {
        return object.getLocation().clone();
    }

    /*
     * StackedObject's methods
     */

    @Override
    public Chunk getChunk() {
        return getLocation().getChunk();
    }

    @Override
    public int getStackLimit() {
        return plugin.getSettings().spawnersLimits.getOrDefault(getSpawnedType().name(), Integer.MAX_VALUE);
    }

    @Override
    public boolean isBlacklisted() {
        return plugin.getSettings().blacklistedSpawners.contains(getSpawnedType().name());
    }

    @Override
    public boolean isWhitelisted() {
        return plugin.getSettings().whitelistedSpawners.isEmpty() ||
                plugin.getSettings().whitelistedSpawners.contains(getSpawnedType().name());
    }

    @Override
    public boolean isWorldDisabled() {
        return plugin.getSettings().spawnersDisabledWorlds.contains(object.getWorld().getName());
    }

    @Override
    public void remove() {
        plugin.getSystemManager().removeStackObject(this);

        Query.SPAWNER_DELETE.getStatementHolder()
                .setLocation(getLocation())
                .execute(true);

        plugin.getProviders().deleteHologram(this);
    }

    @Override
    public void updateName() {
        String customName = plugin.getSettings().hologramCustomName;

        if (customName.isEmpty())
            return;

        int amount = getStackAmount();

        if(amount <= 1) {
            plugin.getProviders().deleteHologram(this);
            return;
        }

        customName = customName
                .replace("{0}", Integer.toString(amount))
                .replace("{1}", EntityUtil.getFormattedType(getSpawnedType().name()))
                .replace("{2}", EntityUtil.getFormattedType(getSpawnedType().name()).toUpperCase());
        plugin.getProviders().changeLine(this, customName, !plugin.getSettings().floatingSpawnerNames);
    }

    @Override
    public CreatureSpawner tryStack(){
        for(StackedSpawner stackedSpawner : getNearbySpawners()){
            if(tryStackInto(stackedSpawner))
                return stackedSpawner.getSpawner();
        }

        updateName();
        return null;
    }

    @Override
    public boolean canStackInto(StackedObject stackedObject) {
        if(!canStackIntoNoLimit(stackedObject))
            return false;

        StackedSpawner targetSpawner = (StackedSpawner) stackedObject;
        int newStackAmount = this.getStackAmount() + targetSpawner.getStackAmount();

        if(getStackLimit() < newStackAmount)
            return false;

        return true;
    }

    private boolean canStackIntoNoLimit(StackedObject stackedObject){
        if(!plugin.getSettings().spawnersStackingEnabled)
            return false;

        if(equals(stackedObject) || !(stackedObject instanceof StackedSpawner) || !isSimilar(stackedObject))
            return false;

        if(!isWhitelisted() || isBlacklisted() || isWorldDisabled())
            return false;

        StackedSpawner targetSpawner = (StackedSpawner) stackedObject;

        if(!targetSpawner.isWhitelisted() || targetSpawner.isBlacklisted() || targetSpawner.isWorldDisabled())
            return false;

        return true;
    }

    @Override
    public boolean tryStackInto(StackedObject stackedObject){
        if(!canStackInto(stackedObject))
            return false;

        StackedSpawner targetSpawner = (StackedSpawner) stackedObject;
        int newStackAmount = this.getStackAmount() + targetSpawner.getStackAmount();

        SpawnerStackEvent spawnerStackEvent = new SpawnerStackEvent(targetSpawner, this);
        Bukkit.getPluginManager().callEvent(spawnerStackEvent);

        if(spawnerStackEvent.isCancelled())
            return false;

        targetSpawner.setStackAmount(newStackAmount, true);

        this.remove();

        return true;
    }

    @Override
    public boolean tryUnstack(int amount) {
        SpawnerUnstackEvent event = new SpawnerUnstackEvent(this, amount);
        Bukkit.getPluginManager().callEvent(event);

        if(event.isCancelled())
            return false;

        setStackAmount(stackAmount - amount, true);

        if(stackAmount < 1) {
            Executor.sync(this::remove, 2L);
        }

        return true;
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        return stackedObject instanceof StackedSpawner && getSpawnedType() == ((StackedSpawner) stackedObject).getSpawnedType();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StackedSpawner ? getLocation().equals(((StackedSpawner) obj).getLocation()) : super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("StackedSpawner{location=%s,amount=%s,type=%s}", getLocation(), getStackAmount(), getSpawnedType());
    }

    /*
     * StackedSpawner's methods
     */

    @Override
    public LivingEntity getLinkedEntity(){
        if (linkedEntity != null && (!linkedEntity.isValid() || linkedEntity.getLocation().distanceSquared(getLocation()) > Math.pow(plugin.getSettings().linkedEntitiesMaxDistance, 2.0)))
            linkedEntity = null;
        return linkedEntity;
    }

    @Override
    public void setLinkedEntity(LivingEntity linkedEntity){
        this.linkedEntity = linkedEntity;
    }

    @Override
    public List<StackedSpawner> getNearbySpawners() {
        List<StackedSpawner> stackedSpawners = new ArrayList<>();

        boolean chunkMerge = plugin.getSettings().chunkMergeSpawners;
        int range = plugin.getSettings().spawnersCheckRange;

        List<Chunk> chunksToScan = new ArrayList<>();

        Location location = getLocation();
        int maxX = location.getBlockX() + range, maxY = location.getBlockY() + range, maxZ = location.getBlockZ() + range;
        int minX = location.getBlockX() - range, minY = location.getBlockY() - range, minZ = location.getBlockZ() - range;

        if(chunkMerge){
            chunksToScan.add(location.getChunk());
            minX = location.getChunk().getX() * 16;
            maxX = location.getChunk().getX() * 16 + 15;
            minZ = location.getChunk().getZ() * 16;
            maxZ = location.getChunk().getZ() * 16 + 15;
        }
        else{
            Location minLocation = new Location(location.getWorld(), minX, 0, minZ);
            Location maxLocation = new Location(location.getWorld(), maxX, 0, maxZ);
            for(int x = minLocation.getChunk().getX(); x <= maxLocation.getChunk().getX(); x++){
                for(int z = minLocation.getChunk().getZ(); z <= maxLocation.getChunk().getZ(); z++){
                    chunksToScan.add(location.getWorld().getChunkAt(x, z));
                }
            }
        }

        final int MAX_X = maxX, MIN_X = minX, MAX_Z = maxZ, MIN_Z = minZ;

        for(Chunk chunk : chunksToScan){
            plugin.getNMSAdapter().getTileEntities(chunk, blockState ->
                    blockState instanceof CreatureSpawner &&
                            blockState.getX() >= MIN_X && blockState.getX() <= MAX_X &&
                            blockState.getY() >= minY && blockState.getY() <= maxY &&
                            blockState.getZ() >= MIN_Z && blockState.getZ() <= MAX_Z)
                    .forEach(blockState -> {
                        StackedSpawner stackedSpawner = WStackedSpawner.of(blockState.getBlock());
                        if (canStackIntoNoLimit(stackedSpawner))
                            stackedSpawners.add(stackedSpawner);
                    });
        }

        return stackedSpawners;
    }

    public LivingEntity getRawLinkedEntity(){
        return linkedEntity;
    }

    public static StackedSpawner of(Block block){
        if(block.getState() instanceof CreatureSpawner)
            return of((CreatureSpawner) block.getState());
        throw new IllegalArgumentException("Only spawners can be applied to StackedSpawner object");
    }

    public static StackedSpawner of(CreatureSpawner creatureSpawner){
        return plugin.getSystemManager().getStackedSpawner(creatureSpawner);
    }

}
