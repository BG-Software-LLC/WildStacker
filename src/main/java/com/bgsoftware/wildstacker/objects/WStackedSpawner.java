package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WStackedSpawner extends WStackedHologramObject<CreatureSpawner> implements StackedSpawner {

    private SpawnersManageMenu spawnersManageMenu;
    private LivingEntity linkedEntity = null;
    private int spawnerUpgradeId = -1;
    private EntityType cachedEntity;

    public WStackedSpawner(CreatureSpawner creatureSpawner) {
        this(creatureSpawner, 1);
    }

    public WStackedSpawner(CreatureSpawner creatureSpawner, int stackAmount) {
        super(SyncedCreatureSpawner.of(creatureSpawner), stackAmount);
        if (plugin.getSettings().spawnersOverrideEnabled)
            plugin.getNMSSpawners().updateStackedSpawner(this);
        cachedEntity = creatureSpawner.getSpawnedType();
    }

    public static StackedSpawner of(Block block) {
        if (block.getState() instanceof CreatureSpawner)
            return of((CreatureSpawner) block.getState());
        throw new IllegalArgumentException("Only spawners can be applied to StackedSpawner object");
    }

    public static StackedSpawner of(CreatureSpawner creatureSpawner) {
        return plugin.getSystemManager().getStackedSpawner(creatureSpawner);
    }

    @Override
    public CreatureSpawner getSpawner() {
        return object;
    }

    @Override
    public EntityType getSpawnedType() {
        if (object.getSpawnedType() == null)
            object.setSpawnedType(EntityType.PIG);
        return Bukkit.isPrimaryThread() ? (cachedEntity = object.getSpawnedType()) : cachedEntity;
    }

    /*
     * StackedObject's methods
     */

    @Override
    public LivingEntity getLinkedEntity() {
        if (linkedEntity != null && (!plugin.getSettings().linkedEntitiesEnabled || linkedEntity.isDead() || !linkedEntity.isValid() ||
                linkedEntity.getLocation().distanceSquared(getLocation()) > Math.pow(plugin.getSettings().linkedEntitiesMaxDistance, 2.0)))
            linkedEntity = null;
        return linkedEntity;
    }

    @Override
    public void setLinkedEntity(LivingEntity linkedEntity) {
        this.linkedEntity = linkedEntity;
    }

    @Override
    public List<StackedSpawner> getNearbySpawners() {
        boolean chunkMerge = plugin.getSettings().chunkMergeSpawners;

        Stream<StackedSpawner> spawnerStream;

        if (chunkMerge) {
            spawnerStream = plugin.getSystemManager().getStackedSpawners(getChunk()).stream();
        } else {
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
        return plugin.getProviders().getSpawnersProvider().getSpawnerItem(object.getSpawnedType(), amount, getUpgrade());
    }

    @Override
    public Location getLocation() {
        return object.getLocation();
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
        return plugin.getSettings().spawnersStackingEnabled && (!isDefaultUpgrade(spawnerUpgradeId, getSpawnedType()) || super.isCached());
    }

    @Override
    public void remove() {
        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(this::remove);
            return;
        }

        plugin.getSystemManager().removeStackObject(this);

        Query.SPAWNER_DELETE.getStatementHolder()
                .setLocation(getLocation())
                .execute(true);

        removeHologram();

        if (spawnersManageMenu != null) {
            spawnersManageMenu.getInventory().getViewers().forEach(HumanEntity::closeInventory);
            spawnersManageMenu.stop();
            unlinkInventory();
        }
    }

    @Override
    public void updateName() {
        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(this::updateName);
            return;
        }

        String customName = plugin.getSettings().spawnersCustomName;

        if (customName.isEmpty())
            return;

        int amount = getStackAmount();

        if ((amount < 1 || (amount == 1 && !plugin.getSettings().spawnersUnstackedCustomName)) &&
                isDefaultUpgrade(spawnerUpgradeId, getSpawnedType())) {
            removeHologram();
            return;
        }

        setCachedDisplayName(EntityUtils.getFormattedType(getSpawnedType().name()));
        customName = plugin.getSettings().spawnersNameBuilder.build(this);
        setHologramName(customName, !plugin.getSettings().floatingSpawnerNames);
    }

    @Override
    public StackCheckResult runStackCheck(StackedObject stackedObject) {
        if (!plugin.getSettings().spawnersStackingEnabled)
            return StackCheckResult.NOT_ENABLED;

        return super.runStackCheck(stackedObject);
    }

    @Override
    public Optional<CreatureSpawner> runStack() {
        if (getStackLimit() <= 1)
            return Optional.empty();

        Chunk chunk = getChunk();

        boolean chunkMerge = plugin.getSettings().chunkMergeSpawners;
        Location blockLocation = getLocation();

        Stream<StackedSpawner> spawnerStream;

        if (chunkMerge) {
            spawnerStream = plugin.getSystemManager().getStackedSpawners(chunk).stream();
        } else {
            int range = getMergeRadius();

            if (range <= 0)
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

        if (!EventsCaller.callSpawnerStackEvent(targetSpawner, this))
            return StackResult.EVENT_CANCELLED;

        targetSpawner.increaseStackAmount(getStackAmount(), true);

        this.remove();

        spawnStackParticle(true);

        return StackResult.SUCCESS;
    }

    @Override
    public UnstackResult runUnstack(int amount, Entity entity) {
        if (!EventsCaller.callSpawnerUnstackEvent(this, entity, amount))
            return UnstackResult.EVENT_CANCELLED;

        int newStackAmount = decreaseStackAmount(amount, true);

        if (newStackAmount < 1)
            remove();

        return UnstackResult.SUCCESS;
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        if(!(stackedObject instanceof StackedSpawner))
            return false;

        StackedSpawner otherSpawner = (StackedSpawner) stackedObject;

        EntityType otherSpawnerType = otherSpawner.getSpawnedType();

        if(getSpawnedType() != otherSpawnerType)
            return false;

        int otherUpgradeId = ((WStackedSpawner) otherSpawner).getUpgradeId();

        if(spawnerUpgradeId == otherUpgradeId)
            return true;

        return isDefaultUpgrade(spawnerUpgradeId, getSpawnedType()) == isDefaultUpgrade(otherUpgradeId, otherSpawnerType);
    }

    /*
     * UpgradeableStackedObject's methods
     */

    @Override
    public void spawnStackParticle(boolean checkEnabled) {
        if (!checkEnabled || plugin.getSettings().spawnersParticlesEnabled) {
            Location location = getLocation();
            for (ParticleWrapper particleWrapper : plugin.getSettings().spawnersParticles)
                particleWrapper.spawnParticle(location);
        }
    }

    @Override
    public Chunk getChunk() {
        return getLocation().getChunk();
    }

    @Override
    public World getWorld() {
        return object.getWorld();
    }

    private boolean canStackIntoNoLimit(StackedObject stackedObject) {
        StackCheckResult stackCheckResult = runStackCheck(stackedObject);
        return stackCheckResult == StackCheckResult.SUCCESS || stackCheckResult == StackCheckResult.LIMIT_EXCEEDED;
    }

    /*
     * StackedSpawner's methods
     */

    @Override
    public int hashCode() {
        return getLocation().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StackedSpawner ? getLocation().equals(((StackedSpawner) obj).getLocation()) : super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("StackedSpawner{location=%s,amount=%s,type=%s}", getLocation(), getStackAmount(), getSpawnedType());
    }

    @Override
    public SpawnerUpgrade getUpgrade() {
        SpawnerUpgrade currentUpgrade = plugin.getUpgradesManager().getUpgrade(spawnerUpgradeId);
        return currentUpgrade == null ? plugin.getUpgradesManager().getDefaultUpgrade(getSpawnedType()) : currentUpgrade;
    }

    @Override
    public void setUpgrade(SpawnerUpgrade spawnerUpgrade) {
        setUpgrade(spawnerUpgrade, null);
    }

    @Override
    public void setUpgrade(SpawnerUpgrade spawnerUpgrade, @Nullable Player player) {
        setUpgradeId(spawnerUpgrade == null ? 0 : spawnerUpgrade.getId(), player, true);
        updateName();
    }

    public int getUpgradeId() {
        return spawnerUpgradeId;
    }

    public void setUpgradeId(int spawnerUpgradeId, @Nullable Player who, boolean fireEvent) {
        if (!isDefaultUpgrade(spawnerUpgradeId, getSpawnedType()) && !isCached())
            plugin.getDataHandler().addStackedSpawner(this);

        this.spawnerUpgradeId = spawnerUpgradeId;

        if (spawnerUpgradeId == 0 && !isCached())
            plugin.getDataHandler().removeStackedSpawner(this);

        SpawnerUpgrade spawnerUpgrade = getUpgrade();

        if (fireEvent)
            EventsCaller.callSpawnerUpgradeEvent(this, spawnerUpgrade, who);

        SyncedCreatureSpawner.of(object).updateSpawner(spawnerUpgrade);

        if (saveData)
            plugin.getSystemManager().markToBeSaved(this);
    }

    private static boolean isDefaultUpgrade(int spawnerUpgradeId, EntityType spawnerType) {
        return spawnerUpgradeId == -1 || spawnerUpgradeId == plugin.getUpgradesManager().getDefaultUpgrade(spawnerType).getId();
    }

    public LivingEntity getRawLinkedEntity() {
        return linkedEntity;
    }

    public void linkInventory(SpawnersManageMenu spawnersManageMenu) {
        this.spawnersManageMenu = spawnersManageMenu;
    }

    public SpawnersManageMenu getLinkedInventory() {
        return spawnersManageMenu;
    }

    public void unlinkInventory() {
        this.spawnersManageMenu = null;
    }

}
