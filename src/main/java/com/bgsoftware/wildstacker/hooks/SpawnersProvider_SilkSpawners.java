package com.bgsoftware.wildstacker.hooks;

import com.bgsoftware.wildstacker.WildStackerPlugin;
import com.bgsoftware.wildstacker.objects.WStackedSpawner;
import com.bgsoftware.wildstacker.utils.Executor;
import com.bgsoftware.wildstacker.utils.items.ItemUtils;
import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.UUID;

@SuppressWarnings("SameParameterValue")
public final class SpawnersProvider_SilkSpawners implements SpawnersProvider {

    private WildStackerPlugin plugin = WildStackerPlugin.getPlugin();
    private static SpawnersProvider_SilkSpawners instance = null;

    private SilkSpawners ss;
    private SilkUtil silkUtil;
    private Random rnd;

    public SpawnersProvider_SilkSpawners(){
        instance = this;
        WildStackerPlugin.log(" - Using SilkSpawners as SpawnersProvider.");
        ss = JavaPlugin.getPlugin(SilkSpawners.class);
        rnd = new Random();
        Executor.sync(() -> silkUtil = SilkUtil.hookIntoSilkSpanwers(), 1L);
    }

    @Override
    public ItemStack getSpawnerItem(CreatureSpawner spawner, int amount) {
        Object entityId = getSpawnerEntityID(spawner);
        String mobName = getCreatureName(entityId).toLowerCase().replace(" ", "");

        ItemStack itemStack = newSpawnerItem(entityId, silkUtil.getCustomSpawnerName(mobName), amount, false);

        if(plugin.getSettings().getStackedItem) {
            itemStack.setAmount(1);
            itemStack = ItemUtils.setSpawnerItemAmount(itemStack, amount);
        }

        if(itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta.hasDisplayName())
                itemMeta.setDisplayName(itemMeta.getDisplayName().replace("{}", ItemUtils.getSpawnerItemAmount(itemStack) + ""));
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    //Called when was broken by an explosion
    @Override
    public void dropOrGiveItem(Entity entity, CreatureSpawner spawner, int amount, UUID explodeSource) {
        boolean drop = true;
        if (ss.config.getBoolean("permissionExplode", false) && entity instanceof TNTPrimed) {
            Entity igniter = ((TNTPrimed) entity).getSource();
            if (igniter instanceof Player) {
                Player sourcePlayer = (Player) igniter;
                drop = sourcePlayer.hasPermission("silkspawners.explodedrop");
            }
        }

        if(drop)
            dropOrGiveItem(explodeSource == null ? null : Bukkit.getPlayer(explodeSource), spawner, amount, explodeSource != null);
    }

    //Code was taken from SilkSpawners's code.
    @Override
    public void dropOrGiveItem(Player player, CreatureSpawner spawner, int amount, boolean isExplodeSource) {
        Object entityId = getSpawnerEntityID(spawner);
        String mobName = getCreatureName(entityId).toLowerCase().replace(" ", "");
        int randomNumber = rnd.nextInt(100), dropChance;

        ItemStack spawnerItem = getSpawnerItem(spawner, amount);

        //If player is null, it broke by an explosion.
        if(player == null){
            if (ss.mobs.contains("creatures." + entityId + ".explosionDropChance")) {
                dropChance = ss.mobs.getInt("creatures." + entityId + ".explosionDropChance", 100);
            } else {
                dropChance = ss.config.getInt("explosionDropChance", 100);
            }
            if (randomNumber < dropChance)
                ItemUtils.dropItem(spawnerItem, spawner.getLocation());
            return;
        }

        if (silkUtil.isValidItemAndHasSilkTouch(player.getInventory().getItemInHand()) &&
                player.hasPermission("silkspawners.silkdrop." + mobName)) {
            if (ss.mobs.contains("creatures." + entityId + ".silkDropChance")) {
                dropChance = ss.mobs.getInt("creatures." + entityId + ".silkDropChance", 100);
            } else {
                dropChance = ss.config.getInt("silkDropChance", 100);
            }

            if (randomNumber < dropChance) {
                if (ss.config.getBoolean("dropSpawnerToInventory", false)) {
                    ItemUtils.addItem(spawnerItem, player.getInventory(), spawner.getLocation());
                } else {
                    ItemUtils.dropItem(spawnerItem, spawner.getLocation());
                }
            }
        }
    }

    @Override
    public void setSpawnerType(CreatureSpawner spawner, ItemStack itemStack, boolean updateName) {
        setSpawnerEntityID(spawner.getBlock(), getStoredSpawnerItemEntityID(itemStack));
        WStackedSpawner.of(spawner).setStackAmount(ItemUtils.getSpawnerItemAmount(itemStack), updateName);
    }

    @Override
    public EntityType getSpawnerType(ItemStack itemStack) {
        Object entityType = getStoredSpawnerItemEntityID(itemStack);
        //noinspection deprecation
        return entityType instanceof String ? EntityType.fromName((String) entityType) : EntityType.fromId((short) entityType);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getSpawnerItem(EntityType entityType, int amount){
        try {
            String mobName = instance.getCreatureName(entityType.name()).toLowerCase().replace(" ", "");
            return instance.newSpawnerItem(entityType.name(), instance.silkUtil.getCustomSpawnerName(mobName), amount, false);
        }catch(Exception ex){
            String mobName = instance.getCreatureName(entityType.getTypeId()).toLowerCase().replace(" ", "");
            return instance.newSpawnerItem(entityType.getTypeId(), instance.silkUtil.getCustomSpawnerName(mobName), amount, false);
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
            return (String) SilkUtil.class.getMethod("getCreatureName", objectClass).invoke(silkUtil, entityId);
        }catch(Exception ex){
            throw new IllegalStateException("Couldn't process the getCreatureName of SilkSpawners.");
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

    private void setSpawnerEntityID(Block block, Object entityId){
        Class objectClass = entityId instanceof String ? String.class : short.class;
        try{
            SilkUtil.class.getMethod("setSpawnerEntityID", Block.class, objectClass).invoke(silkUtil, block, entityId);
        }catch(Exception ex){
            throw new IllegalStateException("Couldn't process the getCreatureName of SilkSpawners.");
        }
    }

    public static boolean isRegisered(){
        return instance != null;
    }

}
