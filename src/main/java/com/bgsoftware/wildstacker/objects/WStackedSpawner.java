package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
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
    public void remove() {
        plugin.getSystemManager().removeStackObject(this);
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

        if(plugin.getSettings().spawnersDisabledWorlds.contains(object.getWorld().getName()))
            return false;

        StackedSpawner targetSpawner = (StackedSpawner) stackedObject;

        if(plugin.getSettings().blacklistedSpawners.contains(object.getSpawnedType().name()) ||
                plugin.getSettings().blacklistedSpawners.contains(targetSpawner.getSpawnedType().name()))
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

        Location location = getLocation();

        int maxX = location.getBlockX() + range, maxY = location.getBlockY() + range, maxZ = location.getBlockZ() + range;
        int minX = location.getBlockX() - range, minY = location.getBlockY() - range, minZ = location.getBlockZ() - range;

        if(chunkMerge) {
            minX = 0; maxX = 16;
            minZ = 0; maxZ = 16;
        }

        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = chunkMerge ? location.getChunk().getBlock(x, y, z) : location.getWorld().getBlockAt(x, y, z);
                    if (block.getState() instanceof CreatureSpawner && !block.getLocation().equals(location)) {
                        StackedSpawner stackedSpawner = WStackedSpawner.of(block);
                        if (canStackIntoNoLimit(stackedSpawner))
                            stackedSpawners.add(stackedSpawner);
                    }
                }
            }
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
