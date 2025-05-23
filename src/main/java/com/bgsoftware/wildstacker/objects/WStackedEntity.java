package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.enums.EntityFlag;
import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.enums.StackResult;
import com.bgsoftware.wildstacker.api.enums.UnstackResult;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.api.upgrades.SpawnerUpgrade;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.utils.GeneralUtils;
import com.bgsoftware.wildstacker.utils.entity.EntitiesGetter;
import com.bgsoftware.wildstacker.utils.entity.EntityStorage;
import com.bgsoftware.wildstacker.utils.entity.EntityUtils;
import com.bgsoftware.wildstacker.utils.entity.StackCheck;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemStackList;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.pair.Pair;
import com.bgsoftware.wildstacker.utils.particles.ParticleWrapper;
import com.bgsoftware.wildstacker.utils.threads.Executor;
import com.bgsoftware.wildstacker.utils.threads.StackService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class WStackedEntity extends WAsyncStackedObject<LivingEntity> implements StackedEntity {

    private final UUID cachedUUID;
    private final int cachedEntityId;
    private List<ItemStack> drops = null;
    private int dropsMultiplier = 1;
    private SpawnCause spawnCause;
    private int spawnerUpgradeId = -1;
    private Predicate<LivingEntity> stackFlag = null;
    private EntityType cachedType;
    private boolean spawnCorpse = true;

    public WStackedEntity(LivingEntity livingEntity) {
        super(livingEntity, 1);
        this.cachedUUID = livingEntity.getUniqueId();
        this.cachedEntityId = livingEntity.getEntityId();
        this.spawnCause = getAndRemoveFlag(EntityFlag.SPAWN_CAUSE);
        if (this.spawnCause == null)
            this.spawnCause = plugin.getNMSEntities().getEntitySpawnCause(livingEntity);
        setCachedDisplayName(EntityUtils.getFormattedType(getType().name()));
    }

    public static StackedEntity of(Entity entity) {
        if (entity instanceof LivingEntity)
            return of((LivingEntity) entity);

        throw new IllegalArgumentException("The entity-type " + entity.getType() + " is not a stackable entity.");
    }

    public static StackedEntity of(LivingEntity livingEntity) {
        if (EntityUtils.isStackable(livingEntity))
            return plugin.getSystemManager().getStackedEntity(livingEntity);

        String customFailureReason = plugin.getProviders().checkStackEntity(livingEntity);
        if (customFailureReason != null)
            throw new IllegalArgumentException(customFailureReason);

        throw new IllegalArgumentException("The entity-type " + livingEntity.getType() + " is not a stackable entity.");
    }

    /*
     * LivingEntity's methods
     */

    @Override
    public Location getLocation() {
        return object.getLocation();
    }

    @Override
    public Chunk getChunk() {
        return object.getLocation().getChunk();
    }

    @Override
    public int getStackLimit() {
        int limit = GeneralUtils.get(plugin.getSettings().entitiesLimits, this, Integer.MAX_VALUE);
        return limit < 1 ? Integer.MAX_VALUE : limit;
    }

    @Override
    public int getMergeRadius() {
        int radius = GeneralUtils.get(plugin.getSettings().entitiesMergeRadius, this, 0);
        return radius < 1 ? 0 : radius;
    }

    @Override
    public boolean isBlacklisted() {
        return GeneralUtils.contains(plugin.getSettings().blacklistedEntities, this);
    }

    @Override
    public boolean isWhitelisted() {
        return GeneralUtils.containsOrEmpty(plugin.getSettings().whitelistedEntities, this);
    }

    @Override
    public boolean isWorldDisabled() {
        return plugin.getSettings().entitiesDisabledWorlds.contains(object.getWorld().getName());
    }

    @Override
    public boolean isCached() {
        return plugin.getSettings().entitiesStackingEnabled && super.isCached();
    }

    @Override
    public void remove() {
        plugin.getSystemManager().removeStackObject(this);

        //Drop leash if exists
        if (object.isLeashed()) {
            ItemUtils.dropItem(new ItemStack(Materials.LEAD.toBukkitType()), getLocation());
            object.setLeashHolder(null);
        }

        /* Entities must be removed sync, otherwise they are not properly removed from chunks.
        Also, in 1.17, the remove() function must be called sync.
        Other than that, slimes must be removed sync as well.
        */
        Executor.sync(() -> {
            object.remove();
            Executor.sync(this::clearFlags, 100L);
        });

        setFlag(EntityFlag.REMOVED_ENTITY, true);
    }

    @Override
    public void updateName() {
        if (isNameBlacklisted() || hasNameTag())
            return;

        if (!plugin.getDataHandler().CACHED_ENTITIES.containsKey(getUniqueId()))
            return;

        try {
            String customName = EntityUtils.getEntityName(this);
            boolean nameVisible = (getStackAmount() > 1 || !isDefaultUpgrade()) && !plugin.getSettings().entitiesHideNames;

            Executor.sync(() -> {
                setCustomName(customName);
                setCustomNameVisible(nameVisible);
                plugin.getProviders().notifyNameChangeListeners(object);
            });
        } catch (NullPointerException ignored) {
        }
    }

    /*
     * StackedObject's methods
     */

    @Override
    public StackCheckResult runStackCheck(StackedObject stackedObject) {
        if (!plugin.getSettings().entitiesStackingEnabled)
            return StackCheckResult.NOT_ENABLED;

        StackCheckResult superResult = super.runStackCheck(stackedObject);

        if (superResult != StackCheckResult.SUCCESS)
            return superResult;

        StackCheckResult similarResult = EntityUtils.areSimilar(this, ((StackedEntity) stackedObject));

        if (similarResult != StackCheckResult.SUCCESS)
            return similarResult;

        if (isNameBlacklisted())
            return StackCheckResult.BLACKLISTED_NAME;

        if (!hasFlag(EntityFlag.DEMO_ENTITY) && (hasFlag(EntityFlag.REMOVED_ENTITY) || object.isDead() || !object.isValid()))
            return StackCheckResult.ALREADY_DEAD;

        if (StackCheck.NAME_TAG.isEnabled() && hasNameTag())
            return StackCheckResult.NAME_TAG;

        if (hasFlag(EntityFlag.BYPASS_STACKING))
            return StackCheckResult.BYPASS_STACKING;

        StackedEntity targetEntity = (StackedEntity) stackedObject;

        if (targetEntity.isNameBlacklisted())
            return StackCheckResult.TARGET_BLACKLISTD_NAME;

        if (targetEntity.hasFlag(EntityFlag.REMOVED_ENTITY) || targetEntity.getLivingEntity().isDead() ||
                !targetEntity.getLivingEntity().isValid())
            return StackCheckResult.TARGET_ALREADY_DEAD;

        if (hasFlag(EntityFlag.CORPSE))
            return StackCheckResult.CORPSE;

        if (StackCheck.NAME_TAG.isEnabled() && targetEntity.hasNameTag())
            return StackCheckResult.TARGET_NAME_TAG;

        if (StackCheck.UPGRADE.isEnabled() && spawnerUpgradeId != ((WStackedEntity) targetEntity).getUpgradeId() &&
                (!isDefaultUpgrade() || !targetEntity.isDefaultUpgrade()))
            return StackCheckResult.UPGRADE;

        if (StackCheck.NERFED.isEnabled() && isNerfed() != targetEntity.isNerfed())
            return StackCheckResult.NERFED;

        if (StackCheck.SPAWN_REASON.isEnabled() && getSpawnCause() != targetEntity.getSpawnCause())
            return StackCheckResult.SPAWN_REASON;

        if (StackCheck.CAN_BREED.isEnabled() && object instanceof Animals &&
                EntityUtils.canBeBred((Animals) object) != EntityUtils.canBeBred((Animals) targetEntity.getLivingEntity()))
            return StackCheckResult.BREED_STATUS;

        if (StackCheck.IS_IN_LOVE.isEnabled() && object instanceof Animals && (
                plugin.getNMSEntities().isInLove((Animals) object) ||
                        plugin.getNMSEntities().isInLove((Animals) targetEntity.getLivingEntity())))
            return StackCheckResult.IN_LOVE_STATUS;

        if (!plugin.getSettings().entitiesDisabledRegions.isEmpty()) {
            Set<String> regions = new HashSet<>();
            regions.addAll(plugin.getProviders().getRegionNames(targetEntity.getLivingEntity().getLocation()));
            regions.addAll(plugin.getProviders().getRegionNames(object.getLocation()));
            if (regions.stream().anyMatch(region -> plugin.getSettings().entitiesDisabledRegions.contains(region)))
                return StackCheckResult.DISABLED_REGION;
        }

        if (plugin.getSettings().stackDownEnabled && GeneralUtils.contains(plugin.getSettings().stackDownTypes, this)) {
            if (object.getLocation().getY() < targetEntity.getLivingEntity().getLocation().getY()) {
                targetEntity.runStackAsync(this, null);
                return StackCheckResult.NOT_BELOW;
            }
        }

        return StackCheckResult.SUCCESS;
    }

    @Override
    public StackResult runStack(StackedObject stackedObject) {
        if (!StackService.canStackFromThread())
            return StackResult.THREAD_CATCHER;

        if (runStackCheck(stackedObject) != StackCheckResult.SUCCESS)
            return StackResult.NOT_SIMILAR;

        StackedEntity targetEntity = (StackedEntity) stackedObject;

        if (!shouldBeStacked() || !((WStackedEntity) targetEntity).shouldBeStacked())
            return StackResult.NOT_SIMILAR;

        if (!EventsCaller.callEntityStackEvent(targetEntity, this))
            return StackResult.EVENT_CANCELLED;

        double health = GeneralUtils.contains(plugin.getSettings().keepLowestHealth, this) ?
                Math.min(getHealth(), targetEntity.getHealth()) : targetEntity.getHealth();

        targetEntity.increaseStackAmount(getStackAmount(), false);
        targetEntity.setHealth(Math.max(health, 0.5D));

        Executor.sync(() -> {
            if (targetEntity.getLivingEntity().isValid())
                targetEntity.updateName();
        }, 2L);

        plugin.getSystemManager().updateLinkedEntity(object, targetEntity.getLivingEntity());

        if (object.getType().name().equals("PARROT"))
            Executor.sync(() -> EntityUtils.removeParrotIfShoulder((Parrot) object));

        this.remove();

        spawnStackParticle(true);

        return StackResult.SUCCESS;
    }

    @Override
    public UnstackResult runUnstack(int amount, Entity entity) {
        if (hasFlag(EntityFlag.DEAD_ENTITY))
            return UnstackResult.ALREADY_DEAD;

        Pair<Boolean, Integer> eventResult = EventsCaller.callEntityUnstackEvent(this, entity, amount);

        if (!eventResult.getKey())
            return UnstackResult.EVENT_CANCELLED;

        int newStackAmount = decreaseStackAmount(eventResult.getValue(), true);

        if (newStackAmount >= 1) {
            if (spawnCorpse && plugin.getSettings().spawnCorpses)
                spawnCorpse();
        } else {
            if (!spawnCorpse) {
                remove();
            } else {
                setFlag(EntityFlag.REMOVED_ENTITY, true);
                Executor.sync(() -> {
                    setFlag(EntityFlag.CORPSE, true);
                    if (EntityTypes.fromEntity(object).isSlime())
                        setFlag(EntityFlag.ORIGINAL_AMOUNT, newStackAmount + eventResult.getValue());
                    plugin.getNMSEntities().setHealthDirectly(object, 0, false);
                    plugin.getNMSEntities().playDeathSound(object);
                    Executor.sync(this::clearFlags, 100L);
                }, 2L);
            }
        }

        // Reset spawn-corpse state
        spawnCorpse = true;

        return UnstackResult.SUCCESS;
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        return stackedObject instanceof StackedEntity;
    }

    @Override
    public void spawnStackParticle(boolean checkEnabled) {
        if (!checkEnabled || plugin.getSettings().entitiesParticlesEnabled) {
            Location location = getLivingEntity().getLocation();
            for (ParticleWrapper particleWrapper : plugin.getSettings().entitiesParticles)
                particleWrapper.spawnParticle(location);
        }
    }

    @Override
    public World getWorld() {
        return object.getWorld();
    }

    @Override
    public LivingEntity getLivingEntity() {
        return object;
    }

    @Override
    public UUID getUniqueId() {
        return cachedUUID;
    }

    @Override
    public EntityType getType() {
        if (cachedType == null)
            cachedType = object.getType();

        return cachedType;
    }

    @Override
    public double getHealth() {
        return object.getHealth();
    }

    @Override
    public void setHealth(double health) {
        object.setHealth(health);
    }

    @Override
    public String getCustomName() {
        return plugin.getNMSEntities().getCustomName(object);
    }

    @Override
    public void setCustomName(String customName) {
        plugin.getNMSEntities().setCustomName(object, customName);
    }

    @Override
    public boolean isCustomNameVisible() {
        return plugin.getNMSEntities().isCustomNameVisible(object);
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        plugin.getNMSEntities().setCustomNameVisible(object, visible);
    }

    @Override
    public void runSpawnerStackAsync(StackedSpawner stackedSpawner, Consumer<Optional<LivingEntity>> result) {
        if (!plugin.getSettings().linkedEntitiesEnabled) {
            runStackAsync(result);
            return;
        }

        LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

        Runnable regularStackAsync = () -> runStackAsync(entityOptional -> {
            LivingEntity targetEntity = entityOptional.orElse(object);
            stackedSpawner.setLinkedEntity(targetEntity);
            if (result != null)
                result.accept(Optional.of(targetEntity));
        });

        if (linkedEntity != null) {
            StackedEntity stackedLinkedEntity = WStackedEntity.of(linkedEntity);
            runStackAsync(stackedLinkedEntity, stackResult -> {
                if (stackResult == StackResult.SUCCESS) {
                    if (result != null)
                        result.accept(Optional.of(linkedEntity));
                } else {
                    regularStackAsync.run();
                }
            });
        } else {
            regularStackAsync.run();
        }
    }

    @Override
    public LivingEntity trySpawnerStack(StackedSpawner stackedSpawner) {
        new UnsupportedOperationException("trySpawnerStack method is no longer supported.").printStackTrace();
        runSpawnerStackAsync(stackedSpawner, null);
        return null;
    }

    @Override
    public StackedEntity spawnDuplicate(int amount) {
        return spawnDuplicate(amount, getSpawnCause());
    }

    @Override
    public StackedEntity spawnDuplicate(int amount, SpawnCause spawnCause) {
        return spawnDuplicate(amount, spawnCause, null);
    }

    @Nullable
    public StackedEntity spawnDuplicate(int amount, SpawnCause spawnCause, @Nullable Predicate<Entity> beforeSpawnConsumer) {
        if (amount <= 0)
            return null;

        StackedEntity duplicate;

        LivingEntity customDuplicate = plugin.getProviders().tryDuplicateEntity(object);
        if (customDuplicate != null) {
            duplicate = WStackedEntity.of(customDuplicate);
            duplicate.setStackAmount(amount, true);
        } else {
            duplicate = WStackedEntity.of(plugin.getSystemManager().spawnEntityWithoutStacking(
                    object.getLocation(), getType().getEntityClass(), spawnCause, beforeSpawnConsumer, null));

            duplicate.setStackAmount(amount, true);

            plugin.getNMSAdapter().updateEntity(object, duplicate.getLivingEntity());

            if (plugin.getSettings().keepFireEnabled && object.getFireTicks() > -1)
                duplicate.getLivingEntity().setFireTicks(160);
        }

        EventsCaller.callDuplicateSpawnEvent(this, duplicate);

        return duplicate;
    }

    @Override
    public void spawnCorpse() {
        if (!spawnCorpse)
            return;

        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(this::spawnCorpse);
            return;
        }

        plugin.getSystemManager().spawnCorpse(this);
    }

    /*
     * StackedEntity's methods
     */

    @Override
    public List<ItemStack> getDrops(int lootBonusLevel) {
        return getDrops(lootBonusLevel, getStackAmount());
    }

    @Override
    public List<ItemStack> getDrops(int lootBonusLevel, int stackAmount) {
        ItemStackList drops = new ItemStackList();

        if (this.drops != null) {
            drops.addAll(getTempDrops(stackAmount));
        } else {
            LootTable lootTable = plugin.getLootHandler().getLootTable(object);
            try {
                drops.addAll(lootTable.getDrops(this, lootBonusLevel, stackAmount));
            } catch (Exception ex) {
                WildStackerPlugin.log("Error while calculating drops for " + getType() + " with looting " + lootBonusLevel + " and stack size of " + stackAmount + ":");
                ex.printStackTrace();
            }
        }

        return drops.toList();
    }

    @Override
    public void setDrops(List<ItemStack> itemStacks) {
        this.drops = new ArrayList<>(itemStacks);
    }

    @Override
    @Deprecated
    public void setTempLootTable(List<ItemStack> itemStacks) {
        setDrops(itemStacks);
    }

    @Override
    public void setDropsMultiplier(int dropsMultiplier) {
        this.dropsMultiplier = dropsMultiplier;
    }

    @Override
    @Deprecated
    public void setLootMultiplier(int multiplier) {
        setDropsMultiplier(multiplier);
    }

    @Override
    public int getExp(int stackAmount, int defaultExp) {
        int exp = plugin.getLootHandler().getLootTable(object).getExp(this, stackAmount);
        return exp == 0 ? defaultExp : exp;
    }

    @Override
    public SpawnCause getSpawnCause() {
        return spawnCause == null ? SpawnCause.CHUNK_GEN : spawnCause;
    }

    @Override
    public void setSpawnCause(SpawnCause spawnCause) {
        this.spawnCause = spawnCause == null ? SpawnCause.CHUNK_GEN : spawnCause;
        if (saveData)
            plugin.getSystemManager().markToBeSaved(this);
    }

    @Override
    public boolean isNerfed() {
        return GeneralUtils.containsOrEmpty(plugin.getSettings().entitiesNerfedWhitelist, this) &&
                !GeneralUtils.contains(plugin.getSettings().entitiesNerfedBlacklist, this) &&
                (plugin.getSettings().entitiesNerfedWorlds.isEmpty() || plugin.getSettings().entitiesNerfedWorlds.contains(object.getWorld().getName()));
    }

    @Override
    public void setNerfed(boolean nerfed) {
        plugin.getNMSEntities().setNerfedEntity(object, nerfed);
    }

    @Override
    public void updateNerfed() {
        setNerfed(isNerfed());
    }

    @Override
    public boolean isNameBlacklisted() {
        return EntityUtils.isNameBlacklisted(getCustomName());
    }

    @Override
    public boolean isInstantKill(EntityDamageEvent.DamageCause damageCause) {
        return GeneralUtils.contains(plugin.getSettings().entitiesInstantKills, this, damageCause);
    }

    @Override
    public int getDefaultUnstack() {
        return Math.max(1, GeneralUtils.get(plugin.getSettings().defaultUnstack, this, 1));
    }

    @Override
    public boolean hasNameTag() {
        return hasFlag(EntityFlag.NAME_TAG);
    }

    @Override
    public boolean hasFlag(EntityFlag entityFlag) {
        return EntityStorage.hasMetadata(cachedUUID, entityFlag);
    }

    @Override
    public <T> T getFlag(EntityFlag entityFlag) {
        return EntityStorage.getMetadata(cachedUUID, entityFlag);
    }

    @Override
    public void setFlag(EntityFlag entityFlag, Object value) {
        EntityStorage.setMetadata(cachedUUID, entityFlag, value);
    }

    @Override
    public void removeFlag(EntityFlag entityFlag) {
        EntityStorage.removeMetadata(cachedUUID, entityFlag);
    }

    @Override
    public <T> T getAndRemoveFlag(EntityFlag entityFlag) {
        return EntityStorage.removeMetadata(cachedUUID, entityFlag);
    }

    @Override
    public void clearFlags() {
        EntityStorage.clearMetadata(cachedUUID);
    }

    @Override
    public void setSpawnCorpse(boolean spawnCorpse) {
        this.spawnCorpse = spawnCorpse;
    }

    @Override
    public int getId() {
        return cachedEntityId;
    }

    @Override
    public void runStackAsync(Consumer<Optional<LivingEntity>> result) {
        // Should be called sync due to collecting nearby entities
        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(() -> runStackAsync(result));
            return;
        }

        int range = getMergeRadius();
        Location entityLocation = getLivingEntity().getLocation();

        if (range <= 0 || getStackLimit() <= 1) {
            if (result != null)
                result.accept(Optional.empty());
            return;
        }

        List<StackedEntity> nearbyEntities = EntitiesGetter.getNearbyEntities(entityLocation, range, EntityUtils::isStackable)
                .map(WStackedEntity::of).filter(stackedEntity -> runStackCheck(stackedEntity) == StackCheckResult.SUCCESS)
                .collect(Collectors.toList());

        if (!nearbyEntities.isEmpty()) {
            int minimumStackSize = GeneralUtils.get(plugin.getSettings().minimumRequiredEntities, this, 1);
            StackedEntity targetEntity = nearbyEntities.get(0);

            if (minimumStackSize > 2) {
                int totalStackSize = getStackAmount();

                for (StackedEntity stackedEntity : nearbyEntities)
                    totalStackSize += stackedEntity.getStackAmount();

                if (totalStackSize < minimumStackSize) {
                    updateName();
                    if (result != null)
                        result.accept(Optional.empty());
                    return;
                }

                nearbyEntities.forEach(nearbyEntity -> nearbyEntity.runStackAsync(targetEntity, null));
            }

            runStackAsync(targetEntity, stackResult -> {
                if (stackResult != StackResult.SUCCESS) {
                    updateName();
                    if (result != null)
                        result.accept(Optional.empty());
                } else {
                    if (result != null)
                        result.accept(Optional.of(targetEntity.getLivingEntity()));
                }
            });
        } else {
            if (result != null)
                result.accept(Optional.empty());
        }
    }

    @Override
    public int hashCode() {
        return getUniqueId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StackedEntity && getUniqueId().equals(((StackedEntity) obj).getUniqueId());
    }

    @Override
    public String toString() {
        return String.format("StackedEntity{uuid=%s,amount=%s,type=%s}", getUniqueId(), getStackAmount(), getType().name());
    }

    private List<ItemStack> getTempDrops(int stackAmount) {
        List<ItemStack> filteredDrops = this.dropsMultiplier <= 0 ? new ArrayList<>() : this.drops.stream()
                .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR && itemStack.getAmount() > 0)
                .collect(Collectors.toList());

        int dropsMultiplier = Math.max(0, this.dropsMultiplier);

        // Reset drop fields
        this.drops = null;
        this.dropsMultiplier = 1;

        List<ItemStack> finalDrops = new ArrayList<>();

        filteredDrops.forEach(itemStack -> {
            ItemStack cloned = itemStack.clone();
            cloned.setAmount(itemStack.getAmount() * stackAmount * dropsMultiplier);
            finalDrops.add(cloned);
        });

        return finalDrops;
    }

    public void setNameTag() {
        setFlag(EntityFlag.NAME_TAG, true);
        if (saveData)
            plugin.getSystemManager().markToBeSaved(this);
    }

    public void setDeadFlag(boolean deadEntityFlag) {
        if (deadEntityFlag)
            setFlag(EntityFlag.DEAD_ENTITY, true);
        else
            removeFlag(EntityFlag.DEAD_ENTITY);

        if (!isCached())
            plugin.getDataHandler().CACHED_DEAD_ENTITIES.add(object.getUniqueId());
    }

    public boolean shouldBeStacked() {
        if (stackFlag == null)
            return true;

        if (stackFlag.test(object)) {
            stackFlag = null;
            return true;
        }

        return false;
    }

    /*
     * UpgradeableStackedObject's methods
     */

    public void setStackFlag(Predicate<LivingEntity> stackFlag) {
        this.stackFlag = stackFlag;
    }

    public void setDemoEntity() {
        // Demo entities should not be cached!
        plugin.getSystemManager().removeStackObject(this);
        setFlag(EntityFlag.DEMO_ENTITY, true);
    }

    @Override
    public SpawnerUpgrade getUpgrade() {
        SpawnerUpgrade currentUpgrade = plugin.getUpgradesManager().getUpgrade(spawnerUpgradeId);

        if (currentUpgrade == null) {
            SpawnerUpgrade defaultUpgrade = plugin.getUpgradesManager().getDefaultUpgrade(getType());
            spawnerUpgradeId = defaultUpgrade.getId();
            return defaultUpgrade;
        }

        return currentUpgrade;
    }

    @Override
    public void setUpgrade(SpawnerUpgrade spawnerUpgrade) {
        setUpgrade(spawnerUpgrade, null);
    }

    @Override
    public void setUpgrade(SpawnerUpgrade spawnerUpgrade, @Nullable Player player) {
        setUpgradeId(spawnerUpgrade == null ? -1 : spawnerUpgrade.getId());
        updateName();
    }

    @Override
    public boolean isDefaultUpgrade() {
        return spawnerUpgradeId == -1 || spawnerUpgradeId == plugin.getUpgradesManager().getDefaultUpgrade(getType()).getId();
    }

    /*
     * Other methods
     */

    public int getUpgradeId() {
        return spawnerUpgradeId;
    }

    public void setUpgradeId(int spawnerUpgradeId) {
        this.spawnerUpgradeId = spawnerUpgradeId;
    }
}
