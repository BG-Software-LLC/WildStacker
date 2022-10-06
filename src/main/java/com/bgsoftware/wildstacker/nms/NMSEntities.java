package com.bgsoftware.wildstacker.nms;

import com.bgsoftware.wildstacker.api.enums.SpawnCause;
import com.bgsoftware.wildstacker.api.enums.StackCheckResult;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import com.bgsoftware.wildstacker.utils.legacy.EntityTypes;
import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

public interface NMSEntities {

    <T extends Entity> T createEntity(Location location, Class<T> type, SpawnCause spawnCause,
                                      Consumer<T> beforeSpawnConsumer, Consumer<T> afterSpawnConsumer);

    StackedItem createItem(Location location, ItemStack itemStack, SpawnCause spawnCause, Consumer<StackedItem> itemConsumer);

    ExperienceOrb spawnExpOrb(Location location, SpawnCause spawnCause, int value);

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

    String getEndermanCarried(Enderman enderman);

    byte getMooshroomType(MushroomCow mushroomCow);

    boolean doesStriderHaveSaddle(Entity strider);

    void removeStriderSaddle(Entity strider);

    void setTurtleEgg(Entity turtle);

    Location getTurtleHome(Entity turtle);

    boolean handleTotemOfUndying(LivingEntity livingEntity);

    void sendEntityDieEvent(LivingEntity livingEntity);

    boolean callEntityBreedEvent(LivingEntity child, LivingEntity mother, LivingEntity father,
                                 @Nullable LivingEntity breeder, @Nullable ItemStack bredWith, int experience);

    StackCheckResult areSimilar(EntityTypes entityType, LivingEntity en1, LivingEntity en2);

    boolean checkEntityAttributes(LivingEntity livingEntity, Map<String, Object> attributes);

    void awardKillScore(Entity bukkitDamaged, Entity damagerEntity);

    void awardPickupScore(Player player, Item pickItem);

    void playPickupAnimation(LivingEntity livingEntity, Item item);

    void playDeathSound(LivingEntity entity);

    void playSpawnEffect(LivingEntity livingEntity);

    void handleItemPickup(LivingEntity livingEntity, StackedItem stackedItem, int remaining);

    void handleSweepingEdge(Player attacker, ItemStack usedItem, LivingEntity target, double damage);

    void giveExp(Player player, int amount);

    void enterVehicle(Vehicle vehicle, Entity entity);

    int getPassengersCount(Vehicle vehicle);

    String getCustomName(Entity entity);

    void setCustomName(Entity entity, String name);

    boolean isCustomNameVisible(Entity entity);

    void setCustomNameVisible(Entity entity, boolean visible);

}
