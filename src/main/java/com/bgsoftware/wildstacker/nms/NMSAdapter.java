package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import org.bukkit.Achievement;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface NMSAdapter {

    Object getNBTTagCompound(LivingEntity livingEntity);

    void setNBTTagCompound(LivingEntity livingEntity, Object _nbtTagCompound);

    void setInLove(Animals entity, Player breeder, boolean inLove);

    boolean isInLove(Animals entity);

    boolean isAnimalFood(Animals animal, ItemStack itemStack);

    boolean canBeBred(Ageable entity);

    List<ItemStack> getEquipment(LivingEntity livingEntity);

    default List<Entity> getNearbyEntities(Entity entity, int range, Predicate<? super Entity> predicate){
        return getNearbyEntities(entity, range, range, range, predicate);
    }

    List<Entity> getNearbyEntities(Entity entity, int xRange, int yRange, int zRange, Predicate<? super Entity> predicate);

    String serialize(ItemStack itemStack);

    ItemStack deserialize(String serialized);

    default Object getChatMessage(String message){
        return message;
    }

    ItemStack setTag(ItemStack itemStack, String key, Object value);

    <T> T getTag(ItemStack itemStack, String key, Class<T> valueType, Object def);

    int getEntityExp(LivingEntity livingEntity);

    boolean canDropExp(LivingEntity livingEntity);

    void updateLastDamageTime(LivingEntity livingEntity);

    void setHealthDirectly(LivingEntity livingEntity, double health);

    void setEntityDead(LivingEntity livingEntity, boolean dead);

    int getNBTInteger(Object nbtTag);

    int getEggLayTime(Chicken chicken);

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

    void setNerfedEntity(LivingEntity livingEntity, boolean nerfed);

    void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra);

    void playSpawnEffect(LivingEntity livingEntity);

    Enchantment getGlowEnchant();

    ItemStack getPlayerSkull(String texture);

    SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner);

    Zombie spawnZombieVillager(Villager villager);

    <T extends Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> entityConsumer);

    Item createItem(Location location, ItemStack itemStack, SpawnCause spawnCause, Consumer<Item> itemConsumer);

    void setKiller(LivingEntity livingEntity, Player killer);

    default Object getBlockData(Material type, short data){
        throw new UnsupportedOperationException("Not supported in this minecraft version.");
    }

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

}
