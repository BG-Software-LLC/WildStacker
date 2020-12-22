package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.database.Query;
import com.bgsoftware.wildstacker.menu.SpawnersManageMenu;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.particles.ParticleWrapper;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WStackedSpawner extends WStackedHologramObject<CreatureSpawner> implements StackedSpawner {

    private SpawnersManageMenu spawnersManageMenu;
    private LivingEntity linkedEntity = null;
    private int spawnerUpgradeId = 0;

    public WStackedSpawner(CreatureSpawner creatureSpawner){
        this(creatureSpawner, 1);
    }

    public WStackedSpawner(CreatureSpawner creatureSpawner, int stackAmount){
        super(SyncedCreatureSpawner.of(creatureSpawner), stackAmount);
        if(plugin.getSettings().spawnersOverrideEnabled)
            plugin.getNMSSpawners().updateStackedSpawner(this);
    }

    @Override
    public void setStackAmount(int stackAmount, boolean updateName) {
        super.setStackAmount(stackAmount, updateName);
        Query.SPAWNER_INSERT.insertParameters().setLocation(getLocation()).setObject(getStackAmount())
                .setObject(spawnerUpgradeId).queue(getLocation());
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
        return object.getLocation();
    }

    @Override
    public World getWorld() {
        return object.getWorld();
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
        int limit = plugin.getSettings().spawnersLimits.getOrDefault(getSpawnedType(), Integer.MAX_VALUE);
        return limit < 1 ? Integer.MAX_VALUE : limit;
    }

    @Override
    public int getMergeRadius() {
        int radius = plugin.getSettings().spawnersMergeRadius.getOrDefault(getSpawnedType(), 0);
        return radius < 1 ? 0 : radius;
    }

    @Override
    public boolean isBlacklisted() {
        return plugin.getSettings().blacklistedSpawners.contains(getSpawnedType());
    }

    @Override
    public boolean isWhitelisted() {
        return plugin.getSettings().whitelistedSpawners.size() == 0 ||
                plugin.getSettings().whitelistedSpawners.contains(getSpawnedType());
    }

    @Override
    public boolean isWorldDisabled() {
        return plugin.getSettings().spawnersDisabledWorlds.contains(object.getWorld().getName());
    }

    @Override
    public boolean isCached() {
        return plugin.getSettings().spawnersStackingEnabled && super.isCached();
    }

    @Override
    public void remove() {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(this::remove);
            return;
        }

        plugin.getSystemManager().removeStackObject(this);

        Query.SPAWNER_DELETE.insertParameters().setLocation(getLocation()).queue(getLocation());

        removeHologram();

        if(spawnersManageMenu != null) {
            spawnersManageMenu.getInventory().getViewers().forEach(HumanEntity::closeInventory);
            spawnersManageMenu.stop();
            unlinkInventory();
        }
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

        if(amount <= 1 && spawnerUpgradeId == 0) {
            removeHologram();
            return;
        }

        customName = customName
                .replace("{0}", Integer.toString(amount))
                .replace("{1}", EntityUtils.getFormattedType(getSpawnedType().name()))
                .replace("{2}", EntityUtils.getFormattedType(getSpawnedType().name()).toUpperCase())
                .replace("{3}", getUpgrade().getDisplayName());

        setHologramName(customName, !plugin.getSettings().floatingSpawnerNames);
    }

    @Override
    public StackCheckResult runStackCheck(StackedObject stackedObject) {
        if(!plugin.getSettings().spawnersStackingEnabled)
            return StackCheckResult.NOT_ENABLED;

        return super.runStackCheck(stackedObject);
    }

    private boolean canStackIntoNoLimit(StackedObject stackedObject){
        StackCheckResult stackCheckResult = runStackCheck(stackedObject);
        return stackCheckResult == StackCheckResult.SUCCESS || stackCheckResult == StackCheckResult.LIMIT_EXCEEDED;
    }

    @Override
    public Optional<CreatureSpawner> runStack() {
        if(getStackLimit() <= 1)
            return Optional.empty();

        Chunk chunk = getChunk();

        boolean chunkMerge = plugin.getSettings().chunkMergeSpawners;
        Location blockLocation = getLocation();

        Stream<StackedSpawner> spawnerStream;

        if (chunkMerge) {
            spawnerStream = plugin.getSystemManager().getStackedSpawners(chunk).stream();
        } else {
            int range = getMergeRadius();

            if(range <= 0)
                return Optional.empty();

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

        Optional<StackedSpawner> spawnerOptional = GeneralUtils.getClosest(blockLocation, spawnerStream
                .filter(stackedSpawner -> runStackCheck(stackedSpawner) == StackCheckResult.SUCCESS));

        if (spawnerOptional.isPresent()) {
            StackedSpawner targetSpawner = spawnerOptional.get();

            StackResult stackResult = runStack(targetSpawner);

            if (stackResult == StackResult.SUCCESS) {
                return spawnerOptional.map(StackedSpawner::getSpawner);
            }
        }

        return Optional.empty();
    }

    @Override
    public StackResult runStack(StackedObject stackedObject) {
        if (!StackService.canStackFromThread())
            return StackResult.THREAD_CATCHER;

        if (runStackCheck(stackedObject) != StackCheckResult.SUCCESS)
            return StackResult.NOT_SIMILAR;

        StackedSpawner targetSpawner = (StackedSpawner) stackedObject;
        int newStackAmount = this.getStackAmount() + targetSpawner.getStackAmount();

        if(!EventsCaller.callSpawnerStackEvent(targetSpawner, this))
            return StackResult.EVENT_CANCELLED;

        targetSpawner.setStackAmount(newStackAmount, true);

        this.remove();

        spawnStackParticle(true);

        return StackResult.SUCCESS;
    }

    @Override
    public UnstackResult runUnstack(int amount, Entity entity) {
        if(!EventsCaller.callSpawnerUnstackEvent(this, entity, amount))
            return UnstackResult.EVENT_CANCELLED;

        int stackAmount = this.getStackAmount() - amount;

        setStackAmount(stackAmount, true);

        if(stackAmount < 1)
            remove();

        return UnstackResult.SUCCESS;
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        return stackedObject instanceof StackedSpawner && getSpawnedType() == ((StackedSpawner) stackedObject).getSpawnedType() &&
                spawnerUpgradeId == ((WStackedSpawner) stackedObject).getUpgradeId();
    }

    @Override
    public void spawnStackParticle(boolean checkEnabled) {
        if (!checkEnabled || plugin.getSettings().spawnersParticlesEnabled) {
            Location location = getLocation();
            for (ParticleWrapper particleWrapper : plugin.getSettings().spawnersParticles)
                particleWrapper.spawnParticle(location);
        }
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
     * UpgradeableStackedObject's methods
     */

    @Override
    public SpawnerUpgrade getUpgrade() {
        SpawnerUpgrade currentUpgrade = plugin.getUpgradesManager().getUpgrade(spawnerUpgradeId);
        return currentUpgrade == null ? plugin.getUpgradesManager().getDefaultUpgrade(getSpawnedType()) : currentUpgrade;
    }

    public int getUpgradeId(){
        return spawnerUpgradeId;
    }

    @Override
    public void setUpgrade(SpawnerUpgrade spawnerUpgrade) {
        setUpgradeId(spawnerUpgrade == null ? 0 : spawnerUpgrade.getId(), true);
        updateName();
    }

    public void setUpgradeId(int spawnerUpgradeId, boolean fireEvent){
        this.spawnerUpgradeId = spawnerUpgradeId;

        SpawnerUpgrade spawnerUpgrade = getUpgrade();

        if(fireEvent)
            EventsCaller.callSpawnerUpgradeEvent(this, spawnerUpgrade);

        SyncedCreatureSpawner.of(object).updateSpawner(spawnerUpgrade);

        Query.SPAWNER_INSERT.insertParameters().setLocation(getLocation()).setObject(getStackAmount())
                .setObject(spawnerUpgradeId).queue(getLocation());
    }

    /*
     * StackedSpawner's methods
     */

    @Override
    public LivingEntity getLinkedEntity(){
        if (linkedEntity != null && (!plugin.getSettings().linkedEntitiesEnabled || linkedEntity.isDead() || !linkedEntity.isValid() ||
                linkedEntity.getLocation().distanceSquared(getLocation()) > Math.pow(plugin.getSettings().linkedEntitiesMaxDistance, 2.0)))
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
            int range = getMergeRadius();
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

    @Override
    public ItemStack getDropItem() {
        return getDropItem(getStackAmount());
    }

    @Override
    public ItemStack getDropItem(int amount) {
        return plugin.getProviders().getSpawnerItem(object.getSpawnedType(), amount, getUpgrade().getDisplayName());
    }

    public LivingEntity getRawLinkedEntity(){
        return linkedEntity;
    }

    public void tick(int spawnCount, Random random, Entity demoEntityBukkit, Consumer<Integer> spawnMobsMethod, Runnable onFinish){
        final int mobsToSpawn;

        if(linkedEntity != null){
            StackedEntity linkedEntity = WStackedEntity.of(this.linkedEntity);
            int limit = linkedEntity.getStackLimit();
            int newStackAmount = linkedEntity.getStackAmount() + spawnCount;

            if(newStackAmount > limit) {
                mobsToSpawn = limit - linkedEntity.getStackAmount();
                newStackAmount = limit;
            }
            else{
                mobsToSpawn = spawnCount;
            }

            linkedEntity.setStackAmount(newStackAmount, true);
        }
        else{
            mobsToSpawn = spawnCount;
        }

        if(demoEntityBukkit == null) {
            onFinish.run();
            return;
        }

        int stackAmount = getStackAmount();

        StackedEntity demoEntity = WStackedEntity.of(demoEntityBukkit);
        demoEntity.setStackAmount(stackAmount + random.nextInt(mobsToSpawn - stackAmount + 1), false);
        ((WStackedEntity) demoEntity).setDemoEntity();

        demoEntity.runStackAsync(optionalEntity -> {
            if(optionalEntity.isPresent()) {
                if(plugin.getSettings().linkedEntitiesEnabled)
                    setLinkedEntity(optionalEntity.get());
                onFinish.run();
                return;
            }

            Executor.sync(() -> {
                spawnMobsMethod.accept(mobsToSpawn);
                onFinish.run();
            });
        });
    }

    public void linkInventory(SpawnersManageMenu spawnersManageMenu){
        this.spawnersManageMenu = spawnersManageMenu;
    }

    public SpawnersManageMenu getLinkedInventory(){
        return spawnersManageMenu;
    }

    public void unlinkInventory(){
        this.spawnersManageMenu = null;
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
