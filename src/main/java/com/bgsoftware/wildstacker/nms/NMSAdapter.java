package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.key.Key;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import org.bukkit.Achievement;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface NMSAdapter {

    /*
     *   Entity methods
     */

    <T extends Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> entityConsumer);

    Zombie spawnZombieVillager(Villager villager);

    void setInLove(Animals entity, Player breeder, boolean inLove);

    boolean isInLove(Animals entity);

    boolean isAnimalFood(Animals animal, ItemStack itemStack);

    boolean canBeBred(Ageable entity);

    List<ItemStack> getEquipment(LivingEntity livingEntity);

    int getEntityExp(LivingEntity livingEntity);

    boolean canDropExp(LivingEntity livingEntity);

    void updateLastDamageTime(LivingEntity livingEntity);

    void setHealthDirectly(LivingEntity livingEntity, double health);

    void setEntityDead(LivingEntity livingEntity, boolean dead);

    int getEggLayTime(Chicken chicken);

    void setNerfedEntity(LivingEntity livingEntity, boolean nerfed);

    void setKiller(LivingEntity livingEntity, Player killer);

    boolean canSpawnOn(Entity entity, Location location);

    Set<Entity> getNearbyEntities(Location location, int range, Predicate<Entity> filter);

    default float getItemInMainHandDropChance(EntityEquipment entityEquipment){
        return entityEquipment.getItemInHandDropChance();
    }

    default float getItemInOffHandDropChance(EntityEquipment entityEquipment){
        return entityEquipment.getItemInHandDropChance();
    }

    default void setItemInMainHand(EntityEquipment entityEquipment, ItemStack itemStack){
        entityEquipment.setItemInHand(itemStack);
    }

    default void setItemInOffHand(EntityEquipment entityEquipment, ItemStack itemStack){
        entityEquipment.setItemInHand(itemStack);
    }

    default Key getEndermanCarried(Enderman enderman){
        MaterialData materialData = enderman.getCarriedMaterial();
        //noinspection deprecation
        return Key.of(materialData.getItemType(), materialData.getData());
    }

    /*
     *   Spawner methods
     */

    SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner);

    boolean isRotatable(Block block);

    /*
     *   Item methods
     */

    Item createItem(Location location, ItemStack itemStack, SpawnCause spawnCause, Consumer<Item> itemConsumer);

    Enchantment getGlowEnchant();

    ItemStack getPlayerSkull(String texture);

    /*
     *   World methods
     */

    Stream<BlockState> getTileEntities(Chunk chunk, Predicate<BlockState> condition);

    default void grandAchievement(Player player, EntityType victim, String name){
        grandAchievement(player, "", name);
    }

    default void grandAchievement(Player player, String criteria, String name){
        Achievement achievement = Achievement.valueOf(name);
        if(!player.hasAchievement(achievement))
            player.awardAchievement(achievement);
    }

    void playPickupAnimation(LivingEntity livingEntity, Item item);

    void playDeathSound(LivingEntity entity);

    void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra);

    void playSpawnEffect(LivingEntity livingEntity);

    default Object getBlockData(Material type, short data){
        throw new UnsupportedOperationException("Not supported in this Minecraft version.");
    }

    /*
     *   Tag methods
     */

    Object getNBTTagCompound(LivingEntity livingEntity);

    void setNBTTagCompound(LivingEntity livingEntity, Object _nbtTagCompound);

    String serialize(ItemStack itemStack);

    ItemStack deserialize(String serialized);

    ItemStack setTag(ItemStack itemStack, String key, Object value);

    <T> T getTag(ItemStack itemStack, String key, Class<T> valueType, Object def);

    int getNBTInteger(Object nbtTag);

    /*
     *   Other methods
     */

    default Object getChatMessage(String message){
        return message;
    }

}
