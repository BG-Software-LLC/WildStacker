package com.bgsoftware.wildstacker.nms;

import com.google.common.base.Predicate;
import org.bukkit.World;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public interface NMSAdapter {

    Object getNBTTagCompound(LivingEntity livingEntity);

    void setNBTTagCompound(LivingEntity livingEntity, Object _nbtTagCompound);

    boolean isInLove(Entity entity);

    void setInLove(Entity entity, Player breeder, boolean inLove);

    List<ItemStack> getEquipment(LivingEntity livingEntity);

    void addCustomPathfinderGoalBreed(LivingEntity livingEntity);

    @SuppressWarnings("all")
    List<Entity> getNearbyEntities(LivingEntity livingEntity, int range, Predicate<? super Entity> predicate);

    @Nullable
    String serialize(ItemStack itemStack);

    @Nullable
    ItemStack deserialize(String serialized);

    default Object getChatMessage(String message){
        return message;
    }

    ItemStack setTag(ItemStack itemStack, String key, Object value);

    int getEntityExp(LivingEntity livingEntity);

    void setHealthDirectly(LivingEntity livingEntity, double health);

    Random getWorldRandom(World world);

    int getNBTInteger(Object nbtTag);

    int getEggLayTime(Chicken chicken);

}
