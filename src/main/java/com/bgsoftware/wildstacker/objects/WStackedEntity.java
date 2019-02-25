package com.bgsoftware.wildstacker.objects;

import com.bgsoftware.wildstacker.api.events.DuplicateSpawnEvent;
import com.bgsoftware.wildstacker.api.events.EntityStackEvent;
import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedObject;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.hooks.MythicMobsHook;
import com.bgsoftware.wildstacker.loot.LootTable;
import com.bgsoftware.wildstacker.loot.custom.LootTableCustom;
import com.bgsoftware.wildstacker.utils.EntityData;
import com.bgsoftware.wildstacker.utils.EntityUtil;
import com.bgsoftware.wildstacker.utils.ItemStackList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("RedundantIfStatement")
public class WStackedEntity extends WStackedObject<LivingEntity> implements StackedEntity {

    private static Set<UUID> latestStacked = new HashSet<>();

    private boolean ignoreDeathEvent = false;
    private boolean nerfed = false;
    private com.bgsoftware.wildstacker.api.loot.LootTable tempLootTable = null;

    public WStackedEntity(LivingEntity livingEntity){
        super(livingEntity, 1);
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
    public void remove() {
        plugin.getSystemManager().removeStackObject(this);
        object.remove();
    }

    @Override
    public void updateName() {
        if(EntityUtil.isNameBlacklisted(object.getCustomName()))
            return;

        String customName = plugin.getSettings().entitiesCustomName;

        if (customName.isEmpty())
            return;

        if (stackAmount <= 1) {
            setCustomName("");
            setCustomNameVisible(false);
        }
        else {
            setCustomName(customName
                    .replace("{0}", Integer.toString(stackAmount))
                    .replace("{1}", EntityUtil.getFormattedType(getType().name()))
                    .replace("{2}", EntityUtil.getFormattedType(getType().name()).toUpperCase()));
            setCustomNameVisible(!plugin.getSettings().entitiesHideNames);
        }
    }

    @Override
    public LivingEntity tryStack() {
        int range = plugin.getSettings().entitiesCheckRange;

        List<Entity> nearbyEntities = object.getNearbyEntities(range, range, range);

        //Making sure it's not armor-stand or player
        if (object.getType() == EntityType.ARMOR_STAND || object.getType() == EntityType.PLAYER)
            return null;

        for (Entity nearby : nearbyEntities) {
            if (nearby instanceof LivingEntity && nearby.isValid() && tryStackInto(WStackedEntity.of(nearby))) {
                return (LivingEntity) nearby;
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

        if (plugin.getSettings().entitiesDisabledWorlds.contains(object.getWorld().getName()))
            return false;

        StackedEntity targetEntity = (StackedEntity) stackedObject;
        int newStackAmount = this.getStackAmount() + targetEntity.getStackAmount();

        if (plugin.getSettings().blacklistedEntities.contains(object.getType().name()) ||
                plugin.getSettings().blacklistedEntities.contains(targetEntity.getType().name()))
            return false;

        if (EntityUtil.isNameBlacklisted(object.getCustomName()) ||
                EntityUtil.isNameBlacklisted(((StackedEntity) stackedObject).getLivingEntity().getCustomName()))
            return false;

        if (plugin.getSettings().entitiesLimits.getOrDefault(targetEntity.getType().name(), Integer.MAX_VALUE) < newStackAmount)
            return false;

        if (plugin.getSettings().stackDownEnabled && plugin.getSettings().stackDownTypes.contains(object.getType().name())) {
            if (object.getLocation().getY() < targetEntity.getLivingEntity().getLocation().getY())
                return false;
        }

        if (MythicMobsHook.isMythicMob(object) && !plugin.getSettings().stackMythicMobsEnabled)
            return false;

        return true;
    }

    @Override
    public boolean tryStackInto(StackedObject stackedObject) {
        if (!canStackInto(stackedObject) || latestStacked.contains(getUniqueId()))
            return false;

        StackedEntity targetEntity = (StackedEntity) stackedObject;

        EntityStackEvent entityStackEvent = new EntityStackEvent(targetEntity, this);
        Bukkit.getPluginManager().callEvent(entityStackEvent);

        if (entityStackEvent.isCancelled())
            return false;

        double health = plugin.getSettings().keepLowestHealth ? Math.min(getHealth(), targetEntity.getHealth()) : targetEntity.getHealth();
        int newStackAmount = getStackAmount() + targetEntity.getStackAmount();

        latestStacked.add(getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> latestStacked.remove(getUniqueId()), 5L);

        targetEntity.setStackAmount(newStackAmount, false);
        targetEntity.setHealth(health);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (targetEntity.getLivingEntity().isValid())
                targetEntity.updateName();
        }, 2L);

        plugin.getSystemManager().updateLinkedEntity(object, targetEntity.getLivingEntity());

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

        ignoreDeathEvent();

        if(stackAmount < 1){
            setHealth(0.0D);
            return true;
        }

        //So the entity won't have a name
        setStackAmount(0, true);
        setHealth(0.0D);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            //Spawning the duplicate in the next tick
            StackedEntity duplicate = spawnDuplicate(stackAmount);
            //Updating linked entities
            plugin.getSystemManager().updateLinkedEntity(object, duplicate.getLivingEntity());
        },1L);

        return true;
    }

    @Override
    public boolean isSimilar(StackedObject stackedObject) {
        return stackedObject instanceof StackedEntity && EntityUtil.areEquals(object, ((StackedEntity) stackedObject).getLivingEntity());
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

        if((_duplicate = MythicMobsHook.tryDuplicate(object)) != null) {
            StackedEntity duplicate = WStackedEntity.of(_duplicate);
            duplicate.setStackAmount(amount, true);
            return duplicate;
        }

        StackedEntity duplicate = WStackedEntity.of(plugin.getSystemManager().spawnEntityWithoutStacking(object.getLocation(), getType().getEntityClass()));
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
    public List<ItemStack> getDrops(int lootBonusLevel) {
        return getDrops(lootBonusLevel, stackAmount);
    }

    @Override
    public List<ItemStack> getDrops(int lootBonusLevel, int stackAmount) {
        if((object instanceof Ageable && !((Ageable) object).isAdult()) || ((object instanceof Zombie) && ((Zombie) object).isBaby()))
            return new ArrayList<>();

        if(tempLootTable != null){
            ItemStackList itemStackList = new ItemStackList(tempLootTable.getDrops(this, lootBonusLevel, stackAmount));
            tempLootTable = null;
            return itemStackList.toList();
        }

        LootTable lootTable = plugin.getLootHandler().getLootTable(object);
        LootTableCustom lootTableCustom = plugin.getLootHandler().getLootTableCustom();
        return new ItemStackList(lootTableCustom == null ? lootTable.getDrops(this, lootBonusLevel, stackAmount) :
                lootTableCustom.getDrops(lootTable, this, lootBonusLevel, stackAmount)).toList();
    }

    @Override
    public void setTempLootTable(com.bgsoftware.wildstacker.api.loot.LootTable tempLootTable) {
        this.tempLootTable = tempLootTable;
    }

    @Override
    public void ignoreDeathEvent() {
        ignoreDeathEvent = true;
    }

    @Override
    public boolean isIgnoreDeathEvent() {
        return ignoreDeathEvent;
    }

    //Not an API method!
    public boolean isNerfed(){
        return nerfed;
    }

    public void setNerfed(boolean nerfed){
        this.nerfed = nerfed;
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
