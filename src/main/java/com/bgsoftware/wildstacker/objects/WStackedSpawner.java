package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.database.SQLHelper;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.entity.EntityUtil;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(this::remove);
            return;
        }

        plugin.getSystemManager().removeStackObject(this);

        Query.SPAWNER_DELETE.getStatementHolder()
                .setLocation(getLocation())
                .execute(true);

        plugin.getProviders().deleteHologram(this);
    }

    @Override
    public void updateName() {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(this::updateName);
            return;
        }

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
    public void runStackAsync(Consumer<Optional<CreatureSpawner>> result) {
        Chunk chunk = getChunk();

        StackService.execute(() -> {
            boolean chunkMerge = plugin.getSettings().chunkMergeSpawners;

            Stream<StackedSpawner> spawnerStream;

            if(chunkMerge){
                spawnerStream = plugin.getSystemManager().getStackedSpawners(chunk).stream();
            }

            else{
                int range = plugin.getSettings().spawnersCheckRange;
                Location location = getLocation();

                int maxX = location.getBlockX() + range, maxY = location.getBlockY() + range, maxZ = location.getBlockZ() + range;
                int minX = location.getBlockX() - range, minY = location.getBlockY() - range, minZ = location.getBlockZ() - range;

                spawnerStream = plugin.getSystemManager().getStackedSpawners().stream()
                        .filter(stackedSpawner -> {
                            Location loc = stackedSpawner.getLocation();
                            return loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                                    loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
                                    loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
                        });
            }

            spawnerStream = spawnerStream.filter(this::canStackInto);

            Optional<StackedSpawner> spawnerOptional = spawnerStream.findFirst();

            if(spawnerOptional.isPresent()){
                StackedSpawner targetSpawner = spawnerOptional.get();

                StackResult stackResult = runStack(targetSpawner);

                if(stackResult == StackResult.SUCCESS) {
                    if(result != null)
                        result.accept(spawnerOptional.map(StackedSpawner::getSpawner));
                    return;
                }
            }

            updateName();

            if(result != null)
                result.accept(Optional.empty());
        });
    }

    @Override
    public StackResult runStack(StackedObject stackedObject) {
        if(!StackService.canStackFromThread())
            return StackResult.THREAD_CATCHER;

        if(!canStackInto(stackedObject))
            return StackResult.NOT_SIMILAR;

        StackedSpawner targetSpawner = (StackedSpawner) stackedObject;
        int newStackAmount = this.getStackAmount() + targetSpawner.getStackAmount();

        SpawnerStackEvent spawnerStackEvent = new SpawnerStackEvent(targetSpawner, this);
        Bukkit.getPluginManager().callEvent(spawnerStackEvent);

        if(spawnerStackEvent.isCancelled())
            return StackResult.EVENT_CANCELLED;

        targetSpawner.setStackAmount(newStackAmount, true);

        this.remove();

        return StackResult.SUCCESS;
    }

    @Override
    public UnstackResult runUnstack(int amount) {
        SpawnerUnstackEvent spawnerUnstackEvent = new SpawnerUnstackEvent(this, amount);
        Bukkit.getPluginManager().callEvent(spawnerUnstackEvent);

        if(spawnerUnstackEvent.isCancelled())
            return UnstackResult.EVENT_CANCELLED;

        int stackAmount = this.stackAmount - amount;

        setStackAmount(stackAmount, true);

        if(stackAmount < 1)
            remove();

        return UnstackResult.SUCCESS;
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
        boolean chunkMerge = plugin.getSettings().chunkMergeSpawners;

        Stream<StackedSpawner> spawnerStream;

        if(chunkMerge){
            spawnerStream = plugin.getSystemManager().getStackedSpawners(getChunk()).stream();
        }

        else{
            int range = plugin.getSettings().spawnersCheckRange;
            Location location = getLocation();

            int maxX = location.getBlockX() + range, maxY = location.getBlockY() + range, maxZ = location.getBlockZ() + range;
            int minX = location.getBlockX() - range, minY = location.getBlockY() - range, minZ = location.getBlockZ() - range;

            spawnerStream = plugin.getSystemManager().getStackedSpawners().stream()
                    .filter(stackedSpawner -> {
                        Location loc = stackedSpawner.getLocation();
                        return loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                                loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
                                loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
                    });
        }

        return spawnerStream.filter(this::canStackIntoNoLimit).collect(Collectors.toList());
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
