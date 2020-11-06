package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildstacker.utils.events.EventsCaller;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("SameParameterValue")
public final class SpawnersProvider_SilkSpawners implements SpawnersProvider {

    private final SilkSpawners ss;
    private final SilkUtil silkUtil;

    public SpawnersProvider_SilkSpawners(){
        WildStackerPlugin.log(" - Using SilkSpawners as SpawnersProvider.");
        ss = JavaPlugin.getPlugin(SilkSpawners.class);
        silkUtil = SilkUtil.hookIntoSilkSpanwers();
    }

    @Override
    public ItemStack getSpawnerItem(EntityType entityType, int amount) {
        try {
            //noinspection deprecation
            String entityID = silkUtil.getDisplayNameToMobID().get(entityType.getName());
            return silkUtil.newSpawnerItem(entityID, silkUtil.getCustomSpawnerName(entityID), amount, false);
        }catch(Throwable ex){
            //noinspection deprecation
            short entityID = entityType.getTypeId();
            return newSpawnerItem(entityID, getCreatureName(entityID), amount, false);
        }
    }

    @Override
    public EntityType getSpawnerType(ItemStack itemStack) {
        Object entityType = getStoredSpawnerItemEntityID(itemStack);
        //noinspection deprecation
        return entityType instanceof String ? EntityType.fromName((String) entityType) : EntityType.fromId((short) entityType);
    }

    @Override
    public void handleSpawnerExplode(StackedSpawner stackedSpawner, Entity entity, Player ignite, int brokenAmount) {
        if(stackedSpawner.getStackAmount() <= brokenAmount)
            brokenAmount = brokenAmount - 1;

        Object entityId = getSpawnerEntityID(stackedSpawner.getSpawner());
        int randomNumber = ThreadLocalRandom.current().nextInt(100), dropChance;

        if (ss.mobs.contains("creatures." + entityId + ".explosionDropChance")) {
            dropChance = ss.mobs.getInt("creatures." + entityId + ".explosionDropChance", 100);
        } else {
            dropChance = ss.config.getInt("explosionDropChance", 100);
        }

        if (randomNumber < dropChance)
            dropSpawner(stackedSpawner, ignite, brokenAmount);
    }

    @Override
    public void handleSpawnerBreak(StackedSpawner stackedSpawner, Player player, int brokenAmount, boolean breakMenu) {
        Object entityId = getSpawnerEntityID(stackedSpawner.getSpawner());
        String mobName = Objects.requireNonNull(getCreatureName(entityId)).toLowerCase().replace(" ", "");
        int randomNumber = ThreadLocalRandom.current().nextInt(100), dropChance;

        if (breakMenu || (silkUtil.isValidItemAndHasSilkTouch(player.getInventory().getItemInHand()) &&
                player.hasPermission("silkspawners.silkdrop." + mobName))) {
            if (ss.mobs.contains("creatures." + entityId + ".silkDropChance")) {
                dropChance = ss.mobs.getInt("creatures." + entityId + ".silkDropChance", 100);
            } else {
                dropChance = ss.config.getInt("silkDropChance", 100);
            }

            if (randomNumber < dropChance)
                dropSpawner(stackedSpawner, player, brokenAmount);
        }
    }

    @Override
    public void handleSpawnerPlace(CreatureSpawner creatureSpawner, ItemStack itemStack) {

    }

    @Override
    public void dropSpawner(StackedSpawner stackedSpawner, Player player, int brokenAmount) {
        ItemStack dropItem = EventsCaller.callSpawnerDropEvent(stackedSpawner, player, brokenAmount);
        Location toDrop = ItemUtils.getSafeDropLocation(stackedSpawner.getLocation());

        if (ss.config.getBoolean("dropSpawnerToInventory", false) && player != null) {
            ItemUtils.addItem(dropItem, player.getInventory(), toDrop);
        } else {
            ItemUtils.dropItem(dropItem, toDrop);
        }
    }

    private Object getSpawnerEntityID(CreatureSpawner spawner){
        Object entityId;
        try{
            entityId = SilkUtil.class.getMethod("getSpawnerEntityID", Block.class).invoke(silkUtil, spawner.getBlock());
        }catch(Exception ex){
            throw new IllegalStateException("Couldn't process the getCreatureName of SilkSpawners.");
        }
        return entityId == null || entityId.equals(0) ? silkUtil.getDefaultEntityID() : entityId;
    }

    private Object getStoredSpawnerItemEntityID(ItemStack itemStack){
        Object entityId;
        try{
            entityId = SilkUtil.class.getMethod("getStoredSpawnerItemEntityID", ItemStack.class).invoke(silkUtil, itemStack);
        }catch(Exception ex){
            throw new IllegalStateException("Couldn't process the getCreatureName of SilkSpawners.");
        }
        return entityId == null || entityId.equals(0) ? silkUtil.getDefaultEntityID() : entityId;
    }

    private String getCreatureName(Object entityId){
        Class objectClass = entityId instanceof String ? String.class : short.class;
        try{
            Method method = SilkUtil.class.getMethod("getCreatureName", objectClass);
            return (String) method.invoke(silkUtil, entityId);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    private ItemStack newSpawnerItem(Object entityId, String customName, int amount, boolean forceLore){
        Class objectClass = entityId instanceof String ? String.class : short.class;
        try{
            return (ItemStack) SilkUtil.class.getMethod("newSpawnerItem", objectClass, String.class, int.class, boolean.class)
                    .invoke(silkUtil, entityId, customName, amount, forceLore);
        }catch(Exception ex){
            throw new IllegalStateException("Couldn't process the getCreatureName of SilkSpawners.");
        }
    }

}
