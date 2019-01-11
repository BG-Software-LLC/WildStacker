package xyz.wildseries.wildstacker.objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildstacker.api.events.DuplicateSpawnEvent;
import xyz.wildseries.wildstacker.api.events.EntityStackEvent;
import xyz.wildseries.wildstacker.api.events.EntityUnstackEvent;
import xyz.wildseries.wildstacker.api.objects.StackedEntity;
import xyz.wildseries.wildstacker.api.objects.StackedObject;
import xyz.wildseries.wildstacker.api.objects.StackedSpawner;
import xyz.wildseries.wildstacker.hooks.MythicMobsHook;
import xyz.wildseries.wildstacker.utils.EntityData;
import xyz.wildseries.wildstacker.utils.EntityUtil;
import xyz.wildseries.wildstacker.utils.ItemStackList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"RedundantIfStatement", "ConstantConditions"})
public final class WStackedEntity extends WStackedObject<LivingEntity> implements StackedEntity {

    private static Set<UUID> latestStacked = new HashSet<>();

    private boolean ignoreDeathEvent;
    private boolean nerfed;

    public WStackedEntity(LivingEntity livingEntity){
        super(livingEntity, 1);
        ignoreDeathEvent = false;
        nerfed = false;
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
        synchronized (StackedEntity.class) {
            int range = plugin.getSettings().entitiesCheckRange;

            //Making sure it's not armor-stand or player
            if (object.getType() == EntityType.ARMOR_STAND || object.getType() == EntityType.PLAYER)
                return null;

            List<Entity> nearbyEntities = object.getNearbyEntities(range, range, range);

            for (Entity nearby : nearbyEntities) {
                if (nearby instanceof LivingEntity && nearby.isValid() && tryStackInto(WStackedEntity.of(nearby))) {
                    return (LivingEntity) nearby;
                }
            }

            updateName();
            return null;
        }
    }

    @Override
    public boolean canStackInto(StackedObject stackedObject) {
        synchronized (StackedEntity.class) {
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
    }

    @Override
    public boolean tryStackInto(StackedObject stackedObject) {
        synchronized (StackedEntity.class) {
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
        synchronized (StackedEntity.class) {
            if (!plugin.getSettings().linkedEntitiesEnabled)
                return tryStack();

            LivingEntity linkedEntity = stackedSpawner.getLinkedEntity();

            if (linkedEntity == null || !tryStackInto(WStackedEntity.of(linkedEntity))) {
                linkedEntity = tryStack();

                if (linkedEntity == null)
                    linkedEntity = object;
            }

            stackedSpawner.setLinkedEntity(linkedEntity);

            return linkedEntity;
        }
    }

    @Override
    public StackedEntity spawnDuplicate(int amount) {
        if (amount <= 0)
            return null;

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
        return new ItemStackList(plugin.getLootHandler().getLootTable(object).getDrops(this, lootBonusLevel)).toList();
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
