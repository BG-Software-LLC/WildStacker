package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.events.DuplicateSpawnEvent;
import com.bgsoftware.wildstacker.api.events.EntityStackEvent;
import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.hooks.MythicMobsHook;
import com.bgsoftware.wildstacker.hooks.WorldGuardHook;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.LootTableTemp;
import com.bgsoftware.wildstacker.loot.custom.LootTableCustom;
import com.bgsoftware.wildstacker.utils.entity.EntityData;
import com.bgsoftware.wildstacker.utils.entity.EntityUtil;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.items.ItemStackList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WStackedEntity extends WStackedObject<LivingEntity> implements StackedEntity {

    private boolean ignoreDeathEvent = false;
    private SpawnCause spawnCause;
    private com.bgsoftware.wildstacker.api.loot.LootTable tempLootTable = null;

    public WStackedEntity(LivingEntity livingEntity){
        this(livingEntity, 1, null);
    }

    public WStackedEntity(LivingEntity livingEntity, int stackAmount, @Nullable SpawnCause spawnCause){
        super(livingEntity, stackAmount);
        this.spawnCause = spawnCause;
    }

    /*
     * LivingEntity's methods
     */

    @Override
    public LivingEntity getLivingEntity() {
        return object;
    }

    @Override
    public UUID getUniqueId(){
        return object.getUniqueId();
    }

    @Override
    public EntityType getType(){
        return object.getType();
    }

    @Override
    public void setHealth(double health){
        object.setHealth(health);
    }

    @Override
    public double getHealth(){
        return object.getHealth();
    }

    @Override
    public void setCustomName(String customName){
        object.setCustomName(customName);
    }

    @Override
    public void setCustomNameVisible(boolean visible){
        object.setCustomNameVisible(visible);
    }

    /*
     * StackedObject's methods
     */

    @Override
    public Chunk getChunk() {
        return object.getLocation().getChunk();
    }

    @Override
    public int getStackLimit() {
        return plugin.getSettings().entitiesLimits.getOrDefault(getType().name(), Integer.MAX_VALUE);
    }

    @Override
    public boolean isBlacklisted() {
        return plugin.getSettings().blacklistedEntities.contains(getType().name()) ||
                (spawnCause != null && plugin.getSettings().blacklistedEntities.contains(getSpawnCause().name()));
    }

    @Override
    public boolean isWhitelisted() {
        return plugin.getSettings().whitelistedEntities.isEmpty() ||
                plugin.getSettings().whitelistedEntities.contains(getType().name()) || plugin.getSettings().whitelistedEntities.contains(getSpawnCause().name());
    }

    @Override
    public boolean isWorldDisabled() {
        return plugin.getSettings().entitiesDisabledWorlds.contains(object.getWorld().getName());
    }

    @Override
    public void remove() {
        plugin.getSystemManager().removeStackObject(this);
        object.remove();
    }

    @Override
    public void updateName() {
        if(EntityUtil.isNameBlacklisted(object.getCustomName()))
            return;

        if(isBlacklisted() || !isWhitelisted() || isWorldDisabled())
            return;

        if(getSpawnCause() == SpawnCause.MYTHIC_MOBS && object.getCustomName() != null){
            object.setCustomName(object.getCustomName().replace("{}", String.valueOf(stackAmount)));
            return;
        }

        String customName = plugin.getSettings().entitiesCustomName;

        if (customName.isEmpty())
            return;

        String newName = "";
        boolean newNameVisible = false;

        if(stackAmount > 1) {
            newName = customName
                    .replace("{0}", Integer.toString(stackAmount))
                    .replace("{1}", EntityUtil.getFormattedType(getType().name()))
                    .replace("{2}", EntityUtil.getFormattedType(getType().name()).toUpperCase());
            newNameVisible = !plugin.getSettings().entitiesHideNames;
        }

        setCustomName(newName);
        setCustomNameVisible(newNameVisible);
    }

    @Override
    public LivingEntity tryStack() {
        int range = plugin.getSettings().entitiesCheckRange;

        //Making sure it's not armor-stand or player
        if (object.getType() == EntityType.ARMOR_STAND || object.getType() == EntityType.PLAYER) {
            remove();
            return null;
        }

        int minimumStackSize = plugin.getSettings().minimumEntitiesLimit.getOrDefault(getType().name(), 1);

        //Checks if minimmum stack size is enabled
        if(minimumStackSize > 1){
            List<Entity> nearbyEntities = plugin.getNMSAdapter().getNearbyEntities(object, range,
                    entity -> entity instanceof LivingEntity && WStackedEntity.of(entity).canStackInto(this));
            List<StackedEntity> entitiesToStack = new ArrayList<>();
            StackedEntity toStackInto = null;
            int totalStackSize = getStackAmount();

            for(Entity nearby : nearbyEntities){
                StackedEntity stackedNearby = WStackedEntity.of(nearby);
                if(canStackInto(stackedNearby)) {
                    entitiesToStack.add(stackedNearby);
                    totalStackSize += stackedNearby.getStackAmount();
                    if(toStackInto == null)
                        toStackInto = stackedNearby;
                }
            }

            if(toStackInto != null && totalStackSize >= minimumStackSize){
                for(StackedEntity stackedEntity : entitiesToStack){
                    stackedEntity.tryStackInto(toStackInto);
                }
                tryStackInto(toStackInto);
                return toStackInto.getLivingEntity();
            }
        }
        else{
            List<Entity> nearbyEntities = plugin.getNMSAdapter().getNearbyEntities(object, range, entity -> entity instanceof LivingEntity);
            for (Entity nearby : nearbyEntities) {
                if (nearby.isValid() && tryStackInto(WStackedEntity.of(nearby))) {
                    return (LivingEntity) nearby;
                }
            }
        }

        updateName();
        return null;
    }

    @Override
    public boolean canStackInto(StackedObject stackedObject) {
        if (!plugin.getSettings().entitiesStackingEnabled)
            return false;

        if (equals(stackedObject) || !(stackedObject instanceof StackedEntity) || !isSimilar(stackedObject))
            return false;

        if(!isWhitelisted() || isBlacklisted() || isWorldDisabled() || isNameBlacklisted() || object.isDead())
            return false;

        StackedEntity targetEntity = (StackedEntity) stackedObject;

        if(!targetEntity.isWhitelisted() || targetEntity.isBlacklisted() || targetEntity.isWorldDisabled() ||
                targetEntity.isNameBlacklisted() || targetEntity.getLivingEntity().isDead())
            return false;

        int newStackAmount = this.getStackAmount() + targetEntity.getStackAmount();

        if(targetEntity.getLivingEntity().hasMetadata("async-stacked") || targetEntity.getLivingEntity().hasMetadata("corpse"))
            return false;

        if (getStackLimit() < newStackAmount)
            return false;

        if (plugin.getSettings().stackDownEnabled && plugin.getSettings().stackDownTypes.contains(object.getType().name())) {
            if (object.getLocation().getY() < targetEntity.getLivingEntity().getLocation().getY()) {
                targetEntity.tryStackInto(this);
                return false;
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")){
            Set<String> regions = new HashSet<>();
            regions.addAll(WorldGuardHook.getRegionsName(targetEntity.getLivingEntity().getLocation()));
            regions.addAll(WorldGuardHook.getRegionsName(object.getLocation()));
            if(regions.stream().anyMatch(region -> plugin.getSettings().entitiesDisabledRegions.contains(region)))
                return false;
        }

        return true;
    }

    @Override
    public boolean tryStackInto(StackedObject stackedObject) {
        if (!canStackInto(stackedObject) /*|| latestStacked.contains(getUniqueId())*/)
            return false;

        if(!Bukkit.isPrimaryThread())
            object.setMetadata("async-stacked", new FixedMetadataValue(plugin, "1"));

        StackedEntity targetEntity = (StackedEntity) stackedObject;

        EntityStackEvent entityStackEvent = new EntityStackEvent(targetEntity, this);
        Bukkit.getPluginManager().callEvent(entityStackEvent);

        if (entityStackEvent.isCancelled()) {
            object.removeMetadata("async-stacked", plugin);
            return false;
        }

        double health = plugin.getSettings().keepLowestHealth ? Math.min(getHealth(), targetEntity.getHealth()) : targetEntity.getHealth();
        int newStackAmount = getStackAmount() + targetEntity.getStackAmount();

        targetEntity.setStackAmount(newStackAmount, false);
        targetEntity.setHealth(health);

        Executor.sync(() -> {
            if (targetEntity.getLivingEntity().isValid())
                targetEntity.updateName();
        }, 2L);

        plugin.getSystemManager().updateLinkedEntity(object, targetEntity.getLivingEntity());

        if(object.getType().name().equals("PARROT"))
            EntityUtil.removeParrotIfShoulder((Parrot) object);

        this.remove();

        return true;
    }

    @Override
    public boolean tryUnstack(int amount) {
        EntityUnstackEvent event = new EntityUnstackEvent(this, amount);
        Bukkit.getPluginManager().callEvent(event);

        if(event.isCancelled())
            return false;

        int stackAmount = this.stackAmount - amount;

        setStackAmount(stackAmount, true);

        if(stackAmount < 1){
            Executor.sync(() -> {
                object.setMetadata("corpse", new FixedMetadataValue(plugin, ""));
                plugin.getNMSAdapter().setHealthDirectly(object, 0);
            }, 2L);
            return true;
        }

        spawnCorpse();

        return true;
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        return stackedObject instanceof StackedEntity && EntityUtil.areEquals(this, (StackedEntity) stackedObject);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StackedEntity ? getUniqueId().equals(((StackedEntity) obj).getUniqueId()) : super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("StackedEntity{uuid=%s,amount=%s,type=%s}", getUniqueId(), getStackAmount(), getType().name());
    }

    /*
     * StackedEntity's methods
     */

    @Override
    public LivingEntity trySpawnerStack(StackedSpawner stackedSpawner) {
        if (!plugin.getSettings().linkedEntitiesEnabled)
            tryStack();

        LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

        if (linkedEntity == null || !tryStackInto(WStackedEntity.of(linkedEntity))) {
            linkedEntity = tryStack();

            if (linkedEntity == null)
                linkedEntity = object;
        }

        stackedSpawner.setLinkedEntity(linkedEntity);

        return linkedEntity;
    }

    @Override
    public StackedEntity spawnDuplicate(int amount) {
        if (amount <= 0)
            return null;

        LivingEntity _duplicate;

        if(getSpawnCause() == SpawnCause.MYTHIC_MOBS && (_duplicate = MythicMobsHook.tryDuplicate(object)) != null) {
            StackedEntity duplicate = WStackedEntity.of(_duplicate);
            duplicate.setStackAmount(amount, true);
            return duplicate;
        }

        StackedEntity duplicate = WStackedEntity.of(plugin.getSystemManager().spawnEntityWithoutStacking(object.getLocation(), getType().getEntityClass(), getSpawnCause()));
        duplicate.setStackAmount(amount, true);

        EntityData entityData = EntityData.of(this);
        entityData.applyEntityData(duplicate.getLivingEntity());
        //EntityUtil.applyEntityData(object, duplicate.getLivingEntity());

        if(plugin.getSettings().keepFireEnabled && object.getFireTicks() > -1)
            duplicate.getLivingEntity().setFireTicks(160);

        DuplicateSpawnEvent duplicateSpawnEvent = new DuplicateSpawnEvent(this, duplicate);
        Bukkit.getPluginManager().callEvent(duplicateSpawnEvent);

        return duplicate;
    }

    @Override
    public void spawnCorpse() {
        plugin.getSystemManager().spawnCorpse(this);
    }

    @Override
    public List<ItemStack> getDrops(int lootBonusLevel) {
        return getDrops(lootBonusLevel, stackAmount);
    }

    @Override
    public List<ItemStack> getDrops(int lootBonusLevel, int stackAmount) {
        ItemStackList drops = new ItemStackList();

//        if(spawnCause == SpawnCause.MYTHIC_MOBS && tempLootTable == null)
//            tempLootTable = MythicMobsHook.getLootTable();
//
//        if(spawnCause == SpawnCause.CUSTOM_BOSSES && tempLootTable == null)
//            tempLootTable = CustomBossesHook.getLootTable();
//
//        if(spawnCause == SpawnCause.EPIC_BOSSES && tempLootTable == null)
//            tempLootTable = EpicBossesHook.getLootTable();

        if(tempLootTable != null){
            drops.addAll(tempLootTable.getDrops(this, lootBonusLevel, stackAmount));
            tempLootTable = null;
        }

        else{
            LootTable lootTable = plugin.getLootHandler().getLootTable(object);
            LootTableCustom lootTableCustom = plugin.getLootHandler().getLootTableCustom();
            drops.addAll(lootTableCustom == null ? lootTable.getDrops(this, lootBonusLevel, stackAmount) :
                    lootTableCustom.getDrops(lootTable, this, lootBonusLevel, stackAmount));
        }

        return drops.toList();
    }

    @Override
    public void setTempLootTable(List<ItemStack> itemStacks) {
        this.tempLootTable = new LootTableTemp() {
            @Override
            public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
                List<ItemStack> drops = new ArrayList<>();

                itemStacks.stream()
                        .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                        .forEach(itemStack -> {
                            ItemStack cloned = itemStack.clone();
                            cloned.setAmount(itemStack.getAmount() * stackAmount);
                            drops.add(cloned);
                        });

                return drops;
            }
        };
    }

    @Override
    public void setLootMultiplier(int multiplier) {
        this.tempLootTable = new LootTableTemp() {
            @Override
            public List<ItemStack> getDrops(StackedEntity stackedEntity, int lootBonusLevel, int stackAmount) {
                List<ItemStack> drops = stackedEntity.getDrops(lootBonusLevel, stackAmount);
                drops.forEach(itemStack -> itemStack.setAmount((itemStack.getAmount() * multiplier)));
                return drops;
            }
        };
    }

    @Override
    @Deprecated
    public int getExp(int defaultExp) {
        return getExp(stackAmount, defaultExp);
    }

    @Override
    public int getExp(int stackAmount, int defaultExp) {
        return plugin.getLootHandler().getLootTable(object).getExp(this, stackAmount);
    }

    @Override
    public void ignoreDeathEvent() {
        ignoreDeathEvent = true;
    }

    @Override
    public boolean isIgnoreDeathEvent() {
        return ignoreDeathEvent;
    }

    @Override
    public CreatureSpawnEvent.SpawnReason getSpawnReason() {
        try{
            return CreatureSpawnEvent.SpawnReason.valueOf(spawnCause.name());
        }catch(Exception ex){
            return CreatureSpawnEvent.SpawnReason.CHUNK_GEN;
        }
    }

    @Override
    public void setSpawnReason(CreatureSpawnEvent.SpawnReason spawnReason) {
        setSpawnCause(spawnReason == null ? null : SpawnCause.valueOf(spawnReason.name()));
    }

    @Override
    public SpawnCause getSpawnCause() {
        return spawnCause == null ? SpawnCause.CHUNK_GEN : spawnCause;
    }

    @Override
    public void setSpawnCause(SpawnCause spawnCause) {
        this.spawnCause = spawnCause == null ? SpawnCause.CHUNK_GEN : spawnCause;
    }

    @Override
    public boolean isNerfed(){
        return plugin.getSettings().nerfedSpawning.contains(getSpawnCause().name()) &&
                plugin.getSettings().nerfedWorlds.contains(object.getWorld().getName());
    }

    @Override
    public boolean isNameBlacklisted() {
        return EntityUtil.isNameBlacklisted(object.getCustomName());
    }

    public static StackedEntity of(Entity entity){
        if(entity instanceof LivingEntity)
            return of((LivingEntity) entity);
        throw new IllegalArgumentException("Only living-entities can be applied to StackedEntity object");
    }

    public static StackedEntity of(LivingEntity livingEntity){
        return plugin.getSystemManager().getStackedEntity(livingEntity);
    }
}
