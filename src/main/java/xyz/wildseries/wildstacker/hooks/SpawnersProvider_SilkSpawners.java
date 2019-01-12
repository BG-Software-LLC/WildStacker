package xyz.wildseries.wildstacker.hooks;

import de.dustplanet.silkspawners.SilkSpawners;
import de.dustplanet.util.SilkUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.wildseries.wildstacker.WildStackerPlugin;
import xyz.wildseries.wildstacker.objects.WStackedSpawner;
import xyz.wildseries.wildstacker.utils.ItemUtil;

import java.util.Random;

@SuppressWarnings({"ConstantConditions", "SameParameterValue"})
public final class SpawnersProvider_SilkSpawners implements SpawnersProvider {

    private WildStackerPlugin plugin = WildStackerPlugin.getPlugin();

    private SilkSpawners ss;
    private SilkUtil silkUtil;
    private Random rnd;

    public SpawnersProvider_SilkSpawners(){
        WildStackerPlugin.log(" - Using SilkSpawners as SpawnersProvider.");
        ss = JavaPlugin.getPlugin(SilkSpawners.class);
        rnd = new Random();
        Bukkit.getScheduler().runTaskLater(plugin, () -> silkUtil = SilkUtil.hookIntoSilkSpanwers(), 1L);
    }

    @Override
    public ItemStack getSpawnerItem(CreatureSpawner spawner, int amount) {
        Object entityId = getSpawnerEntityID(spawner);
        String mobName = getCreatureName(entityId).toLowerCase().replace(" ", "");

        ItemStack itemStack = newSpawnerItem(entityId, silkUtil.getCustomSpawnerName(mobName), amount, false);

        if(plugin.getSettings().getStackedItem) {
            itemStack.setAmount(1);
            itemStack = ItemUtil.setSpawnerItemAmount(itemStack, amount);
        }

        return itemStack;
    }

    //Called when was broken by an explosion
    @Override
    public void dropOrGiveItem(Entity entity, CreatureSpawner spawner, int amount) {
        boolean drop = true;
        if (ss.config.getBoolean("permissionExplode", false) && entity instanceof TNTPrimed) {
            Entity igniter = ((TNTPrimed) entity).getSource();
            if (igniter instanceof Player) {
                Player sourcePlayer = (Player) igniter;
                drop = sourcePlayer.hasPermission("silkspawners.explodedrop");
            }
        }

        if(drop)
            dropOrGiveItem(null, spawner, amount);
    }

    //Code was taken from SilkSpawners's code.
    @Override
    public void dropOrGiveItem(Player player, CreatureSpawner spawner, int amount) {
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
                spawner.getWorld().dropItemNaturally(spawner.getLocation(), spawnerItem);
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
                    Bukkit.broadcastMessage("1");
                    ItemUtil.addItem(spawnerItem, player.getInventory(), spawner.getLocation());
                } else {
                    Bukkit.broadcastMessage("2");
                    spawner.getWorld().dropItemNaturally(spawner.getLocation(), spawnerItem);
                }
            }
        }
    }

    @Override
    public void setSpawnerType(CreatureSpawner spawner, ItemStack itemStack, boolean updateName) {
        setSpawnerEntityID(spawner.getBlock(), getStoredSpawnerItemEntityID(itemStack));
        WStackedSpawner.of(spawner).setStackAmount(ItemUtil.getSpawnerItemAmount(itemStack), updateName);
    }

    private Object getSpawnerEntityID(CreatureSpawner spawner){
        Object entityId = null;
        try{
            entityId = SilkUtil.class.getMethod("getSpawnerEntityID", Block.class).invoke(silkUtil, spawner.getBlock());
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return entityId == null || entityId.equals(0) ? silkUtil.getDefaultEntityID() : entityId;
    }

    private Object getStoredSpawnerItemEntityID(ItemStack itemStack){
        Object entityId = null;
        try{
            entityId = SilkUtil.class.getMethod("getStoredSpawnerItemEntityID", ItemStack.class).invoke(silkUtil, itemStack);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return entityId == null || entityId.equals(0) ? silkUtil.getDefaultEntityID() : entityId;
    }

    private String getCreatureName(Object entityId){
        Class objectClass = entityId instanceof String ? String.class : short.class;
        try{
            return (String) SilkUtil.class.getMethod("getCreatureName", objectClass).invoke(silkUtil, entityId);
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
            ex.printStackTrace();
            return null;
        }
    }

    private void setSpawnerEntityID(Block block, Object entityId){
        Class objectClass = entityId instanceof String ? String.class : short.class;
        try{
            SilkUtil.class.getMethod("setSpawnerEntityID", Block.class, objectClass).invoke(silkUtil, block, entityId);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
