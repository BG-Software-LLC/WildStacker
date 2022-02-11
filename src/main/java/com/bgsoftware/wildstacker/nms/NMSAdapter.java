package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.objects.StackedEntity;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.listeners.ServerTickListener;
import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import com.bgsoftware.wildstacker.utils.legacy.Materials;
import com.bgsoftware.wildstacker.utils.spawners.SyncedCreatureSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface NMSAdapter {

    /*
     *   Entity methods
     */

    <T extends Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause, Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer);

    Zombie spawnZombieVillager(Villager villager);

    void setInLove(Animals entity, Player breeder, boolean inLove);

    boolean isInLove(Animals entity);

    boolean isAnimalFood(Animals animal, ItemStack itemStack);

    int getEntityExp(LivingEntity livingEntity);

    boolean canDropExp(LivingEntity livingEntity);

    void updateLastDamageTime(LivingEntity livingEntity);

    void setHealthDirectly(LivingEntity livingEntity, double health);

    void setEntityDead(LivingEntity livingEntity, boolean dead);

    int getEggLayTime(Chicken chicken);

    void setNerfedEntity(LivingEntity livingEntity, boolean nerfed);

    void setKiller(LivingEntity livingEntity, Player killer);

    boolean canSpawnOn(Entity entity, Location location);

    Collection<Entity> getEntitiesAtChunk(ChunkPosition chunkPosition);

    default Collection<Entity> getNearbyEntities(Location location, int range, Predicate<Entity> filter) {
        return null;
    }

    default float getItemInMainHandDropChance(EntityEquipment entityEquipment) {
        return entityEquipment.getItemInHandDropChance();
    }

    default float getItemInOffHandDropChance(EntityEquipment entityEquipment) {
        return entityEquipment.getItemInHandDropChance();
    }

    default void setItemInMainHand(EntityEquipment entityEquipment, ItemStack itemStack) {
        entityEquipment.setItemInHand(itemStack);
    }

    default void setItemInOffHand(EntityEquipment entityEquipment, ItemStack itemStack) {
        entityEquipment.setItemInHand(itemStack);
    }

    default ItemStack getItemInOffHand(EntityEquipment entityEquipment) {
        return entityEquipment.getItemInHand();
    }

    boolean shouldArmorBeDamaged(ItemStack itemStack);

    default boolean doesStriderHaveSaddle(Strider strider) {
        return false;
    }

    default void removeStriderSaddle(Strider strider) {

    }

    default String getEndermanCarried(Enderman enderman) {
        MaterialData materialData = enderman.getCarriedMaterial();
        //noinspection deprecation
        return materialData.getItemType() + ":" + materialData.getData();
    }

    default byte getMooshroomType(MushroomCow mushroomCow) {
        return 0;
    }

    default void setTurtleEgg(Entity turtle) {

    }

    default Location getTurtleHome(Entity turtle) {
        return null;
    }

    default void setTurtleEggsAmount(Block turtleEggBlock, int amount) {

    }

    default void handleSweepingEdge(Player attacker, ItemStack usedItem, LivingEntity target, double damage) {

    }

    default String getCustomName(Entity entity) {
        return entity.getCustomName();
    }

    default void setCustomName(Entity entity, String name) {
        entity.setCustomName(name);
    }

    default boolean isCustomNameVisible(Entity entity) {
        return entity.isCustomNameVisible();
    }

    default void setCustomNameVisible(Entity entity, boolean visibleName) {
        entity.setCustomNameVisible(visibleName);
    }

    default boolean handleTotemOfUndying(LivingEntity livingEntity) {
        return false;
    }

    /*
     *   Spawner methods
     */

    SyncedCreatureSpawner createSyncedSpawner(CreatureSpawner creatureSpawner);

    boolean isRotatable(Block block);

    /*
     *   Item methods
     */

    StackedItem createItem(Location location, ItemStack itemStack, SpawnCause spawnCause, Consumer<StackedItem> itemConsumer);

    Enchantment getGlowEnchant();

    ItemStack getPlayerSkull(ItemStack bukkitItem, String texture);

    default ItemStack getPlayerSkull(String texture) {
        return getPlayerSkull(Materials.PLAYER_HEAD.toBukkitItem(), texture);
    }

    default boolean isDroppedItem(Entity entity) {
        return entity instanceof Item;
    }

    /*
     *   World methods
     */

    void awardKillScore(Entity bukkitDamaged, Entity damagerEntity);

    default void awardPickupScore(Player player, Item pickItem) {

    }

    void playPickupAnimation(LivingEntity livingEntity, Item item);

    void playDeathSound(LivingEntity entity);

    void playParticle(String particle, Location location, int count, int offsetX, int offsetY, int offsetZ, double extra);

    void playSpawnEffect(LivingEntity livingEntity);

    default Object getBlockData(Material type, short data) {
        throw new UnsupportedOperationException("Not supported in this Minecraft version.");
    }

    default void attemptJoinRaid(Player player, Entity raider) {

    }

    default void startEntityListen(World world) {

    }

    default boolean handlePiglinPickup(Entity bukkitPiglin, Item item) {
        return false;
    }

    boolean handleEquipmentPickup(LivingEntity livingEntity, Item bukkitItem);

    default void giveExp(Player player, int amount) {
        if (amount > 0) {
            PlayerExpChangeEvent playerExpChangeEvent = new PlayerExpChangeEvent(player, amount);
            Bukkit.getPluginManager().callEvent(playerExpChangeEvent);
            if (playerExpChangeEvent.getAmount() > 0)
                player.giveExp(playerExpChangeEvent.getAmount());
        }
    }

    default void enterVehicle(Vehicle vehicle, Entity entity) {
        vehicle.setPassenger(entity);
    }

    default int getPassengersCount(Vehicle vehicle) {
        return vehicle.getPassenger() == null ? 0 : 1;
    }

    /*
     *   Tag methods
     */

    void updateEntity(LivingEntity source, LivingEntity target);

    String serialize(ItemStack itemStack);

    ItemStack deserialize(String serialized);

    ItemStack setTag(ItemStack itemStack, String key, Object value);

    <T> T getTag(ItemStack itemStack, String key, Class<T> valueType, Object def);

    /*
     *   Other methods
     */

    default Object getChatMessage(String message) {
        return message;
    }

    default void runAtEndOfTick(Runnable code) {
        ServerTickListener.addTickEndTask(code);
    }

    /*
     *   Data methods
     */

    void saveEntity(StackedEntity stackedEntity);

    void loadEntity(StackedEntity stackedEntity);

    void saveItem(StackedItem stackedItem);

    void loadItem(StackedItem stackedItem);

}
