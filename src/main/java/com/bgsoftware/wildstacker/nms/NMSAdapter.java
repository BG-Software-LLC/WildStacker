package com.bgsoftware.wildstacker.nms;

import com.google.common.base.Predicate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMSAdapter {

    Object getNBTTagCompound(LivingEntity livingEntity);

    void setNBTTagCompound(LivingEntity livingEntity, Object _nbtTagCompound);

    boolean isInLove(Entity entity);

    void setInLove(Entity entity, boolean inLove);

    List<ItemStack> getEquipment(LivingEntity livingEntity);

    void addCustomPathfinderGoalBreed(LivingEntity livingEntity);

    @SuppressWarnings("all")
    List<Entity> getNearbyEntities(LivingEntity livingEntity, int range, Predicate<? super Entity> predicate);

}
